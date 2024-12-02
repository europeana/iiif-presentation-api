package eu.europeana.api.iiif.web;


import eu.europeana.api.commons_sb3.definitions.format.RdfFormat;
import eu.europeana.api.commons_sb3.definitions.iiif.AcceptUtils;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.web.http.HttpHeaders;
import eu.europeana.api.iiif.io.IIIFJsonHandler;
import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.service.CollectionService;
import eu.europeana.api.iiif.utils.IIIFUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;

import static eu.europeana.api.iiif.utils.IIIFConstants.ACCEPT_HEADER_JSON;
import static eu.europeana.api.iiif.utils.IIIFConstants.ACCEPT_HEADER_JSONLD;

@Tag(
        name = "IIIF Presentation API collection rest endpoints"
)
@RestController
public class CollectionController {

    private static final Logger LOGGER = LogManager.getLogger(CollectionController.class);

    private final CollectionService collectionService;

    @Autowired
    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }


    /**
     * Retrieves all the collection
     *
     * @param request HttpServlet request
     * @return Response Entity with StreamingResponseBody
     */

    @Operation(
            summary = "retrieveCollection",
            description = "Retrieve all the collections"
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status 200 OK"
    )
    @GetMapping(
            value = {
                    "collection/",
            },
            headers = {ACCEPT_HEADER_JSONLD, ACCEPT_HEADER_JSON}
    )
    public ResponseEntity<StreamingResponseBody> retrieveCollection(
            HttpServletRequest request) {
        String iiifVersion = AcceptUtils.getRequestVersion(request, null);
        IIIFResource resource = collectionService.retrieveCollection(iiifVersion);
        LOGGER.debug("iiif version {} , Accept : {}", iiifVersion, request.getHeader("Accept"));

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        // TODO - Check if the requested media type in the Accept header is supported (if empty defaults to JSON-LD), otherwise respond with HTTP 406;
        headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_JSONLD);
        StreamingResponseBody responseBody = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                IIIFJsonHandler jsonHandler = new IIIFJsonHandler();
                jsonHandler.write(resource, out);
                //formatHandlerRegistry.get(recordRequest.getRdfFormat()).write(providedCHO, out);
                out.flush();
            }
        };
        return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
    }


    /**
     * Endpoint to serve all the gallery collections
     *
     * @param request HttpServlet request
     * @return Response Entity with StreamingResponseBody
     */

    @Operation(
            summary = "galleryCollection",
            description = "Retrieve gallery collections"
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status 200 OK"
    )
    @GetMapping(
            value = {
                    "collection/gallery/",
            },
            headers = {ACCEPT_HEADER_JSONLD, ACCEPT_HEADER_JSON})
    public ResponseEntity<StreamingResponseBody> galleryCollection(
            HttpServletRequest request) {
        String iiifVersion = AcceptUtils.getRequestVersion(request, null);
        RdfFormat format = IIIFUtils.getRDFFormatFromHeader(request);

        LOGGER.debug("iiif version {} , Accept : {} rdf format {}", iiifVersion, request.getHeader("Accept"), format);

        IIIFResource resource = collectionService.getGalleryCollection(iiifVersion);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        // TODO - Check if the requested media type in the Accept header is supported (if empty defaults to JSON-LD), otherwise respond with HTTP 406;
        headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_JSONLD);
        StreamingResponseBody responseBody = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                IIIFJsonHandler jsonHandler = new IIIFJsonHandler();
                jsonHandler.write(resource, out);
                //formatHandlerRegistry.get(recordRequest.getRdfFormat()).write(providedCHO, out);
                out.flush();
            }
        };
        return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
    }


    /**
     * Endpoint to serve a collection generated from a set/gallery
     *
     * @param request HttpServlet request
     * @return Response Entity with StreamingResponseBody
     */

    @Operation(
            summary = "retrieveGallery",
            description = "Retrieve a Gallery in json/json-ld"
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status 200 OK"
    )
    @GetMapping(
            value = {
                    "collection/gallery/{setId}",
            },
            headers = {ACCEPT_HEADER_JSONLD, ACCEPT_HEADER_JSON},
            produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StreamingResponseBody> retrieveGallery(
            @PathVariable String setId,
            @RequestParam(value = "format", required = false) String version,
            HttpServletRequest request) throws EuropeanaApiException {

        // get format and clean the setId if required
        RdfFormat format = IIIFUtils.getRDFFormatFromId(setId);
        // if there is an extension present .json/.jsonld, clean the set id
        if (format != null) {
            setId = StringUtils.substringBeforeLast(setId, ".");
        }
        // if format is still empty check for Accept header.
        if (format == null && request.getHeader(org.springframework.http.HttpHeaders.ACCEPT) != null) {
            format = IIIFUtils.getRDFFormatFromHeader(request);
        }

        // if format is still null -> no .extension and no Accept header, default it to JsonLD format
        if (format == null) {
            format = RdfFormat.JSONLD;
        }

        LOGGER.debug("RdfFormat : {} , set ID : {}", format, setId);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }


}
