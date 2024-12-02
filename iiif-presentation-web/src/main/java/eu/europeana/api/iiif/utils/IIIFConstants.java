package eu.europeana.api.iiif.utils;

import eu.europeana.api.commons_sb3.web.http.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

public class IIIFConstants {


    // beans
    public static final String BEAN_USER_SET_API_CLIENT = "userSetApiClient";
    public static final String BEAN_COLLECTION_V2_GENERATOR = "collectionV2Generator";
    public static final String BEAN_COLLECTION_V3_GENERATOR = "collectionV3Generator";
    public static final String BEAN_JSON_MAPPER = "jsonMapper";


    public static final String ACCEPT  = "Accept=";

    // Accept Headers for retrieval endpoint
    public static final String ACCEPT_HEADER_JSONLD = ACCEPT + HttpHeaders.CONTENT_TYPE_JSONLD;
    public static final String ACCEPT_HEADER_JSON = ACCEPT + MediaType.APPLICATION_JSON_VALUE;
}
