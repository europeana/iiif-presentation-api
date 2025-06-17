package eu.europeana.api.iiif.web;

import eu.europeana.api.commons_sb3.definitions.caching.CachingUtils;
import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.config.BuildInfo;
import eu.europeana.api.iiif.exceptions.InvalidIIIFVersionException;
import eu.europeana.api.iiif.exceptions.ManifestInvalidUrlException;
import eu.europeana.api.iiif.generator.ManifestGenerator;
import eu.europeana.api.iiif.generator.ManifestSettings;
import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.model.info.FulltextSummaryCanvas;
import eu.europeana.api.iiif.service.AbsChainCachingStrategy;
import eu.europeana.api.iiif.service.FulltextService;
import eu.europeana.api.iiif.service.IIIFVersionSupport;
import eu.europeana.api.iiif.service.IIIFVersionSupportHandler;
import eu.europeana.api.iiif.service.ManifestCachingStrategy;
import eu.europeana.api.iiif.service.ManifestService;
import eu.europeana.api.iiif.service.RecordService;
import eu.europeana.api.iiif.utils.ValidateUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static eu.europeana.api.iiif.utils.IIIFConstants.*;
import static eu.europeana.api.iiif.oauth.AuthorizationService.*;

/**
 * Rest controller that handles manifest requests
 *
 * @author Patrick Ehlert
 * Created on 06-12-2017
 */
@RestController
@RequestMapping("/presentation")
public class ManifestController {

    private static final Logger LOG = LogManager.getLogger(ManifestController.class);

    private static final ManifestCachingStrategy cachingStrategy 
        = new ManifestCachingStrategy();

    private ManifestService           manifestService;
    private RecordService             recordService;
    private FulltextService           fulltextService;
    private IIIFVersionSupportHandler versionHandler;
    private final BuildInfo           buildInfo;
    private ManifestSettings          settings;
    private AuthenticationHandler     authFallback;

    public ManifestController(BuildInfo buildInfo
                            , ManifestSettings settings
                            , ManifestService manifestService
                            , RecordService recordService
                            , FulltextService fulltextService
                            , @Qualifier(value = BEAN_IIIF_VERSION_SUPPORT) 
                              IIIFVersionSupportHandler versionHandler
                            , @Qualifier(value = BEAN_FALLBACK_AUTHORIZATION) 
                              AuthenticationHandler authFallback) {
        this.settings        = settings;
        this.manifestService = manifestService;
        this.recordService   = recordService;
        this.fulltextService = fulltextService;
        this.versionHandler  = versionHandler;
        this.buildInfo       = buildInfo;
        this.authFallback    = authFallback;
    }

    /**
     * handles Invalid Urls like '/x/y/', '/x/manifest' , '/manifest'
     * Returns 400 bad Request
     * @return responseEntity
     * @return
     * @throws ManifestInvalidUrlException
     */
    @GetMapping(value = {"/{datasetId}/{recordId}", "/{Id}/manifest", "/manifest"})
    public ResponseEntity<String> invalidMappingUrls() 
            throws ManifestInvalidUrlException {
        throw new ManifestInvalidUrlException("Either recordId or datasetId is missing. Correct url is /{datasetId}/{recordId}/manifest");
    }

    /**
     * Handles manifest requests
     *
     * @param datasetId    (required field)
     * @param recordId     (required field)
     * @param wskey        apikey (required field)
     * @param version      (optional) indicates which IIIF version to generate, either '2' or '3'
     * @param recordApi    (optional) alternative recordApi baseUrl to use for retrieving record data
     * @param addFullText  (optional) perform fulltext exists check or not`1
     * @param fullTextApi  (optional) alternative fullTextApi baseUrl to use for retrieving record data
     * @return JSON-LD string containing manifest
     * @throws EuropeanaApiException when something goes wrong during processing
     */
    @SuppressWarnings("squid:S00107") // too many parameters -> we cannot avoid it.

    @GetMapping(value = "/{datasetId}/{recordId}/manifest"
              , headers = ACCEPT_HEADER_JSON)
    public ResponseEntity<StreamingResponseBody> manifestRequestJson(
            @PathVariable String datasetId,
            @PathVariable String recordId,
            @RequestParam(value = "recordApi", required = false) String recordApi,
            @RequestParam(value = "fullText", required = false, defaultValue = "true") Boolean addFullText,
            @RequestParam(value = "fullTextApi", required = false) String fullTextApi,
            HttpServletRequest request) throws EuropeanaApiException {
        return handleRequest(datasetId, recordId, recordApi, addFullText, fullTextApi, request);
    }

    @GetMapping(value = "/test/error")
    public void testError() throws InvalidIIIFVersionException {
        throw new InvalidIIIFVersionException("This is a test");
    }

    @GetMapping(value = "/{datasetId}/{recordId}/manifest"
              , headers = ACCEPT_HEADER_JSONLD)
    public ResponseEntity<StreamingResponseBody> manifestRequestJsonLd(
            @PathVariable String datasetId,
            @PathVariable String recordId,
            @RequestParam(value = "recordApi", required = false) String recordApi,
            @RequestParam(value = "fullText", required = false, defaultValue = "true") Boolean addFullText,
            @RequestParam(value = "fullTextApi", required = false) String fullTextApi,
            HttpServletRequest request) throws EuropeanaApiException {
        return handleRequest(datasetId, recordId, recordApi, addFullText, fullTextApi, request);
    }

    private ResponseEntity<StreamingResponseBody> handleRequest( String datasetId,
            String recordId,
            String recordApi,
            boolean addFullText,
            String fullTextApi,
            HttpServletRequest request) throws EuropeanaApiException {

        String id = "/" + datasetId + "/" + recordId;
        ValidateUtils.validateRecordIdFormat(id);

        if (recordApi != null) {
            ValidateUtils.validateApiUrlFormat(recordApi);
        }
        if (fullTextApi != null) {
            ValidateUtils.validateApiUrlFormat(fullTextApi);
        }

        AuthenticationHandler auth       = getAuthorization(request, authFallback);
        IIIFVersionSupport    version    = versionHandler.getVersionSupport(request);
        HttpHeaders           rspHeaders = new HttpHeaders();
        ResourceCaching       baseCache  = getBaseCache(version);

        final SourceData data = new SourceData();

        ResponseEntity<StreamingResponseBody> rsp = 
                cachingStrategy.applyForReadAccess(baseCache, request, rspHeaders,
            new AbsChainCachingStrategy.Service() {

                @Override
                public boolean request(HttpHeaders reqHeaders
                                  , ResourceCaching caching) throws EuropeanaApiException {
                    String endpoint 
                        = ( recordApi == null ? settings.getRecordApiEndpoint() 
                                              : recordApi + settings.getRecordApiPath());
                    data.record = recordService.getRecordJson(endpoint, id, auth, reqHeaders, caching);
                    return true;
                }
            },
            new AbsChainCachingStrategy.Service() {

                @Override
                public boolean request(HttpHeaders reqHeaders
                                  , ResourceCaching caching) throws EuropeanaApiException {
                    if (!addFullText) { return false; }

                    data.fulltext = fulltextService.getFulltextSummary(id, fullTextApi, auth, reqHeaders, caching);
                    return (data.fulltext != null);
                }
            }
        );
        if ( rsp != null ) { return rsp; }


        ManifestGenerator<IIIFResource> generator = version.getManifestGenerator();
        IIIFResource manifest = generator.generateManifest(data.record);
        generator.fillWithFullText(manifest, data.fulltext);

        rspHeaders.add(HttpHeaders.CONTENT_TYPE, version.getContentType());        
        StreamingResponseBody responseBody = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                manifestService.serializeManifest(manifest, out);
                out.flush();
            }
        };
        return new ResponseEntity<>(responseBody, rspHeaders, HttpStatus.OK);
    }

    private static class SourceData {
        public Object                             record   = null;
        public Map<String, FulltextSummaryCanvas> fulltext = null;
    }

    private ResourceCaching getBaseCache(IIIFVersionSupport version) {
        
        return new ResourceCaching(
                null
              , CachingUtils.genWeakEtag(version.getVersionNr()
                                       , buildInfo.getAppVersion())
              , this.buildInfo.getBuildDateTime());
    }

}
