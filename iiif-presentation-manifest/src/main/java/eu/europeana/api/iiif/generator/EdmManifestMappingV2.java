package eu.europeana.api.iiif.generator;

import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.definitions.iiif.AcceptUtils;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.media.MediaTypes;
import eu.europeana.api.iiif.model.ManifestDefinitions;
import eu.europeana.api.iiif.model.WebResource;
import eu.europeana.api.iiif.model.info.FulltextSummaryCanvas;
import eu.europeana.api.iiif.service.FulltextService;
import eu.europeana.api.iiif.utils.EdmManifestUtils;
import eu.europeana.api.iiif.utils.GenerateUtils;
import eu.europeana.api.iiif.utils.LanguageMapUtils;
import eu.europeana.api.iiif.v2.model.*;
import eu.europeana.api.iiif.v3.model.LanguageMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.jayway.jsonpath.Filter.filter;
import static com.jayway.jsonpath.Criteria.where;
import static eu.europeana.api.iiif.model.ManifestDefinitions.CANVAS_THUMBNAIL_POSTFIX;

/**
 * This class contains all the methods for mapping EDM record data to IIIF Manifest data for IIIF v2
 *
 * @author Patrick Ehlert
 * Created on 08-02-2018
 *
 * Updated By Srishti Singh to adjust with the new model class
 * TODO - look into todo comments in the class
 *
 */
// ignore sonarqube rule: we return null on purpose in this class
// ignore pmd rule:  we want to make a clear which objects are v2 and which v3
@SuppressWarnings({"squid:S1168", "pmd:UnnecessaryFullyQualifiedName"})
public final class EdmManifestMappingV2 implements ManifestGenerator<Manifest> {

    private static final Logger LOG = LogManager.getLogger(EdmManifestMappingV2.class);

    //@TODO: Why is this static?
    private static String THUMBNAIL_API_URL;

    private ManifestSettings     settings;
    private MediaTypes       mediaTypes;

    public EdmManifestMappingV2(ManifestSettings settings
                              , MediaTypes mediaTypes) {
        this.settings        = settings;
        this.mediaTypes      = mediaTypes;
    }

    /**
     * Generates a IIIF v2 manifest based on the provided (parsed) json document
     * @param jsonDoc parsed json document
     * @return IIIF Manifest v2 object
     */
    public Manifest generateManifest(Object jsonDoc) {
        THUMBNAIL_API_URL = settings.getThumbnailApiUrl();
        String europeanaId = EdmManifestUtils.getEuropeanaId(jsonDoc);
        String isShownBy = EdmManifestUtils.getValueFromDataProviderAggregation(jsonDoc, europeanaId, "edmIsShownBy");
        Manifest manifest = new Manifest(settings.getManifestId(europeanaId));
        manifest.getServices().add(getServiceDescriptionV2(settings, europeanaId));
        // EA-3325
//        manifest.setWithin(getWithinV2(jsonDoc));
        manifest.setLabel(getLabelsV2(jsonDoc));
        manifest.setDescription(getDescriptionV2(jsonDoc));
        manifest.getMetadata().addAll(getMetaDataV2(jsonDoc));
        manifest.setThumbnail(getThumbnailImageV2(europeanaId, jsonDoc));
        manifest.setNavDate(EdmManifestUtils.getNavDate(europeanaId, jsonDoc));
        manifest.setAttribution(getAttributionV2(europeanaId, isShownBy, jsonDoc));
        manifest.setLicense(getLicense(europeanaId, jsonDoc));
        manifest.setSeeAlso(getDataSetsV2(settings, europeanaId));
        List<Sequence> sequences = getSequencesV2(settings, mediaTypes, europeanaId, isShownBy, jsonDoc);
        if (sequences != null) {
            manifest.setSequences(sequences);
            // TODO find missing fields
            //manifest.setStartCanvasPageNr(getStartCanvasV2(manifest.getSequences()[0].getCanvases(), isShownBy));
        } else {
            LOG.debug("No Canvas generated for europeanaId {}", europeanaId);
        }
        return manifest;
    }

    /**
     * We generate all full text links in one place, so we can raise a timeout if retrieving the necessary
     * data for all full texts is too slow.
     * From EA-2604 on, originalLanguage is available on the FulltextSummaryCanvas and copied to the AnnotationBody if
     * motivation = 'sc:painting'
     */
    public void fillWithFullText(Manifest manifest
                               , Map<String, FulltextSummaryCanvas> summary) {

        if (manifest.getSequences() == null || manifest.getSequences().size() == 0) {
            LOG.debug("Not checking for fulltext because record doesn't have any sequences");
            return;
        }

        // there is always only 1 sequence
        Sequence sequence = manifest.getSequences().get(0);
        if ( summary == null ) { return; }
            
        // loop over canvases to add full-text link(s) to all
        for (Canvas canvas : sequence.getCanvases()) {
            // we need to generate the same annopageId hash based on imageId
            String apHash = GenerateUtils.derivePageId(canvas.getStartImageAnnotation().getBody().getID());
            FulltextSummaryCanvas ftCanvas = summary.get(apHash);
            if (ftCanvas == null) {
                // This warning can be logged for empty pages that do not have a fulltext, but if we get a lot
                // then Record API and Fulltext API are not in sync (or the hashing algorithm changed)
                LOG.warn("Possible inconsistent data. No fulltext annopage found for record {} page {}. Generated hash = {}",
                        manifest.getID(), canvas.getID(), apHash);
            } else {
                addFulltextLinkToCanvasV2(canvas, ftCanvas);
            }
        }

        /*
        Map<String, FulltextSummaryCanvas> summaryCanvasMap;
        if (manifest.getSequences() != null && manifest.getSequences().size() > 0) {
            // there is always only 1 sequence
            Sequence sequence = manifest.getSequences().get(0);
            // Get all the available AnnoPages incl translations from the summary endpoint of Fulltext
            String fullTextSummaryUrl = generateFullTextSummaryUrl(manifest.getEuropeanaId(), fullTextApi);
            summaryCanvasMap = fulltextService.getFulltextSummary(fullTextSummaryUrl);
            if (null != summaryCanvasMap) {
                // loop over canvases to add full-text link(s) to all
                for (Canvas canvas : sequence.getCanvases()) {
                    // we need to generate the same annopageId hash based on imageId
                    String apHash = GenerateUtils.derivePageId(canvas.getStartImageAnnotation().getBody().getID());
                    FulltextSummaryCanvas ftCanvas = summaryCanvasMap.get(apHash);
                    if (ftCanvas == null) {
                        // This warning can be logged for empty pages that do not have a fulltext, but if we get a lot
                        // then Record API and Fulltext API are not in sync (or the hashing algorithm changed)
                        LOG.warn("Possible inconsistent data. No fulltext annopage found for record {} page {}. Generated hash = {}",
                                manifest.getEuropeanaId(), canvas.getPageNr(), apHash);
                    } else {
                        addFulltextLinkToCanvasV2(canvas, ftCanvas);
                    }
                }
            }
        } else {
            LOG.debug("Not checking for fulltext because record doesn't have any sequences");
        }
        */
    }

    /**
     * Generates a url to a full text resource
     *
     * @param fullTextApiUrl optional, if not specified then the default Full-Text API specified in .properties is used
     * @param europeanaId    identifier to include in the path
     */
    private String generateFullTextSummaryUrl(String europeanaId, URL fullTextApiUrl) {
        if (fullTextApiUrl == null) {
            return settings.getFullTextApiBaseUrl() + ManifestDefinitions.getFulltextSummaryPath(europeanaId);
        } else {
            return fullTextApiUrl + ManifestDefinitions.getFulltextSummaryPath(europeanaId);
        }
    }

    private void addFulltextLinkToCanvasV2(Canvas canvas, FulltextSummaryCanvas summaryCanvas) {
        canvas.getOtherContent().addAll(summaryCanvas.getAnnoPageIDs());
        for (eu.europeana.api.iiif.v2.model.Annotation ann : canvas.getImages()) {
            // original language will be null for translation
            if (StringUtils.equalsAnyIgnoreCase(ann.getMotivation(), "sc:painting") && summaryCanvas.getOriginalLanguage() != null) {
                ann.getBody().setLanguage(summaryCanvas.getOriginalLanguage());
            }
        }
    }

    /**
     * Generates a service description for the manifest
     */
    private static Service getServiceDescriptionV2(ManifestSettings settings, String europeanaId) {
        // TODO need to know the type value
        Service service = new Service(settings.getContentSearchURL(europeanaId), null);
        service.setContext(ManifestDefinitions.SEARCH_CONTEXT_VALUE);
        service.setProfile(ManifestDefinitions.SEARCH_PROFILE_VALUE);
        return service;
    }

    /**
     * Return first proxy.dctermsIsPartOf that starts with "http://data.theeuropeanlibrary.org/ that we can find
     * @param jsonDoc parsed json document
     * @return
     */

    // EA-3325
    static String getWithinV2(Object jsonDoc) {
        List<String> result = EdmManifestUtils.getEuropeanaLibraryCollections(jsonDoc);
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    /**
     * We first check all proxies for a title. If there are no titles, then we check the description fields
     * @param jsonDoc parsed json document
     * @return array of LanguageObject
     */
    static LanguageValue getLabelsV2(Object jsonDoc) {
        // we read everything in as LanguageMap[] because that best matches the EDM implementation, then we convert to LanguageObjects[]
        LanguageMap labelsV3 = EdmManifestMappingV3.getLabels(jsonDoc);
        if (labelsV3 == null) {
            return null;
        }
        return LanguageMapUtils.langMapToObjects(labelsV3);
    }

    /**
     * Returns the values from the proxy.dcDescription fields, but only if they aren't used as a label yet.
     * @param jsonDoc parsed json document
     * @return
     */
    static LanguageValue getDescriptionV2(Object jsonDoc) {
        // we read everything in as LanguageMap[] because that best matches the EDM implementation, then we convert to LanguageObjects[]
        LanguageMap descriptionsV3 = EdmManifestMappingV3.getDescription(jsonDoc);
        if (descriptionsV3 == null) {
            return null;
        }
        return LanguageMapUtils.langMapToObjects(EdmManifestMappingV3.getDescription(jsonDoc));
    }

    /**
     * Reads the dcDate, dcFormat, dcRelation, dcType, dcLanguage and dcSource values from all proxies and puts them in a
     * map with the appropriate label
     * @param jsonDoc parsed json document
     * @return
     */
    static List<LabelledValue> getMetaDataV2(Object jsonDoc) {
        // fieldname , list of values
        Map<String, List<LanguageValue>> data = new LinkedHashMap<>();
        addMetaDataV2(data, JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcDate", LanguageMap[].class), "date");
        addMetaDataV2(data, JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcFormat", LanguageMap[].class), "format");
        addMetaDataV2(data, JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcRelation", LanguageMap[].class), "relation");
        addMetaDataV2(data, JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcType", LanguageMap[].class), "type");
        addMetaDataV2(data, JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcLanguage", LanguageMap[].class), "language");
        addMetaDataV2(data, JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcSource", LanguageMap[].class), "source");

        List<LabelledValue> result = new ArrayList<>(data.entrySet().size());
        for (Map.Entry<String, List<LanguageValue>> entry : data.entrySet()) {
            String label = entry.getKey();
            List<LanguageValue> values = entry.getValue();
            result.add(new LabelledValue(label, values));
        }

        if (result.isEmpty()) {
            return null;
        }
        return result;
    }

    /**
     * We read in metadata as a LanguageMap[], but we need to convert it to Map consisting of labels and List<LanguageObjects>
     * Also if the key is 'def' we should leave that out (for v2)
     */
    private static void addMetaDataV2(Map<String, List<LanguageValue>> metaData, LanguageMap[] dataToAdd, String fieldName) {
        for (LanguageMap map : dataToAdd) {
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String language = entry.getKey();
                List<String> values = entry.getValue();
                for (String value: values) {
                    processMetaDataField(fieldName, metaData, language, value);
                }
            }
        }
    }

    private static void processMetaDataField(String fieldName,  Map<String, List<LanguageValue>> metaData, String language, String value) {
        List<LanguageValue> langObjects;
        if (!metaData.containsKey(fieldName)) {
            langObjects = new ArrayList<>();
            metaData.put(fieldName, langObjects);
        } else {
            langObjects = metaData.get(fieldName);
        }
        langObjects.add(new LanguageValue(language, value));
    }

    /**
     * Return an with the id of the thumbnail as defined in 'europeanaAggregation.edmPreview'
     * @param jsonDoc parsed json document
     * @return Image object, or null if no edmPreview was found
     */
    static Image getThumbnailImageV2(String europeanaId, Object jsonDoc) {
        String thumbnailId = EdmManifestUtils.getThumbnailId(europeanaId, jsonDoc);
        if (StringUtils.isEmpty(thumbnailId)) {
            return null;
        }
        return new Image(EdmManifestUtils.getThumbnailId(europeanaId, jsonDoc));
    }

    /**
     * EA-3325 Return array with the id of the canvas-specific thumbnail created from the Webresource id
     * @param webresourceId hasview image ID
     * @return Image object, or null if either provided String was null
     */
    static Image getCanvasThumbnailImageV2(String webresourceId, String ThumbnailApiUrl) {
        if (StringUtils.isAnyEmpty(ThumbnailApiUrl, webresourceId)) {
            return null;
        }
        return new Image(ThumbnailApiUrl + webresourceId + CANVAS_THUMBNAIL_POSTFIX);
    }

    /**
     * Return attribution text as a String
     * We look for the webResource that corresponds to our edmIsShownBy and return the 'textAttributionSnippet' for that.
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @param isShownBy edmIsShownBy value
     * @param jsonDoc parsed json document
     * @return
     */
    static String getAttributionV2(String europeanaId, String isShownBy, Object jsonDoc) {
        Filter isShownByFilter = filter(where(EdmManifestUtils.ABOUT).is(isShownBy));
        String[] attributions = JsonPath.parse(jsonDoc).
                read("$.object.aggregations[*].webResources[?]." + EdmManifestUtils.TEXT_ATTRIB_SNIPPET, String[].class, isShownByFilter);
        return (String) EdmManifestUtils.getFirstValueArray(EdmManifestUtils.TEXT_ATTRIB_SNIPPET, europeanaId, attributions);
    }


    /**
     * Generates 3 datasets with the appropriate ID and format (one for rdf/xml, one for json and one for json-ld)
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @return array of 3 datasets
     */
    static List<Dataset> getDataSetsV2(ManifestSettings settings, String europeanaId) {
        List<Dataset> result = new ArrayList<>(3);
        result.add(new Dataset(settings.getDatasetId(europeanaId, ".json-ld"), AcceptUtils.MEDIA_TYPE_JSONLD));
        result.add(new Dataset(settings.getDatasetId(europeanaId, ".json"), org.springframework.http.MediaType.APPLICATION_JSON_VALUE));
        result.add(new Dataset(settings.getDatasetId(europeanaId, ".rdf"), ManifestDefinitions.MEDIA_TYPE_RDF));
        return result;
    }

    /**
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @param isShownBy
     * @param jsonDoc parsed json document
     * @return
     */
    static List<Sequence> getSequencesV2(ManifestSettings settings, MediaTypes mediaTypes,String europeanaId, String isShownBy, Object jsonDoc) {
        // generate canvases in a same order as the web resources
        List<WebResource> sortedResources = EdmManifestUtils.getSortedWebResources(europeanaId, isShownBy, jsonDoc);
        if (sortedResources.isEmpty()) {
            return null;
        }
        int order = 1;
        Map<String, Object>[] services = JsonPath.parse(jsonDoc).read("$.object[?(@.services)].services[*]", Map[].class);
        List<Canvas> canvases = new ArrayList<>(sortedResources.size());
        for (WebResource webResource: sortedResources) {
            Canvas canvas = getCanvasV2(settings, mediaTypes, europeanaId, order, webResource, services);
            // for non supported media types we do not create any canvas. Case-4 of media type handling : See-EA-3413
            if (canvas != null) {
                canvases.add(canvas);
                order++;
            }
        }
        // if there are canvas generated add the sequence
        if (!canvases.isEmpty()) {
            // there should be only 1 sequence, so sequence number is always 1
            List<Sequence> result = new ArrayList<>(1);
            Sequence sequence = new Sequence();
            sequence.setStartCanvas(settings.getCanvasId(europeanaId, 1));
            sequence.setCanvases(canvases);
            result.add(sequence);
            return result;
        }
        return null;
    }


    /**
     * Return the first license description we find in any 'aggregation.edmRights' field. Note that we first try the europeanaAggregation and if
     * that doesn't contain an edmRights, we check the other aggregations
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @param jsonDoc parsed json document
     * @return String containing rights information
     */
    static String getLicense(String europeanaId, Object jsonDoc) {
        return EdmManifestUtils.getLicenseText(europeanaId, jsonDoc);
    }

    /**
     * @return Integer containing the page number of the canvas that refers to the edmIsShownBy, or else just the first
     *  Canvas. Null if there are no canvases
     *  TODO see usage of this in the new model there is no field
     *   private Integer startCanvasPageNr; // for internal use only, similar to 'start' field in v3
     */
//    static Integer getStartCanvasV2(Canvas[] items, String edmIsShownBy) {
//        if (items == null) {
//            LOG.trace("Start canvas = null (no canvases present)");
//            return null;
//        }
//
//        Canvas result = null;
//        for (Canvas c : items) {
//            String annotationBodyId = c.getStartImageAnnotation().getResource().getId();
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
//        return result.getPageNr();
//    }

    /**
     * Generates a new canvas, but note that we do not fill the otherContent (Full-Text) here. That is done later
     */
    private static Canvas getCanvasV2(ManifestSettings settings,
                                                                 MediaTypes mediaTypes,
                                                                 String europeanaId,
                                                                 int order,
                                                                 WebResource webResource,
                                                                 Map<String, Object>[] services) {
        //Canvas c = new Canvas(settings.getCanvasId(europeanaId, order), order);
        Canvas c = new Canvas(settings.getCanvasId(europeanaId, order));

        c.setLabel(new LanguageValue("p. "+order));

        Object obj = webResource.get(EdmManifestUtils.EBUCORE_HEIGHT);
        if (obj instanceof Integer){
            c.setHeight((Integer) obj);
        }

        obj = webResource.get(EdmManifestUtils.EBUCORE_WIDTH);
        if (obj instanceof Integer){
            c.setWidth((Integer) obj);
        }

        String attributionText = (String) webResource.get(EdmManifestUtils.TEXT_ATTRIB_SNIPPET);
        if (!StringUtils.isEmpty(attributionText)){
            c.setAttribution(attributionText);
        }

        //EA-3325: check if the webResource has a "svcsHasService"; if not, add a thumbnail
        if (Objects.isNull(webResource.get(EdmManifestUtils.SVCS_HAS_SERVICE))){
            c.setThumbnail(getCanvasThumbnailImageV2(URLEncoder.encode(webResource.getId(), StandardCharsets.UTF_8), settings.getThumbnailApiUrl()));
        }

        LinkedHashMap<String, ArrayList<String>> license = (LinkedHashMap<String, ArrayList<String>>) webResource.get(EdmManifestUtils.WEB_RESOURCE_EDM_RIGHTS);
        if (license != null && !license.values().isEmpty()) {
            c.setLicense(license.values().iterator().next().get(0));
        }

        // canvas has 1 annotation (image field)
        Annotation annotation = new Annotation(c.getID());
        annotation.setOn(c.getID());
        c.setImages(Collections.singletonList(annotation));
//        c.setImages(new eu.europeana.iiif.model.v2.Annotation[1]);
//        c.getImages()[0] = new eu.europeana.iiif.model.v2.Annotation();
//        c.getImages()[0].setOn(c.getId());

        // MEDIA TYPE HANDLING ....

        // Fetch the mime type from the web resource
        String ebuCoreMimeType = (String) webResource.get(EdmManifestUtils.EBUCORE_HAS_MIMETYPE);
        MediaType mediaType = null;

        // get the configured media type of the mimetype
        Optional<MediaType> media = mediaTypes.getMediaType(ebuCoreMimeType);
        if (media.isPresent()) {
            mediaType = media.get();
        }

        // ignored cases CASE 4 for version 2
        if (mediaType == null || ifSupportedMediaTypeIsVideoOrSound(mediaType)) {
            LOG.debug("No canvas added for webresource {} as the media type - {} is invalid or not supported.",
                    webResource.get(EdmManifestUtils.ABOUT),
                    ebuCoreMimeType);
            return null;
        }

        // Now create the annotation body based on the media type (annotation has 1 annotationBody)
        AnnotationBody annoBody = new AnnotationBody((String) webResource.get(EdmManifestUtils.ABOUT));

        // EA- 3436 add technical metadata for case 2 and 3
        // case 2
         if (mediaType.isBrowserSupported() && !mediaType.isVideoOrSound()) {
             annoBody.setFormat(mediaType.getMimeType());
             addTechnicalMetadata(c, annoBody);
         }

         // case 3
        if (mediaType.isRendered() && !mediaType.isVideoOrSound()) {
            if(c.getThumbnail()!=null) {
                annoBody = new AnnotationBody(c.getThumbnail().getID());
            }
            // update height and width
            setHeightWidthForRendered(c);
            // set rendering
            Image renderingImage = new Image((String) webResource.get(EdmManifestUtils.ABOUT));
            renderingImage.setFormat(mediaType.getMimeType());
            renderingImage.setLabel(new LanguageValue(mediaType.getLabel()));
            c.getRendering().add(renderingImage);
            //c.setRendering(new Rendering((String) webResource.get(EdmManifestUtils.ABOUT), mediaType.getMimeType(), mediaType.getLabel()));
            addTechnicalMetadata(c, annoBody);
        }

        // body can have a service
        String serviceId = EdmManifestUtils.getServiceId(webResource, europeanaId);
        if (serviceId != null) {
            Service service = new Service(serviceId, ManifestDefinitions.IMAGE_CONTEXT_VALUE);
            service.setProfile(EdmManifestUtils.lookupServiceDoapImplements(services, serviceId, europeanaId));
            annoBody.setService(service);
        }
        c.getImages().get(0).setBody(annoBody);
        return c;
    }

    /**
     * If media type is present and
     * is either browser or rendered supported but has type video or sound
     * return true
     *
     * @param mediaType
     * @return
     */
    private static boolean ifSupportedMediaTypeIsVideoOrSound(MediaType mediaType) {
        return mediaType != null && ((mediaType.isRendered() || mediaType.isBrowserSupported()) && mediaType.isVideoOrSound());
    }

    /**
     * Adds the technical metadata in the annotation body of the canvas
     * @param canvas
     * @param body
     */
    private static void addTechnicalMetadata(Canvas canvas, AnnotationBody body) {
        body.setHeight(canvas.getHeight());
        body.setWidth(canvas.getWidth());
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
}
