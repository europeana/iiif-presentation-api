package eu.europeana.api.iiif.utils;

import eu.europeana.api.commons_sb3.web.http.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

public class IIIFConstants {


    // format
    public static final String V2 = "2";
    public static final String V3 = "3";

    // beans
    public static final String BEAN_FALLBACK_AUTHORIZATION = "fallbackAuth";
    public static final String BEAN_USER_SET_API_CLIENT = "userSetApiClient";
    public static final String BEAN_IIIF_VERSION_SUPPORT = "iiifVersionSupport";    
    public static final String BEAN_IIIF_JSON_HANDLER = "iifJsonHandler";
    public static final String BEAN_MEDIA_TYPES = "mediaTypes";
    public static final String BEAN_XML_MAPPER = "xmlMapper";


    public static final String BEAN_V2_JSON_MAPPER = "v2JsonMapper";
    public static final String BEAN_V3_JSON_MAPPER = "v3JsonMapper";


    public static final String ACCEPT  = "Accept=";

    // Accept Headers for retrieval endpoint
    public static final String ACCEPT_HEADER_JSONLD = ACCEPT + HttpHeaders.CONTENT_TYPE_JSONLD;
    public static final String ACCEPT_HEADER_JSON = ACCEPT + MediaType.APPLICATION_JSON_VALUE;
    public static final String CONTENT_TYPE  = "application/ld+json;profile=\"%s\";charset=UTF-8";

    // for search query
    public static final String QUERY_VISIBILITY_PUBLISHED = "visibility:published";

}
