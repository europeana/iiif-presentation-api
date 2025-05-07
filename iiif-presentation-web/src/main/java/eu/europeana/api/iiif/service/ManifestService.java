package eu.europeana.api.iiif.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.config.IIIfSettings;
import eu.europeana.api.iiif.config.MediaTypes;
import eu.europeana.api.iiif.exceptions.RecordParseException;
import eu.europeana.api.iiif.model.ManifestDefinitions;
import eu.europeana.api.iiif.model.info.FulltextSummaryAnnoPage;
import eu.europeana.api.iiif.model.info.FulltextSummaryCanvas;
import eu.europeana.api.iiif.service.manifest.EdmManifestMappingV2;
import eu.europeana.api.iiif.service.manifest.EdmManifestMappingV3;
import eu.europeana.api.iiif.utils.GenerateUtils;
import eu.europeana.api.iiif.v2.model.Canvas;
import eu.europeana.api.iiif.v2.model.Manifest;
import eu.europeana.api.iiif.v2.model.Sequence;
import eu.europeana.api.iiif.v3.model.AnnotationPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.*;


/**
 * Service that loads record data, uses that to generate a Manifest object and serializes the manifest in JSON-LD
 *
 * @author Patrick Ehlert
 * Created on 06-12-2017
 */
@Service
public class ManifestService {

    private static final Logger LOG = LogManager.getLogger(ManifestService.class);

    private final IIIfSettings settings;
    private final MediaTypes mediaTypes;
    private final FulltextService  fulltextService;
    private final RecordService recordService;
    private final ObjectMapper mapper;


    /**
     * Creates an instance of the ManifestService bean with provided settings
     *  @param settings   read from properties file
     * @param mediaTypes
     * @param fulltextService
     * @param recordService
     */
    public ManifestService(IIIfSettings settings, MediaTypes mediaTypes, FulltextService fulltextService, RecordService recordService) {
        this.settings = settings;
        this.mediaTypes = mediaTypes;
        mapper = new ObjectMapper();

        // configure jsonpath: we use jsonpath in combination with Jackson because that makes it easier to know what
        // type of objects are returned (see also https://stackoverflow.com/a/40963445)
        com.jayway.jsonpath.Configuration.setDefaults(new com.jayway.jsonpath.Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                if (Boolean.TRUE.equals(settings.getSuppressParseException())) {
                    // we want to be fault tolerant in production, but for testing we may want to disable this option
                    return EnumSet.of(Option.SUPPRESS_EXCEPTIONS);
                } else {
                    return EnumSet.noneOf(Option.class);
                }
            }
        });

        this.fulltextService = fulltextService;
        this.recordService = recordService;
    }


    /**
     * Return record information in Json format using the Record API base URL defined in the iiif.properties
     *
     * @param recordId Europeana record id in the form of "/datasetid/recordid" (so with leading slash and without trailing slash)
     * @param wsKey    api key to send to record API
     * @return record information in json format
     * @throws EuropeanaApiException (IllegalArgumentException if a parameter has an illegal format,
     *                               InvalidApiKeyException if the provide key is not valid,
     *                               RecordNotFoundException if there was a 404,
     *                               RecordRetrieveException on all other problems)
     */
    public String getRecordJson(URL recordApiUrl, String recordId, String wsKey) throws EuropeanaApiException {
        if (recordApiUrl != null) {
            return recordService.getRecordJson(recordApiUrl + settings.getRecordApiPath(), recordId, wsKey);
        }
        return recordService.getRecordJson(settings.getRecordApiEndpoint(), recordId, wsKey);
    }

    /**
     * Generates a manifest object for IIIF v2 filled with data that is extracted from the provided JSON
     *
     * @param json record data in JSON format
     * @return Manifest v2 object
     */
    public Manifest generateManifestV2(String json) {
        long start = System.currentTimeMillis();
        Object document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(json);
        Manifest result = EdmManifestMappingV2.getManifestV2(settings, mediaTypes, document);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated in {} ms", System.currentTimeMillis() - start);
        }
        return result;
    }

    /**
     * Generates a manifest object for IIIF v2 filled with data that is extracted from the provided JSON.
     * It checks for each canvas if a full text exists; and if so, adds the link to its annotation page
     *
     * @param json        record data in JSON format
     * @param fullTextApi optional, if provided this url will be used to check if a full text is available or not
     * @return Manifest v2 object
     */
    public Manifest generateManifestV2(String json, URL fullTextApi) {
        long start = System.currentTimeMillis();
        Object document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(json);
        Manifest result = EdmManifestMappingV2.getManifestV2(settings, mediaTypes, document);

        try {
            fillInFullTextLinksV2(result, fullTextApi);
        } catch (EuropeanaApiException ie) {
            LOG.error("Error adding full text links", ie);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated in {} ms", System.currentTimeMillis() - start);
        }
        return result;
    }

    /**
     * Generates a manifest object for IIIF v3 filled with data that is extracted from the provided JSON
     *
     * @param json record data in JSON format
     * @return Manifest v3 object
     */
    public eu.europeana.api.iiif.v3.model.Manifest generateManifestV3(String json) {
        long start = System.currentTimeMillis();
        Object document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(json);
        eu.europeana.api.iiif.v3.model.Manifest result = EdmManifestMappingV3.getManifestV3(settings, mediaTypes, document);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated in {} ms ", System.currentTimeMillis() - start);
        }
        return result;
    }

    /**
     * Generates a manifest object for IIIF v3 filled with data that is extracted from the provided JSON
     * It checks for each canvas if a full text exists; and if so, adds the link to its annotation page
     *
     * @param json        record data in JSON format
     * @param fullTextApi optional, if provided this url will be used to check if a full text is available or not
     * @return Manifest v3 object
     */
    public eu.europeana.api.iiif.v3.model.Manifest generateManifestV3(String json, URL fullTextApi) {
        long start = System.currentTimeMillis();
        Object document = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(json);
        eu.europeana.api.iiif.v3.model.Manifest result = EdmManifestMappingV3.getManifestV3(settings, mediaTypes, document);
        try {
            fillInFullTextLinksV3(result, fullTextApi);
        } catch (EuropeanaApiException ie) {
            LOG.error("Error adding full text links", ie);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated in {} ms ", System.currentTimeMillis() - start);
        }
        return result;
    }

    /**
     * We generate all full text links in one place, so we can raise a timeout if retrieving the necessary
     * data for all full texts is too slow.
     * From EA-2604 on, originalLanguage is available on the FulltextSummaryCanvas and copied to the AnnotationBody if
     * motivation = 'sc:painting'
     */
    private void fillInFullTextLinksV2(Manifest manifest, URL fullTextApi) throws EuropeanaApiException {
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
                                manifest.getEuropeanaId(), canvas.getID(), apHash);
                    } else {
                        addFulltextLinkToCanvasV2(canvas, ftCanvas);
                    }
                }
            }
        } else {
            LOG.debug("Not checking for fulltext because record doesn't have any sequences");
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
     * We generate all full text links in one place, so we can raise a timeout if retrieving the necessary
     * data for all full texts is too slow.
     * From EA-2604 on, originalLanguage is available on the FulltextSummaryCanvas and copied to the AnnotationBody if
     * motivation = 'painting'
     */
    private void fillInFullTextLinksV3(eu.europeana.api.iiif.v3.model.Manifest manifest, URL fullTextApi) throws EuropeanaApiException {
        Map<String, FulltextSummaryCanvas> summaryCanvasMap;
        List<eu.europeana.api.iiif.v3.model.Canvas> canvases = manifest.getItems();
        if (canvases != null) {
            // Get all the available AnnoPages incl translations from the summary endpoint of Fulltext
            String fullTextSummaryUrl = generateFullTextSummaryUrl(manifest.getEuropeanaId(), fullTextApi);
            summaryCanvasMap = fulltextService.getFulltextSummary(fullTextSummaryUrl);
            if (null != summaryCanvasMap) {
                // loop over canvases to add full-text link(s) to all
                for (eu.europeana.api.iiif.v3.model.Canvas canvas : canvases) {
                    // we need to generate the same annopageId hash based on imageId
                    String apHash = GenerateUtils.derivePageId(canvas.getStartCanvasAnnotation().getBody().getID());
                    FulltextSummaryCanvas ftCanvas = summaryCanvasMap.get(apHash);
                    if (ftCanvas == null) {
                        // This warning can be logged for empty pages that do not have a fulltext, but if we get a lot
                        // then Record API and Fulltext API are not in sync (or the hashing algorithm changed).
                        LOG.warn("Inconsistent data! No fulltext annopage found for record {} page {}. Generated hash = {}",
                                manifest.getEuropeanaId(), canvas.getPageNr(), apHash);
                    } else {
                        addFulltextLinkToCanvasV3(canvas, ftCanvas);
                    }
                }
            }
        } else {
            LOG.debug("Not checking for fulltext because record doesn't have any canvases");
        }
    }

    private void addFulltextLinkToCanvasV3(eu.europeana.api.iiif.v3.model.Canvas canvas, FulltextSummaryCanvas summaryCanvas) {
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
     * Generates a url to a full text resource
     *
     * @param fullTextApiUrl optional, if not specified then the default Full-Text API specified in .properties is used
     * @param europeanaId    identifier to include in the path
     */
    String generateFullTextSummaryUrl(String europeanaId, URL fullTextApiUrl) {
        if (fullTextApiUrl == null) {
            return settings.getFullTextApiBaseUrl() + ManifestDefinitions.getFulltextSummaryPath(europeanaId);
        } else {
            return fullTextApiUrl + ManifestDefinitions.getFulltextSummaryPath(europeanaId);
        }
    }

    /**
     * Serialize manifest to JSON-LD
     *
     * @param m manifest
     * @return JSON-LD string
     * @throws RecordParseException when there is a problem parsing
     */
    public String serializeManifest(Object m) throws RecordParseException {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(m);
        } catch (IOException e) {
            throw new RecordParseException(String.format("Error serializing data: %s", e.getMessage()), e);
        }
    }

    /**
     * @return ManifestSettings object containing settings loaded from properties file
     */
    public IIIfSettings getSettings() {
        return settings;
    }

}
