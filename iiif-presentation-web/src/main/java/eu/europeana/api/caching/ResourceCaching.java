/**
 * 
 */
package eu.europeana.api.caching;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpHeaders;

import static eu.europeana.api.caching.CachingHeaders.*;

/**
 * @author Hugo
 * @since 25 Nov 2024
 */
public class ResourceCaching {

    private String        cacheControl;
    private ETag          etag;
    private ZonedDateTime lastModified;

    public ResourceCaching(String cacheControl, ETag etag
                         , ZonedDateTime lastModified) {
        this.cacheControl = cacheControl;
        this.etag = etag;
        this.lastModified = lastModified;
    }

    public String getCacheControl() {
        return this.cacheControl;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    public ETag getETag() {
        return this.etag;
    }

    public void setETag(ETag etag) {
        this.etag = etag;
    }

    public ZonedDateTime getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(ZonedDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public void setHeaders(HttpHeaders headers) {
        if ( cacheControl != null ) { 
            headers.set(CACHE_CONTROL, cacheControl);
        }
        if ( etag != null ) { 
            headers.set(ETAG, etag.toString());
        }
        if ( lastModified != null ) { 
            headers.set(LAST_MODIFIED, dateToString(lastModified)); 
        }
    }

    private static String dateToString(ZonedDateTime lastModified) {
        return lastModified.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}
