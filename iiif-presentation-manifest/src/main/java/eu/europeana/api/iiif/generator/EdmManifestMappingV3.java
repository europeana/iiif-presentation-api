package eu.europeana.api.iiif.generator;

import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

import eu.europeana.api.commons_sb3.definitions.iiif.AcceptUtils;
import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.media.MediaTypes;
import eu.europeana.api.iiif.model.ManifestDefinitions;
import eu.europeana.api.iiif.model.WebResource;
import eu.europeana.api.iiif.model.info.FulltextSummaryAnnoPage;
import eu.europeana.api.iiif.model.info.FulltextSummaryCanvas;
import eu.europeana.api.iiif.service.WebResourceSorter;
import eu.europeana.api.iiif.utils.EdmManifestUtils;
import eu.europeana.api.iiif.utils.GenerateUtils;
import eu.europeana.api.iiif.utils.LanguageMapUtils;
import eu.europeana.api.iiif.v3.model.*;
import eu.europeana.api.iiif.v3.model.content.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.jayway.jsonpath.Filter.filter;
import static com.jayway.jsonpath.Criteria.where;
import static eu.europeana.api.iiif.model.ManifestDefinitions.ATTRIBUTION_STRING;
import static eu.europeana.api.iiif.model.ManifestDefinitions.CANVAS_THUMBNAIL_POSTFIX;
import static eu.europeana.api.iiif.media.MediaType.SOUND;
import static eu.europeana.api.iiif.media.MediaType.VIDEO;
import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

/**
 * This class contains all the methods for mapping EDM record data to IIIF Manifest data for IIIF v3
 *
 * @author Srishti Singh
 * Created on 25-03-2020
 *
 * Updated By Lúthien
 * modified on 15-02-2023
 */
// ignore sonarqube rule: we return null on purpose in this class
// ignore pmd rule:  we want to make a clear which objects are v2 and which v3
public final class EdmManifestMappingV3 implements ManifestGenerator<Manifest> {

    private static final Logger LOG = LogManager.getLogger(EdmManifestMappingV3.class);

    //@TODO: Why is this static?
    private static String thumbnailApiUrl;

    private ManifestSettings     settings;
    private MediaTypes       mediaTypes;

    public EdmManifestMappingV3(ManifestSettings settings
                              , MediaTypes mediaTypes) {
        this.settings   = settings;
        this.mediaTypes = mediaTypes;
        thumbnailApiUrl = settings.getThumbnailApiUrl();
    }

    /**
     * Generates a IIIF v3 manifest based on the provided (parsed) json document
     * @param jsonDoc parsed json document
     * @return IIIF Manifest v3 object
     */
    public Manifest generateManifest(Object jsonDoc) {
        String europeanaId = EdmManifestUtils.getEuropeanaId(jsonDoc);
        String isShownBy = EdmManifestUtils.getValueFromDataProviderAggregation(jsonDoc, europeanaId, "edmIsShownBy");

        // if Item is EU screen then get the mediaTypevalue and the isShownBy value is replaced with isShownAt if empty
        MediaType euScreenTypeHack = ifEuScreenGetMediaType(mediaTypes, jsonDoc, europeanaId, isShownBy);
        Manifest manifest = new Manifest(settings.getManifestId(europeanaId));
        manifest.getServices().add(getServiceDescriptionV3(settings, europeanaId));
        // EA-3325
//        manifest.setPartOf(getWithinV3(jsonDoc));
        manifest.setLabel(getLabels(jsonDoc));
        manifest.setSummary(getDescription(jsonDoc));
        manifest.getMetadata().addAll(getMetaDataV3(jsonDoc));
        manifest.getThumbnail().add(getThumbnailImageV3(europeanaId, jsonDoc));
        manifest.setNavDate(EdmManifestUtils.getNavDate(europeanaId, jsonDoc));
        manifest.getHomepage().add(EdmManifestUtils.getHomePage(europeanaId, jsonDoc));
        manifest.setRequiredStatement(getAttributionV3Root(europeanaId, isShownBy, jsonDoc));
        manifest.setRights(getRights(europeanaId, jsonDoc));
        manifest.setSeeAlso(getDataSetsV3(settings, europeanaId));
        // get the canvas items and if present add to manifest
        List<Canvas> items = getItems(settings, mediaTypes, europeanaId, isShownBy, jsonDoc, euScreenTypeHack);
        if (items != null && items.size() > 0) {
            manifest.getItems().addAll(items);
            // TODO get missing fields - c.getStartCanvasAnnotation().getBody().getId()
           // manifest.setStart(getStartCanvasV3(manifest.getItems(), isShownBy));
        } else {
            LOG.debug("No Canvas generated for europeanaId {}", europeanaId);
        }
        return manifest;
    }

    /**
     * We generate all full text links in one place, so we can raise a timeout if retrieving the necessary
     * data for all full texts is too slow.
     * From EA-2604 on, originalLanguage is available on the FulltextSummaryCanvas and copied to the AnnotationBody if
     * motivation = 'painting'
     */
    public void fillWithFullText(Manifest manifest, Map<String, FulltextSummaryCanvas> summary) {
        if ( summary == null ) { return; }

        if ( !manifest.hasItems() ) { return; }

        for (Canvas canvas : manifest.getItems()) {
            // we need to generate the same annopageId hash based on imageId
            String apHash = GenerateUtils.derivePageId(canvas.getStartCanvasAnnotation().getBody().getID());
            FulltextSummaryCanvas ftCanvas = summary.get(apHash);
            if (ftCanvas == null) {
                // This warning can be logged for empty pages that do not have a fulltext, but if we get a lot
                // then Record API and Fulltext API are not in sync (or the hashing algorithm changed).
                LOG.warn("Inconsistent data! No fulltext annopage found for record {} page {}. Generated hash = {}",
                        manifest.getID(), canvas.getID(), apHash);
            } else {
                addFulltextLinkToCanvas(canvas, ftCanvas);
            }
        }
    }

    private void addFulltextLinkToCanvas(eu.europeana.api.iiif.v3.model.Canvas canvas, FulltextSummaryCanvas summaryCanvas) {
        List<AnnotationPage> summaryAnnoPages = new ArrayList<>();
        createFTSummaryAnnoPages(summaryAnnoPages, summaryCanvas);
        canvas.getAnnotations().addAll(summaryAnnoPages);
        for (eu.europeana.api.iiif.v3.model.AnnotationPage ap : canvas.getItems()) {
            for (eu.europeana.api.iiif.v3.model.Annotation ann : ap.getItems()) {
                // for translations originalLanguage will be null
                if (StringUtils.equalsAnyIgnoreCase(ann.getMotivation(), "painting") && summaryCanvas.getOriginalLanguage() != null) {
                    ann.getBody().setLanguage(summaryCanvas.getOriginalLanguage());
                }
            }
        }
    }

    private void createFTSummaryAnnoPages(List<AnnotationPage> summaryAnnoPages, FulltextSummaryCanvas summaryCanvas) {
        for (FulltextSummaryAnnoPage sap : summaryCanvas.getFTSummaryAnnoPages()) {
            AnnotationPage page = new AnnotationPage(sap.getID());
            summaryAnnoPages.add(page);
            // TODO language source and text graulraity field in AnnotationPage class
            //summaryAnnoPages.add(new AnnotationPage(sap.getId(), sap.getLanguage(), sap.getTextGranularity(), sap.getSource()));
        }
    }

    /**
     * Eu screen items are only checked in format/ iiif version 3
     * If there is a edm:isShownAt or edm:isShownBy starting with http(s)://www.euscreen.eu/item.html
     * and a proxy with edmType = SOUND or VIDEO, then generate a Canvas with that URL
     *
     * @param jsonDoc
     * @param europeanaId
     * @param isShownBy
     * @return
     */
    private static MediaType ifEuScreenGetMediaType(MediaTypes mediaTypes, Object jsonDoc, String europeanaId, String isShownBy) {
        MediaType euScreenTypeHack = null;
        //1. find edmType (try first Europeana Proxy, use other proxies as fallback)
        String edmType = (String) EdmManifestUtils.getFirstValueArray("edmType", europeanaId,
                JsonPath.parse(jsonDoc).read("$.object.proxies[?(@.europeanaProxy == true)].edmType", String[].class));
        if (StringUtils.isEmpty(edmType)) {
            edmType = (String) EdmManifestUtils.getFirstValueArray("edmType", europeanaId,
                    JsonPath.parse(jsonDoc).read("$.object.proxies[?(!@.lineage && @.europeanaProxy != true )].edmType", String[].class));
        }

        //2. get isShownAt
        String isShownAt = EdmManifestUtils.getValueFromDataProviderAggregation(jsonDoc, europeanaId, "edmIsShownAt");
        LOG.debug("isShownAt = {}", isShownAt);

        // 3. check if it's a EUScreen item
        if (isEuScreenItem(edmType, isShownBy, isShownAt)) {
            LOG.debug("Item is EUScreen :  edmType - {}, isShownBy - {}", edmType, isShownBy);
            // if the item is EUscreen then the value will always be present
            euScreenTypeHack=  mediaTypes.getEUScreenType(edmType).orElse(null);
            isShownBy = isShownAt; // replace isShownBy with IsShownAt for EU Screen items
        }
        return euScreenTypeHack;
    }

    /**
     * If there is a edm:isShownAt or edm:isShownBy starting with http(s)://www.euscreen.eu/item.html
     * and a proxy with edmType = SOUND or VIDEO
     *
     * @param edmType
     * @param isShownBy
     * @param isShownAt
     * @return
     */
    private static boolean isEuScreenItem(String edmType, String isShownBy, String isShownAt) {
        String isShownAtOrBy = StringUtils.isBlank(isShownBy) ? isShownAt : isShownBy;
        return (isShownAtOrBy != null && (VIDEO.equalsIgnoreCase(edmType) || SOUND.equalsIgnoreCase(edmType))  &&
                (isShownAtOrBy.startsWith("http://www.euscreen.eu/item.html") ||
                        isShownAtOrBy.startsWith("https://www.euscreen.eu/item.html")));
    }


    /**
     * Generates Service descriptions for the manifest
     */
    private static Service getServiceDescriptionV3(ManifestSettings ms, String europeanaId) {
        Service service = new Service(ms.getContentSearchURL(europeanaId), null);
        service.setContext(ManifestDefinitions.SEARCH_CONTEXT_VALUE);
        service.setProfile(ManifestDefinitions.SEARCH_PROFILE_VALUE);
        return service;
    }

    // TODO used in testing
    /**
     * Create a collection for all proxy.dctermsIsPartOf that start with "http://data.theeuropeanlibrary.org/
     * @param jsonDoc parsed json document
     * @return
     */
//    static Collection[] getWithinV3(Object jsonDoc) {
//        List<String> collections = EdmManifestUtils.getEuropeanaLibraryCollections(jsonDoc);
//        if (collections.isEmpty()) {
//            return null;
//        }
//        List<Collection> result = new ArrayList<>(collections.size());
//        for (String collection : collections) {
//            result.add(new Collection(collection));
//        }
//        return result.toArray(new Collection[0]);
//    }

    /**
     * We first check all proxies for a title. If there are no titles, then we check the description fields
     * @param jsonDoc parsed json document
     * @return
     */
    protected static LanguageMap getLabels(Object jsonDoc)  {
        LanguageMap[] maps = JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcTitle", LanguageMap[].class);
        if (maps == null || maps.length == 0) {
            maps = JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcDescription", LanguageMap[].class);
        }
        return LanguageMapUtils.mergeLanguageMaps(maps);
    }


    /**
     * Returns the values from the proxy.dcDescription fields, but only if they aren't used as a label yet.
     * @param jsonDoc parsed json document
     * @return
     */
    protected static LanguageMap getDescription(Object jsonDoc) {
        if (JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcTitle", LanguageMap[].class).length > 0) {
            return LanguageMapUtils.mergeLanguageMaps(JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcDescription", LanguageMap[].class));
        }
        return null;
    }

    /**
     * Reads the dcDate, dcFormat, dcRelation, dcType, dcLanguage and dcSource values from all proxies and puts them in a
     * LanguageMap with the appropriate label
     * @param jsonDoc parsed json document
     * @return
     */
    static List<LabelledValue> getMetaDataV3(Object jsonDoc) {
        List<LabelledValue> metaData = new ArrayList<>();
        addMetaDataV3(metaData, "date", jsonDoc, "$.object.proxies[*].dcDate");
        addMetaDataV3(metaData, "format", jsonDoc, "$.object.proxies[*].dcFormat");
        addMetaDataV3(metaData, "relation", jsonDoc, "$.object.proxies[*].dcRelation");
        addMetaDataV3(metaData, "type", jsonDoc, "$.object.proxies[*].dcType");
        addMetaDataV3(metaData, "language", jsonDoc,"$.object.proxies[*].dcLanguage");
        addMetaDataV3(metaData, "source", jsonDoc, "$.object.proxies[*].dcSource");
        if (!metaData.isEmpty()) {
            return metaData;
        }
        return null;
    }


    private static void addMetaDataV3(List<LabelledValue> metaData, String fieldName, Object jsonDoc, String jsonPath) {
        // We go over all meta data values and check if it's an url or not.
        // Non-url values are always included as is. If it's an url then we wrap that with an html anchor tag.
        // Additionally we check if the url is also present in object.timespans, agents, concepts or places. If so we
        // add the corresponding preflabels (in all available languages) as well.

        LanguageMap[] metaDataValues = JsonPath.parse(jsonDoc).read(jsonPath, LanguageMap[].class);

        for (LanguageMap metaDataValue : metaDataValues) {
            LanguageMap metaDataLabel = new LanguageMap(LanguageMap.DEFAULT_METADATA_KEY, fieldName);
            LOG.trace("START '{}' value map: {} ", fieldName, metaDataValue);

            List<LanguageMap> extraPrefLabelMaps = new ArrayList<>(); // keep track of extra prefLabels we need to add
            for (Map.Entry<String, List<String>> entry : metaDataValue.entrySet()) {
                String      key = entry.getKey();
                List<String> values = entry.getValue();

                LOG.trace("  checking key {} with {} values", key, values.size());

                List<String> newValues = new ArrayList<>(); // recreate all values (because we may change one)
                for (String value : values) {
                    processMetaDataValue(value, newValues, jsonDoc, extraPrefLabelMaps);
                }

                // replace old values with new ones for the current key
                LOG.trace("  done checking key = {}, new values = {}", key, newValues);
                metaDataValue.replace(key, newValues);
            }
            // if there are extra prefLabel maps, we merge all into our metaDataValues map
            if (!extraPrefLabelMaps.isEmpty()) {
                LOG.trace("  adding extra preflabels = {}", extraPrefLabelMaps);
                // add the original languagemap
                extraPrefLabelMaps.add(0, metaDataValue);
                metaDataValue = LanguageMapUtils.mergeLanguageMaps(extraPrefLabelMaps.toArray(new LanguageMap[0]));
            }
            LOG.trace("FINISH '{}' value map = {}", fieldName, metaDataValue);

            metaData.add(new LabelledValue(metaDataLabel, metaDataValue));
        }
    }


    static void processMetaDataValue(String value, List<String> newValues, Object jsonDoc,
                                     List<LanguageMap> extraPrefLabelMaps) {
        LOG.trace("  processing value {}", value);
        if (EdmManifestUtils.isUrl(value)) {
            // 1. add html anchor tag to current value
            String newValue = "<a href='" + value + "'>" + value + "</a>";
            LOG.trace("    isUrl -> newValue = {} ", newValue);
            newValues.add(newValue);

            // 2. check if we should add extra preflabels
            LanguageMap extraPrefLabelMap = getTimespanAgentConceptOrPlaceLabels(jsonDoc, value);
            if (extraPrefLabelMap != null) {
                LOG.trace("    isUrl -> extraLabels = {}", extraPrefLabelMap);
                extraPrefLabelMaps.add(extraPrefLabelMap);
            }
        } else {
            // no url, we keep it.
            newValues.add(value);
        }
    }

    public static LanguageMap getTimespanAgentConceptOrPlaceLabels(Object jsonDoc, String value) {
        LanguageMap result = getEntityPrefLabels(jsonDoc, "timespans", value);
        if (result != null) {
            return result;
        }
        result = getEntityPrefLabels(jsonDoc, "agents", value);
        if (result != null) {
            return result;
        }
        result = getEntityPrefLabels(jsonDoc, "concepts", value);
        if (result != null) {
            return result;
        }
        return getEntityPrefLabels(jsonDoc, "places", value);
    }

    private static LanguageMap getEntityPrefLabels(Object jsonDoc, String entityName, String value) {
        Filter aboutFilter = filter(where(EdmManifestUtils.ABOUT).is(value));
        LanguageMap[] labels = JsonPath.parse(jsonDoc).
                read("$.object[?(@." + entityName + ")]." + entityName + "[?].prefLabel", LanguageMap[].class, aboutFilter);
        if (labels.length > 0) {
            return labels[0];
        }
        return null;
    }

    /**
     * Return the first license description we find in any 'aggregation.edmRights' field. Note that we first try the europeanaAggregation and if
     * that doesn't contain an edmRights, we check the other aggregations
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @param jsonDoc parsed json document
     * @return Rights object containing rights information
     */
    public static Text getRights(String europeanaId, Object jsonDoc) {
        String licenseText = EdmManifestUtils.getLicenseText(europeanaId, jsonDoc);
        if (StringUtils.isEmpty(licenseText)) {
            return null;
        }
        return new Text(licenseText);
    }

    /**
     * Return array with the id of the thumbnail as defined in 'europeanaAggregation.edmPreview'
     * @param jsonDoc parsed json document
     * @return Image object, or null if no edmPreview was found
     */
    static Image getThumbnailImageV3(String europeanaId, Object jsonDoc) {
        String thumbnailId = EdmManifestUtils.getThumbnailId(europeanaId, jsonDoc);
        if (StringUtils.isEmpty(thumbnailId)) {
//            return new eu.europeana.iiif.model.v3.Image[] {};
            return null;
        }
        return new Image(thumbnailId);
    }

    /**
     * EA-3325 Return array with the id of the canvas-specific thumbnail created from the Webresource id
     * @param webresourceId hasview image ID
     * @return Image object, or null if either provided String was null
     */
    static Image getCanvasThumbnailImageV3(String webresourceId) {
        if (StringUtils.isAnyEmpty(thumbnailApiUrl, webresourceId)) {
            return new Image();
        }
        return new Image(thumbnailApiUrl + webresourceId + CANVAS_THUMBNAIL_POSTFIX);
    }


    /**
     * Return attribution text as a String
     * We look for the webResource that corresponds to our edmIsShownBy and return the attribution snippet for that.
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @param isShownBy edmIsShownBy value
     * @param jsonDoc parsed json document
     * @return
     */
    static LabelledValue getAttributionV3Root(String europeanaId, String isShownBy, Object jsonDoc) {
        Filter isShownByFilter = filter(where(EdmManifestUtils.ABOUT).is(isShownBy));
        String[] attributions = JsonPath.parse(jsonDoc).
                read("$.object.aggregations[*].webResources[?]."+ EdmManifestUtils.HTML_ATTRIB_SNIPPET, String[].class, isShownByFilter);
        String attribution = (String) EdmManifestUtils.getFirstValueArray(EdmManifestUtils.HTML_ATTRIB_SNIPPET, europeanaId, attributions);
        return createRequiredStatementMap(attribution);
    }

    static LabelledValue createRequiredStatementMap(String attribution){
        if (StringUtils.isEmpty(attribution)) {
            return null;
        }
        return new LabelledValue(new LanguageMap(LanguageMap.DEFAULT_METADATA_KEY, ATTRIBUTION_STRING),
                                        new LanguageMap(LanguageMap.DEFAULT_METADATA_KEY, attribution));
    }

    /**
     * Generates 3 datasets with the appropriate ID and format (one for rdf/xml, one for json and one for json-ld)
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @return array of 3 datasets
     */
    static List<Dataset> getDataSetsV3(ManifestSettings settings, String europeanaId) {
        List<Dataset> result = new ArrayList<>(3);
        result.add(new Dataset(settings.getDatasetId(europeanaId, ".json-ld"), AcceptUtils.MEDIA_TYPE_JSONLD));
        result.add(new Dataset(settings.getDatasetId(europeanaId, ".json"), org.springframework.http.MediaType.APPLICATION_JSON_VALUE));
        result.add(new Dataset(settings.getDatasetId(europeanaId, ".rdf"), ManifestDefinitions.MEDIA_TYPE_RDF));
        return result;
    }


    // TODO get missing fields

    /**
     * @return the {@link eu.europeana.iiif.model.v3.Canvas} that refers to edmIsShownBy, or else just the first Canvas
     */
//    static Canvas getStartCanvasV3(List<Canvas> items, String edmIsShownBy) {
//        if (items == null) {
//            LOG.trace("Start canvas = null (no canvases present)");
//            return null;
//        }
//
//        Canvas result = null;
//        for (Canvas c : items) {
//            String annotationBodyId = c.getStartCanvasAnnotation().getBody().getId();
//            if (!StringUtils.isEmpty(edmIsShownBy) && edmIsShownBy.equals(annotationBodyId)) {
//                result = c;
//                LOG.trace("Start canvas = {} (matches with edmIsShownBy)", result.getPageNr());
//                break;
//            }
//        }
//        // nothing found, return first canvas
//        if (result == null) {
//            result = items[0];
//            LOG.trace("Start canvas = {} (no match with edmIsShownBy, select first)", result.getPageNr());
//        }
//        return new eu.europeana.iiif.model.v3.Canvas(result.getId(), result.getPageNr());
//    }

    /**
     * Generates an ordered array of {@link Canvas}es referring to edmIsShownBy and hasView {@link WebResource}s.
     * For more information about the ordering @see {@link WebResourceSorter}
     * @param europeanaId
     * @param isShownBy
     * @param jsonDoc
     * @return array of Canvases
     */
    static List<Canvas> getItems(ManifestSettings settings, MediaTypes mediaTypes, String europeanaId, String isShownBy, Object jsonDoc, MediaType euScreenTypeHack) {
        // generate canvases in a same order as the web resources
        List<WebResource> sortedResources = EdmManifestUtils.getSortedWebResources(europeanaId, isShownBy, jsonDoc);
        if (sortedResources.isEmpty()) {
            return null;
        }
        int order = 1;
        Map<String, Object>[] services = JsonPath.parse(jsonDoc).read("$.object[?(@.services)].services[*]", Map[].class);
        List<Canvas> canvases = new ArrayList<>(sortedResources.size());
        for (WebResource webResource: sortedResources) {
            Canvas canvas = getCanvasV3(settings, mediaTypes, europeanaId, order, webResource, services, euScreenTypeHack);
            // for non supported media types we do not create any canvas. Case-4 of media type handling : See-EA-3413
            if (canvas != null) {
                canvases.add(canvas);
                order++;
            }
        }
        return canvases;

    }


    /**
     * Generates a new canvas, but note that we do not fill the otherContent (Full-Text) here. That's done later.
     */
    private static Canvas getCanvasV3(ManifestSettings settings,
                                                                 MediaTypes mediaTypes,
                                                                 String europeanaId,
                                                                 int order,
                                                                 WebResource webResource,
                                                                 Map<String, Object>[] services,
                                                                 MediaType euScreenTypeHack){
        // Canvas c = new Canvas(settings.getCanvasId(europeanaId, order), order);
        Canvas c = new Canvas(settings.getCanvasId(europeanaId, order));

        c.setLabel(new LanguageMap(null, "p. "+order));

        Object obj = webResource.get(EdmManifestUtils.EBUCORE_HEIGHT);
        if (obj instanceof Integer){
            c.setHeight((Integer) obj);
        }

        obj = webResource.get(EdmManifestUtils.EBUCORE_WIDTH);
        if (obj instanceof Integer){
            c.setWidth((Integer) obj);
        }

        String durationText = (String) webResource.get(EdmManifestUtils.EBUCORE_DURATION);
        if (durationText != null) {
            Long durationInMs = Long.valueOf(durationText);
            c.setDuration(durationInMs / 1000D);
        }

        String attributionText = (String) webResource.get(EdmManifestUtils.HTML_ATTRIB_SNIPPET);
        if (!StringUtils.isEmpty(attributionText)){
            c.setRequiredStatement(createRequiredStatementMap(attributionText));
        }

        LinkedHashMap<String, ArrayList<String>> license = (LinkedHashMap<String, ArrayList<String>>) webResource.get("webResourceEdmRights");
        if (license != null && !license.values().isEmpty()) {
            c.setRights(new Text(license.values().iterator().next().get(0)));
        }

        //EA-3325: check if the webResource has a "svcsHasService"; if not, add a thumbnail
        if (Objects.isNull(webResource.get(EdmManifestUtils.SVCS_HAS_SERVICE))){
            c.getThumbnail().add(getCanvasThumbnailImageV3(URLEncoder.encode(webResource.getId(), StandardCharsets.UTF_8)));
        }

        // a canvas has 1 annotation page by default (an extra annotation page is added later if there is a full text available)
        AnnotationPage annoPage = new AnnotationPage(null); // id is not really necessary in this case
        c.getItems().add(annoPage);

        // Add annotation - annotation page has 1 annotation
        Annotation anno = new Annotation(null);
        annoPage.getItems().add(anno);
        anno.setTarget(c.getID());

        // Fetch the mime type from the web resource
        String ebucoreMimeType = (String) webResource.get(EdmManifestUtils.EBUCORE_HAS_MIMETYPE);
        MediaType mediaType = null;
        // MEDIA TYPE handling..
        // case 1 -  EU screen items. Override the media type
        if (euScreenTypeHack != null) {
            LOG.debug("Override mediaType {} with {} because of EUScreen hack", mediaType, euScreenTypeHack);
            mediaType = euScreenTypeHack;
            anno.setTimeMode(TimeMode.trim); // as it's AV
        } else {
            // get the mediaType from the mimetype fetched
            Optional<MediaType> media = mediaTypes.getMediaType(ebucoreMimeType);
            if (media.isPresent()) {
                mediaType = media.get();
            }
        }

        // CASE 4 -  No canvas should be generated -
        // if media type is not supported (media type is null)
        // OR if item is not EU screen and media type is not either browser or rendered
        // See - EA-3413
        if (mediaType == null || (euScreenTypeHack == null && ifMediaTypeIsNotBrowserOrRendered(mediaType))) {
            LOG.debug("No canvas added for webresource {} as the media type - {} is invalid or not supported.",
                    webResource.get(EdmManifestUtils.ABOUT),
                    ebucoreMimeType);
            return null;
        }

        // Now create the annotation body with webresource url and media type
        // EA- 3436 add technical metadata for case 2 and 3
        ContentResource annoBody = getAnnotationBody(webResource, mediaType, anno,c);
        // annotation has 1 annotationBody
        anno.setBody(annoBody);
        // body can have a service
        setServiceIdForAnnotation(europeanaId, webResource, services, annoBody);
        return c;
    }

    private static Canvas createCanvas(ManifestSettings settings, String europeanaId, int order) {
        Canvas c = new Canvas(settings.getCanvasId(europeanaId, order));
        c.setLabel(new LanguageMap(null, "p. "+ order));
        return c;
    }

    private static ContentResource getAnnotationBody(WebResource webResource, MediaType mediaType,
        Annotation anno, Canvas c) {

        ContentResource annoBody = instantiateBodyWithType(mediaType,(String) webResource.get(EdmManifestUtils.ABOUT));
        //ContentResource annoBody = new ContentResource((String) webResource.get(EdmManifestUtils.ABOUT), mediaType.getType());

        // case 2 - browser supported
        if (mediaType.isBrowserSupported() ) {
            annoBody.setFormat(mediaType.getMimeType());
            // add timeMode for AV
            if (mediaType.isVideoOrSound()) {
                anno.setTimeMode(TimeMode.trim);
            }
            addTechnicalMetadata(c, annoBody);
        }
        // case 3 - rendered - No time mode added as we paint an image here
        if(mediaType.isRendered()) {
            // Use the URL of the thumbnail for the respective WebResource as id of the Annotation Body
            if(c.hasThumbnail()) {
                annoBody = new Image(c.getThumbnail().get(0).getID());
            }
            // update the width and height
            setHeightWidthForRendered(c);
           //EA-3745 - use media type 'service' for oembed mimeTypes who do not have type configured in 'mediacategories.xml'
            String mediaTypeValue=StringUtils.isEmpty(mediaType.getType()) && EdmManifestUtils.EMBEDED_RESOURCE_MIME_TYPES.contains(
                mediaType.getMimeType()) ?
                EdmManifestUtils.SERVICE: mediaType.getType();
            // TODO check the field (right now string) add rendering in canvas for original web resource url
//            c.setRendering(new Rendering((String) webResource.get(EdmManifestUtils.ABOUT),
//                    mediaTypeValue,
//                    mediaType.getMimeType(),
//                    new LanguageMap(EdmManifestUtils.LINGUISTIC, mediaType.getLabel())));
            addTechnicalMetadata(c, annoBody);
        }
        return annoBody;
    }

    private static void setServiceIdForAnnotation(String europeanaId, WebResource webResource,
        Map<String, Object>[] services, ContentResource annoBody) {
        String serviceId = EdmManifestUtils.getServiceId(webResource, europeanaId);
        if (serviceId != null) {
            Service service = new Service(serviceId, ManifestDefinitions.IMAGE_SERVICE_TYPE_3);
            service.setProfile(EdmManifestUtils.lookupServiceDoapImplements(services, serviceId,
                europeanaId));
            annoBody.getServices().add(service);
        }
    }

    private static void setThumbnailIfRequired(WebResource webResource, Canvas c) {
        //EA-3325: check if the webResource has a "svcsHasService"; if not, add a thumbnail
        if (Objects.isNull(webResource.get(EdmManifestUtils.SVCS_HAS_SERVICE))){
            c.getThumbnail().add(getCanvasThumbnailImageV3(webResource.getId()));
        }
    }

    private static void setRightsForCanvas(WebResource webResource, Canvas c) {
        LinkedHashMap<String, ArrayList<String>> license = (LinkedHashMap<String, ArrayList<String>>) webResource.get("webResourceEdmRights");
        if (license != null && !license.values().isEmpty()) {
            c.setRights(new Text(license.values().iterator().next().get(0)));
        }
    }

    private static void setRequiredStatementForCanvas(WebResource webResource, Canvas c) {
        String attributionText = (String) webResource.get(EdmManifestUtils.HTML_ATTRIB_SNIPPET);
        if (!StringUtils.isEmpty(attributionText)){
            c.setRequiredStatement(createRequiredStatementMap(attributionText));
        }
    }

    private static void setDurationForCanvas(WebResource webResource, Canvas c) {
        String durationText = (String) webResource.get(EdmManifestUtils.EBUCORE_DURATION);
        if (durationText != null) {
            Long durationInMs = Long.valueOf(durationText);
            c.setDuration(durationInMs / 1000D);
        }
    }

    private static void setHeightAndWidthForCanvas(WebResource webResource, Canvas c) {
        Object obj = webResource.get(EdmManifestUtils.EBUCORE_HEIGHT);
        if (obj instanceof Integer val){
            c.setHeight(val);
        }
        obj = webResource.get(EdmManifestUtils.EBUCORE_WIDTH);
        if (obj instanceof Integer val){
            c.setWidth(val);
        }
    }

    /**
     * If media type is not either Browser or Rendered
     *
     * NOTE - This is will not happen with the media categories configured for now.
     * As if media type is present it will either be browser or rendered.
     * But for future if something else is added in the XML file
     * the code should be resilient to handle that
     * @param mediaType
     * @return
     */
    private static boolean ifMediaTypeIsNotBrowserOrRendered(MediaType mediaType) {
        return mediaType != null && !(mediaType.isRendered() || mediaType.isBrowserSupported());
    }

    /**
     * Adds the technical metadata in the annotation body of the canvas
     * @param canvas
     * @param body
     */
    // TODO see how to set these values based on type of content resource
    private static void addTechnicalMetadata(Canvas canvas, ContentResource body) {
//        body.setHeight(canvas.getHeight());
//        body.setWidth(canvas.getWidth());
//        body.setDuration(canvas.getDuration());
    }

    /**
     * Update the width and height of the canvas based
     * on few conditons
     * @param c
     */
    private static void setHeightWidthForRendered(Canvas c) {
        Integer height = c.getHeight();
        Integer width = c.getWidth();

        if (height != null && width != null) {
            // width is higher (and equal) than 400px - Set width = 400 ; Set height = (height / width) x 400
            if (width >= 400) {
                c.setWidth(400);
                c.setHeight((int) ((height/(width.doubleValue())) * 400));
            }
        } else {
            // if the WebResource does not have width or height
            // Set width and height to 400 (this is the size of the default icon which is what will likely be displayed)
            c.setHeight(400);
            c.setWidth(400);
        }
    }

    private static ContentResource instantiateBodyWithType(MediaType mediaType, String id) {
        switch (mediaType.getType()) {
            case Dataset: return new Dataset(id);
            case Image: return new Image(id);
            case Model: return new Model(id);
            case Sound: return new Sound(id);
            case Text: return new Text(id);
            case Video: return new Video(id);
            default: return new Other(id);
        }
    }
}
