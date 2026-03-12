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
import eu.europeana.api.iiif.model.info.FulltextSummaryCanvas;
import eu.europeana.api.iiif.utils.EdmManifestUtils;
import eu.europeana.api.iiif.utils.LanguageMapUtils;
import eu.europeana.api.iiif.v2.model.*;
import eu.europeana.api.iiif.v3.model.LanguageMap;
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
 * This class contains all the methods for mapping EDM record data to IIIF Manifest data for IIIF v2
 *
 * @author Patrick Ehlert
 * Created on 08-02-2018
 *
 * Updated By Srishti Singh to adjust with the new model class
 *
 */
// ignore sonarqube rule: we return null on purpose in this class
// ignore pmd rule:  we want to make a clear which objects are v2 and which v3
@SuppressWarnings({"squid:S1168", "pmd:UnnecessaryFullyQualifiedName"})
public final class EdmManifestMappingV2 implements ManifestGenerator<Manifest> {

    private static final Logger LOG = LogManager.getLogger(EdmManifestMappingV2.class);

    public static final MediaGeneratorVersion VERSION = MediaGeneratorVersion.V2;

    private ManifestSettings    settings;
    private MediaTypeCatalog    mediaTypes;
    private MediaGeneratorRegistry registry;

    public EdmManifestMappingV2(ManifestSettings settings
                              , MediaTypeCatalog mediaTypes, MediaGeneratorRegistry registry) {
        this.settings        = settings;
        this.mediaTypes      = mediaTypes;
        this.registry        = registry;
    }

    /**
     * Generates a IIIF v2 manifest based on the provided (parsed) json document
     * @param jsonDoc parsed json document
     * @return IIIF Manifest v2 object
     */
    public Manifest generateManifest(Object jsonDoc) {
        String europeanaId = EdmManifestUtils.getEuropeanaId(jsonDoc);
        String isShownBy = EdmManifestUtils.getValueFromDataProviderAggregation(jsonDoc, europeanaId, "edmIsShownBy");
        Manifest manifest = new Manifest(settings.getManifestId(europeanaId));
        manifest.getServices().add(getServiceDescriptionV2(settings, europeanaId));
        // EA-3325
//        manifest.setWithin(getWithinV2(jsonDoc));
        manifest.setLabel(getLabelsV2(jsonDoc));
        manifest.getDescription().addAll(getDescriptionV2(jsonDoc));
        manifest.getMetadata().addAll(getMetaDataV2(jsonDoc));
        manifest.setThumbnail(getThumbnailImageV2(europeanaId, jsonDoc));
        manifest.setNavDate(EdmManifestUtils.getNavDate(europeanaId, jsonDoc));
        manifest.setAttribution(getAttributionV2(europeanaId, isShownBy, jsonDoc));
        manifest.setLicense(getLicense(europeanaId, jsonDoc));
        manifest.setLogo(new Image(ManifestGeneratorConstants.EUROPEANA_LOGO_URL));
        manifest.setSeeAlso(getDataSetsV2(settings, europeanaId));
        List<Sequence> sequences = getSequencesV2(settings, mediaTypes, europeanaId, isShownBy, jsonDoc);
        if (sequences != null) {
            manifest.setSequences(sequences);

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

        if (manifest.getSequences() == null || manifest.getSequences().isEmpty()) {
            LOG.debug("Not checking for fulltext because record doesn't have any sequences");
            return;
        }

        // there is always only 1 sequence
        Sequence sequence = manifest.getSequences().get(0);
        if ( summary == null ) { return; }
            
        // loop over canvases to add full-text link(s) to all
        for (Canvas canvas : sequence.getCanvases()) {
            // we need to generate the same annopageId hash based on imageId
            String apHash = derivePageId(canvas.getStartImageAnnotation().getBody().getID());
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
    private Service getServiceDescriptionV2(ManifestSettings settings, String europeanaId) {
        Service service = new Service(settings.getContentSearchURL(europeanaId));
        service.setContext(ManifestGeneratorConstants.SEARCH_CONTEXT_VALUE);
        service.setProfile(ManifestGeneratorConstants.SEARCH_PROFILE_VALUE);
        return service;
    }

    /**
     * Return first proxy.dctermsIsPartOf that starts with "http://data.theeuropeanlibrary.org/ that we can find
     * @param jsonDoc parsed json document
     * @return
     */

    /**
     * We first check all proxies for a title. If there are no titles, then we check the description fields
     * @param jsonDoc parsed json document
     * @return array of LanguageObject
     */
    private LanguageValue getLabelsV2(Object jsonDoc) {
        // we read everything in as LanguageMap[] because that best matches the EDM implementation, then we convert to LanguageObjects[]
        LanguageMap labelsV3 = getLabels(jsonDoc);
        if (labelsV3 == null) {
            return null;
        }
        return LanguageMapUtils.langMapToObject(labelsV3);
    }

    /**
     * Returns the values from the proxy.dcDescription fields, but only if they aren't used as a label yet.
     * @param jsonDoc parsed json document
     * @return
     */
    private List<LanguageValue> getDescriptionV2(Object jsonDoc) {
        // we read everything in as LanguageMap[] because that best matches the EDM implementation, then we convert to LanguageObjects[]
        LanguageMap descriptionsV3 = getDescription(jsonDoc);
        if (descriptionsV3 == null) {
            return Collections.emptyList();
        }
        return LanguageMapUtils.langMapToObjects(getDescription(jsonDoc));
    }

    /**
     * Reads the dcDate, dcFormat, dcRelation, dcType, dcLanguage and dcSource values from all proxies and puts them in a
     * map with the appropriate label
     * @param jsonDoc parsed json document
     * @return
     */
    private List<LabelledValue> getMetaDataV2(Object jsonDoc) {
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
            return Collections.emptyList();
        }
        return result;
    }

    /**
     * We read in metadata as a LanguageMap[], but we need to convert it to Map consisting of labels and List<LanguageObjects>
     * Also if the key is 'def' we should leave that out (for v2)
     */
    private void addMetaDataV2(Map<String, List<LanguageValue>> metaData, LanguageMap[] dataToAdd, String fieldName) {
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

    private void processMetaDataField(String fieldName,  Map<String, List<LanguageValue>> metaData, String language, String value) {
        List<LanguageValue> langObjects;
        if (!metaData.containsKey(fieldName)) {
            langObjects = new ArrayList<>();
            metaData.put(fieldName, langObjects);
        } else {
            langObjects = metaData.get(fieldName);
        }
        langObjects.add(new LanguageValue(value, language));
    }

    /**
     * Return an with the id of the thumbnail as defined in 'europeanaAggregation.edmPreview'
     * @param jsonDoc parsed json document
     * @return Image object, or null if no edmPreview was found
     */
    private Image getThumbnailImageV2(String europeanaId, Object jsonDoc) {
        String thumbnailId = EdmManifestUtils.getThumbnailId(europeanaId, jsonDoc);
        if (StringUtils.isEmpty(thumbnailId)) {
            return null;
        }
        return new Image(EdmManifestUtils.getThumbnailId(europeanaId, jsonDoc));
    }

    /**
     * Return attribution text as a String
     * We look for the webResource that corresponds to our edmIsShownBy and return the 'textAttributionSnippet' for that.
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @param isShownBy edmIsShownBy value
     * @param jsonDoc parsed json document
     * @return
     */
    private String getAttributionV2(String europeanaId, String isShownBy, Object jsonDoc) {
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
    private List<Dataset> getDataSetsV2(ManifestSettings settings, String europeanaId) {
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
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @param isShownBy
     * @param jsonDoc parsed json document
     * @return
     */
    private List<Sequence> getSequencesV2(ManifestSettings settings, MediaTypeCatalog mediaTypes,String europeanaId, String isShownBy, Object jsonDoc) {
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
            sequence.setLabel(new LanguageValue("Current Page Order"));
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
    private String getLicense(String europeanaId, Object jsonDoc) {
        return EdmManifestUtils.getLicenseText(europeanaId, jsonDoc);
    }

    /**
     * Generates a new canvas, but note that we do not fill the otherContent (Full-Text) here. That is done later
     */
    private Canvas getCanvasV2(ManifestSettings settings,
                               MediaTypeCatalog mediaTypes,
                               String europeanaId,
                               int order,
                               WebResource webResource,
                               Map<String, Object>[] services) {

    	Canvas c = new Canvas(settings.getCanvasId(europeanaId, order));
        c.setLabel(new LanguageValue("p. "+order));

        if (EdmManifestUtils.isEuScreen(webResource.getId())) {
            MediaGenerator<Canvas> generator = registry.getGenerator(MediaGeneratorType.EUSCREEN,
                VERSION);
            return generator.generate(c,webResource);
            //return MediaGeneratorType.euscreen.generate(c, webResource);
        }

        // get the configured media type of the mimetype
        String mimeType = webResource.getMimeType();
        Optional<MediaType> media = mediaTypes.getMediaType(mimeType);
        if (media.isEmpty()) { return null; }

        webResource.setMediaType(media.get());

        MediaGenerator<Canvas> generator = registry.getGenerator(MappingTable.getGeneratorTypeV2(mimeType),
            VERSION);
        return generator.generate(c,webResource);


    }
}