/**
 * 
 */
package eu.europeana.api.caching;

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
}
