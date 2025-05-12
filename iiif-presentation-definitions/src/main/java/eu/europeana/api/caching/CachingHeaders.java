package eu.europeana.api.caching;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Hugo
 * @since 21 Nov 2024
 */
public interface CachingHeaders {

    public static final String IF_MATCH          = "If-Match";
    public static final String IF_NONE_MATCH     = "If-None-Match";
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    public static final String CACHE_CONTROL     = "Cache-Control";
    public static final String ETAG              = "ETag";
    public static final String LAST_MODIFIED     = "Last-Modified";


    /**
     * Method will check caching headers are present
     *
     * @param request
     * @return
     */
    public static boolean cachingHeadersPresent(HttpServletRequest request) {
        return StringUtils.isNotBlank(request.getHeader(IF_MODIFIED_SINCE)) ||
                StringUtils.isNotBlank(request.getHeader(IF_NONE_MATCH)) ||
                StringUtils.isNotBlank(request.getHeader(IF_MATCH));
    }
}
