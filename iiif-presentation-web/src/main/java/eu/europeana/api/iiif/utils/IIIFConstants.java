package eu.europeana.api.iiif.utils;

import eu.europeana.api.commons_sb3.web.http.HttpHeaders;
import org.springframework.http.MediaType;

public class IIIFConstants {

    public static final String ACCEPT  = "Accept=";

    // Accept Headers for retrieval endpoint
    public static final String ACCEPT_HEADER_JSONLD = ACCEPT + HttpHeaders.CONTENT_TYPE_JSONLD;
    public static final String ACCEPT_HEADER_JSON = ACCEPT + MediaType.APPLICATION_JSON_VALUE;
}
