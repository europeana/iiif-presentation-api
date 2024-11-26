package eu.europeana.api.iiif.web;


import eu.europeana.api.commons_sb3.definitions.format.RdfFormat;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.web.http.HttpHeaders;
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

import static eu.europeana.api.iiif.utils.IIIFConstants.ACCEPT_HEADER_JSONLD;
import static eu.europeana.api.iiif.utils.IIIFConstants.ACCEPT_HEADER_JSON;

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
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StreamingResponseBody> retrieveCollection(
            @RequestParam(value = "format", required = false) String version,
            HttpServletRequest request) {
        collectionService.retrieveCollection("3");

        return new ResponseEntity<>(null, HttpStatus.OK);
    }


    /**
     * Endpoint to serve all the gallery collections
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
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StreamingResponseBody> galleryCollection(
            @RequestParam(value = "format", required = false) String version,
            HttpServletRequest request) {
//       String iiifVersion = AcceptUtils.getRequestVersion(request, version);
        collectionService.getGalleryCollection("3");
        return new ResponseEntity<>(null, HttpStatus.OK);
    }



    /**
     * Endpoint to serve a collection generated from a set/gallery
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
            headers = { ACCEPT_HEADER_JSONLD, ACCEPT_HEADER_JSON},
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
