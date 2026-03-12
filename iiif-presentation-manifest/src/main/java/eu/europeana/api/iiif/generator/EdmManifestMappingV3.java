package eu.europeana.api.iiif.generator;

import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

import eu.europeana.api.commons_sb3.definitions.iiif.AcceptUtils;
import eu.europeana.api.iiif.generator.utils.MediaGenerator;
import eu.europeana.api.iiif.generator.utils.MediaGeneratorType;
import eu.europeana.api.iiif.generator.utils.MediaGeneratorVersion;
import eu.europeana.api.iiif.generator.utils.MediaGeneratorRegistry;
import eu.europeana.api.iiif.media.MappingTable;
import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.media.MediaTypeCatalog;
import eu.europeana.api.iiif.model.info.FulltextSummaryAnnoPage;
import eu.europeana.api.iiif.model.info.FulltextSummaryCanvas;
import eu.europeana.api.iiif.service.WebResourceSorter;
import eu.europeana.api.iiif.utils.EdmManifestUtils;
import eu.europeana.api.iiif.utils.LanguageMapUtils;
import eu.europeana.api.iiif.v3.model.*;
import eu.europeana.api.iiif.v3.model.content.*;
import eu.europeana.api.iiif.v3.model.fulltext.FullTextAnnotationPage;
import eu.europeana.api.record.model.WebResource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.jayway.jsonpath.Filter.filter;
import static com.jayway.jsonpath.Criteria.where;
import static eu.europeana.api.iiif.generator.ManifestGeneratorUtils.*;

import static eu.europeana.api.iiif.generator.ManifestGeneratorConstants.*;

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
    public static final MediaGeneratorVersion VERSION = MediaGeneratorVersion.V3;

    private ManifestSettings     settings;
    private MediaTypeCatalog       mediaTypes;

    private MediaGeneratorRegistry registry;

    public EdmManifestMappingV3(ManifestSettings settings
        , MediaTypeCatalog mediaTypes
        , MediaGeneratorRegistry registry) {
        this.settings = settings;
        this.mediaTypes = mediaTypes;
        this.registry = registry;
    }

    /**
     * Generates a IIIF v3 manifest based on the provided (parsed) json document
     * @param jsonDoc parsed json document
     * @return IIIF Manifest v3 object
     */
    public Manifest generateManifest(Object jsonDoc) {
        String europeanaId = EdmManifestUtils.getEuropeanaId(jsonDoc);
        String isShownBy = EdmManifestUtils.getValueFromDataProviderAggregation(jsonDoc, europeanaId, "edmIsShownBy");

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
        List<Canvas> items = getItems(settings, mediaTypes, europeanaId, isShownBy, jsonDoc);
        if (items != null && items.size() > 0) {
            manifest.getItems().addAll(items);
            manifest.setStart(getStartCanvasV3(manifest.getItems(), isShownBy));
        } else {
            LOG.debug("No Canvas generated for europeanaId {}", europeanaId);
        }
        manifest.getProvider().add(new Agent("https://www.europeana.eu/en/about-us",
                new Image(ManifestGeneratorConstants.EUROPEANA_LOGO_URL),
                new Text("https://www.europeana.eu",
                        new LanguageMap(LanguageMap.DEFAULT_METADATA_KEY, "Europeana"), "text/html")));
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
            String apHash = derivePageId(canvas.getStartCanvasAnnotation().getBody().getID());
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
            summaryAnnoPages.add(new FullTextAnnotationPage(sap.getID(), sap.getLanguage(), sap.getTextGranularity(), sap.getSource()));
        }
    }





    /**
     * Generates Service descriptions for the manifest
     */
    private Service getServiceDescriptionV3(ManifestSettings ms, String europeanaId) {
        Service service = new Service(ms.getContentSearchURL(europeanaId), null);
        service.setContext(ManifestGeneratorConstants.SEARCH_CONTEXT_VALUE);
        service.setProfile(ManifestGeneratorConstants.SEARCH_PROFILE_VALUE);
        return service;
    }

    /**
     * Reads the dcDate, dcFormat, dcRelation, dcType, dcLanguage and dcSource values from all proxies and puts them in a
     * LanguageMap with the appropriate label
     * @param jsonDoc parsed json document
     * @return
     */
    private List<LabelledValue> getMetaDataV3(Object jsonDoc) {
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
        return Collections.emptyList();
    }


    private void addMetaDataV3(List<LabelledValue> metaData, String fieldName, Object jsonDoc, String jsonPath) {
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


    private void processMetaDataValue(String value, List<String> newValues, Object jsonDoc,
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

    private LanguageMap getTimespanAgentConceptOrPlaceLabels(Object jsonDoc, String value) {
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

    private LanguageMap getEntityPrefLabels(Object jsonDoc, String entityName, String value) {
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
    private Text getRights(String europeanaId, Object jsonDoc) {
        String licenseText = EdmManifestUtils.getLicenseText(europeanaId, jsonDoc);
        if (StringUtils.isEmpty(licenseText)) {
            return null;
        }
        return new Text(licenseText, null, "text/html");
    }

    /**
     * Return array with the id of the thumbnail as defined in 'europeanaAggregation.edmPreview'
     * @param jsonDoc parsed json document
     * @return Image object, or null if no edmPreview was found
     */
    private Image getThumbnailImageV3(String europeanaId, Object jsonDoc) {
        String thumbnailId = EdmManifestUtils.getThumbnailId(europeanaId, jsonDoc);
        if (StringUtils.isEmpty(thumbnailId)) {
            return null;
        }
        return new Image(thumbnailId);
    }

    /**
     * Return attribution text as a String
     * We look for the webResource that corresponds to our edmIsShownBy and return the attribution snippet for that.
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @param isShownBy edmIsShownBy value
     * @param jsonDoc parsed json document
     * @return
     */
    private LabelledValue getAttributionV3Root(String europeanaId, String isShownBy, Object jsonDoc) {
        Filter isShownByFilter = filter(where(EdmManifestUtils.ABOUT).is(isShownBy));
        String[] attributions = JsonPath.parse(jsonDoc).
                read("$.object.aggregations[*].webResources[?]."+ EdmManifestUtils.HTML_ATTRIB_SNIPPET, String[].class, isShownByFilter);
        String attribution = (String) EdmManifestUtils.getFirstValueArray(EdmManifestUtils.HTML_ATTRIB_SNIPPET, europeanaId, attributions);
        return createRequiredStatementMap(attribution);
    }

    private LabelledValue createRequiredStatementMap(String attribution){
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
    private List<Dataset> getDataSetsV3(ManifestSettings settings, String europeanaId) {
        List<Dataset> result = new ArrayList<>(3);
        result.add(new Dataset(settings.getDatasetId(europeanaId, ".json-ld")
        		             , AcceptUtils.MEDIA_TYPE_JSONLD
        		             , EDM_SCHEMA_URL));
        result.add(new Dataset(settings.getDatasetId(europeanaId, ".json")
        		             , org.springframework.http.MediaType.APPLICATION_JSON_VALUE
        		             , EDM_SCHEMA_URL));
        result.add(new Dataset(settings.getDatasetId(europeanaId, ".rdf")
        		             , ManifestGeneratorConstants.MEDIA_TYPE_RDF
        		             , EDM_SCHEMA_URL));
        return result;
    }


    /**
     * @return the {@link  eu.europeana.api.iiif.v3.model.Canvas} that refers to edmIsShownBy, or else just the first Canvas
     */
    private Canvas getStartCanvasV3(List<Canvas> items, String edmIsShownBy) {
        if (items == null) {
            LOG.trace("Start canvas = null (no canvases present)");
            return null;
        }

        Canvas result = null;
        for (Canvas c : items) {
            String annotationBodyId = c.getStartCanvasAnnotation().getBody().getID();
            if (!StringUtils.isEmpty(edmIsShownBy) && edmIsShownBy.equals(annotationBodyId)) {
                result = c;
                LOG.trace("Start canvas = {} (matches with edmIsShownBy)", result.getPageNr());
                break;
            }
        }
        // nothing found, return first canvas
        if (result == null) {
            result = items.get(0);
            LOG.trace("Start canvas = {} (no match with edmIsShownBy, select first)", result.getPageNr());
        }

        return new Canvas(result.getID());
    }

    /**
     * Generates an ordered array of {@link Canvas}es referring to edmIsShownBy and hasView {@link WebResource}s.
     * For more information about the ordering @see {@link WebResourceSorter}
     * @param europeanaId
     * @param isShownBy
     * @param jsonDoc
     * @return array of Canvases
     */
    private List<Canvas> getItems(ManifestSettings settings, MediaTypeCatalog mediaTypes, String europeanaId, String isShownBy, Object jsonDoc) {
        
    	// generate canvases in a same order as the web resources
        List<WebResource> sortedResources = EdmManifestUtils.getSortedWebResources(europeanaId, isShownBy, jsonDoc);

        if (sortedResources.isEmpty()) {
            return null;
        }
        int order = 1;
        Map<String, Object>[] services = JsonPath.parse(jsonDoc).read("$.object[?(@.services)].services[*]", Map[].class);
        List<Canvas> canvases = new ArrayList<>(sortedResources.size());
        for (WebResource webResource: sortedResources) {
            Canvas canvas = getCanvasV3(settings, mediaTypes, europeanaId, order, webResource);
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
    private Canvas getCanvasV3(ManifestSettings settings,
                               MediaTypeCatalog mediaTypes,
                               String europeanaId,
                               int order,
                               WebResource webResource
                               ) {

        Canvas c = new Canvas(settings.getCanvasId(europeanaId, order));
        c.setLabel(new LanguageMap(null, "p. " + order));

        //special exception for euscreen which is not mimetype specific
        if ( EdmManifestUtils.isEuScreen(webResource.getId()) ) {
            MediaGenerator<Canvas> generator = registry.getGenerator(MediaGeneratorType.EUSCREEN,
                VERSION);
            return generator.generate(c,webResource);
        }

        // get the configured media type of the mimetype
        String mimeType = webResource.getMimeType();
        Optional<MediaType> media = mediaTypes.getMediaType(mimeType);
        if (media.isEmpty()) { return null; }

        webResource.setMediaType(media.get());



        MediaGenerator<Canvas> generator = registry.getGenerator(MappingTable.getGeneratorTypeV3(mimeType),
            VERSION);
        return generator.generate(c,webResource);
    }
}