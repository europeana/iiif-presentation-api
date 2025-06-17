package eu.europeana.api.iiif.web;

import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.definitions.caching.CachingStrategy;
import eu.europeana.api.commons_sb3.definitions.caching.CachingUtils;
import eu.europeana.api.commons_sb3.definitions.caching.DefaultCachingStrategy;
import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
import eu.europeana.api.commons_sb3.definitions.format.RdfFormat;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.service.IIIFJsonHandler;
import eu.europeana.api.iiif.service.IIIFVersionSupport;
import eu.europeana.api.iiif.service.IIIFVersionSupportHandler;
import eu.europeana.api.iiif.config.BuildInfo;
import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.service.AbsChainCachingStrategy;
import eu.europeana.api.iiif.service.CollectionCachingStrategy;
import eu.europeana.api.iiif.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;

import static eu.europeana.api.iiif.utils.IIIFConstants.*;
import static eu.europeana.api.iiif.utils.IIIFUtils.*;
import static eu.europeana.api.iiif.oauth.AuthorizationService.*;


@Tag(
        name = "IIIF Presentation API collection rest endpoints"
)
@RestController
public class CollectionController {

    private static final Logger LOGGER = LogManager.getLogger(CollectionController.class);

    private final CollectionService         collectionService;
    private final IIIFJsonHandler           iiifJsonHandler;
    private final IIIFVersionSupportHandler versionHandler;
    private final BuildInfo                 buildInfo;
    private AuthenticationHandler           authFallback;

    private static final CachingStrategy defaultCaching = new DefaultCachingStrategy();
    private static final CollectionCachingStrategy colCaching = new CollectionCachingStrategy();

    @Autowired
    public CollectionController(BuildInfo buildInfo
                              , CollectionService collectionService
                              , IIIFJsonHandler iiifJsonHandler
                              , @Qualifier(value = BEAN_IIIF_VERSION_SUPPORT) IIIFVersionSupportHandler versionHandler
                              , @Qualifier(value = BEAN_FALLBACK_AUTHORIZATION) AuthenticationHandler authFallback) {
        this.collectionService = collectionService;
        this.iiifJsonHandler = iiifJsonHandler;
        this.versionHandler  = versionHandler;
        this.buildInfo       = buildInfo;
        this.authFallback    = authFallback;
    }


    /**
     * Retrieves all the collection
     *
     * @param request HttpServlet request
     * @return Response Entity with StreamingResponseBody
     */

    @Operation(
            summary = "getRootCollection",
            description = "Retrieve all the collections"
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status 200 OK"
    )
    @GetMapping(
            value = {
                    "collection",
            },
            headers = {ACCEPT_HEADER_JSONLD, ACCEPT_HEADER_JSON}
    )

    public ResponseEntity<StreamingResponseBody> getRootCollection(
            HttpServletRequest request) throws EuropeanaApiException {
        IIIFVersionSupport version = versionHandler.getVersionSupport(request);
        RdfFormat          format  = getFormatFromHeader(request, RdfFormat.JSONLD);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(IIIF_VERSION_RDF_FORMAT, version.getVersionNr(), format);
        }

        HttpHeaders     headers = new HttpHeaders();
        ResourceCaching caching = getRootCollectionCaching(version);

        ResponseEntity<StreamingResponseBody> rsp = 
                defaultCaching.applyForReadAccess(caching, request, headers);
        if ( rsp != null ) { return rsp; }

        IIIFResource resource = collectionService.getCollectionRoot(version, caching);
        return getResponse(headers, version, resource);
    }


    /**
     * Endpoint to serve all the gallery collections
     *
     * @param req HttpServlet request
     * @return Response Entity with StreamingResponseBody
     */

    @Operation(
            summary = "getCollectionOfGalleries",
            description = "Retrieve gallery collections"
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status 200 OK"
    )
    @GetMapping(
            value = {
                    "collection/gallery",
            },
            headers = {ACCEPT_HEADER_JSONLD, ACCEPT_HEADER_JSON})
    public ResponseEntity<StreamingResponseBody> getCollectionOfGalleries(
            HttpServletRequest req) throws EuropeanaApiException {

        AuthenticationHandler auth    = getAuthorization(req, authFallback);
        IIIFVersionSupport    version = versionHandler.getVersionSupport(req);
        RdfFormat             format  = getFormatFromHeader(req, RdfFormat.JSONLD);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(IIIF_VERSION_RDF_FORMAT, version.getVersionNr(), format);
        }

        HttpHeaders     headers = new HttpHeaders();
        ResourceCaching caching = getCollectionOfGalleriesCaching();

        IIIFResource resource = 
                collectionService.getCollectionOfGalleries(version, auth, caching);

        ResponseEntity<StreamingResponseBody> rsp = 
                defaultCaching.applyForReadAccess(caching, req, headers);
        if ( rsp != null ) { return rsp; }
        return getResponse(headers, version, resource);
    }

    /**
     * Endpoint to serve a collection generated from a set/gallery
     *
     * @param path - path consist of  -
     *                       set id ,
     *                       Rdf Format extension with .json or .jsonld(optional)
     * @param req HttpServlet request
     * @return Response Entity with StreamingResponseBody
     */
    @Operation(
            summary = "getGalleryCollection",
            description = "Retrieve a Gallery in json/json-ld"
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status 200 OK"
    )
    @GetMapping(
            value = {
                    "collection/gallery/{path}",
            },
            headers = {ACCEPT_HEADER_JSONLD, ACCEPT_HEADER_JSON})
    public ResponseEntity<StreamingResponseBody> getGalleryCollection(
            @PathVariable String path,
            HttpServletRequest req) throws EuropeanaApiException {

        AuthenticationHandler auth    = getAuthorization(req, authFallback);
        IIIFVersionSupport    version = versionHandler.getVersionSupport(req);
        RdfFormat             format = getFormat(req);
        String                setId  = getId(path);


        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(IIIF_VERSION_RDF_FORMAT, version.getVersionNr(), format);
        }

        HttpHeaders     rspHeaders = new HttpHeaders();
        ResourceCaching baseCache  = getGalleryCollectionCaching(version);

        final SourceData data = new SourceData();

        ResponseEntity<StreamingResponseBody> rsp = 
                colCaching.applyForReadAccess(baseCache, req, rspHeaders,
            new AbsChainCachingStrategy.Service() {
                @Override
                public boolean request(HttpHeaders reqHeaders
                                     , ResourceCaching caching) throws EuropeanaApiException {
                    data.col = collectionService.getGalleryCollection(version, setId
                                                               , auth, caching);
                    return (data.col != null);
                }
            }
        );
        if ( rsp != null ) { return rsp; }
        return getResponse(rspHeaders, version, data.col);
    }

    private ResponseEntity<StreamingResponseBody> getResponse(
            HttpHeaders rspHeaders,
            IIIFVersionSupport version, IIIFResource iiifResource) {
        rspHeaders.add(HttpHeaders.CONTENT_TYPE, version.getContentType());
        StreamingResponseBody responseBody = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                iiifJsonHandler.write(iiifResource, out);
                out.flush();
            }
        };
        return new ResponseEntity<>(responseBody, rspHeaders, HttpStatus.OK);
    }

    private ResourceCaching getRootCollectionCaching(IIIFVersionSupport iiifVersion) {
        return new ResourceCaching(
                "public, max-age=604800, immutable"
              , CachingUtils.genWeakEtag(buildInfo.getBuildTimestamp()
                                       , iiifVersion.getVersionNr()
                                       , buildInfo.getAppVersion())
              , buildInfo.getBuildDateTime());
        
    }

    private ResourceCaching getCollectionOfGalleriesCaching() {
        return new ResourceCaching("public, max-age=86400", null, null);
    }

    private ResourceCaching getGalleryCollectionCaching(IIIFVersionSupport version) {
        return new ResourceCaching(
                null
              , CachingUtils.genWeakEtag(version.getVersionNr()
                                       , buildInfo.getAppVersion())
              , this.buildInfo.getBuildDateTime());
    }

    private static class SourceData {
        protected IIIFResource col = null;
    }
}
