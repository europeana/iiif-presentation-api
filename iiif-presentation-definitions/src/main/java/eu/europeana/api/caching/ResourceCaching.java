/**
 * 
 */
package eu.europeana.api.caching;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpHeaders;

import static eu.europeana.api.caching.CachingHeaders.*;

/**
 * @author Hugo
 * @since 25 Nov 2024
 */
/*
 * Represents the caching information of the resource.
 * 
 * Typically used to check caching request headers against and return in response 
 * to caching requests.
 */
public class ResourceCaching {

    private String        cacheControl;
    private ETag          etag;
    private ZonedDateTime lastModified;

    public ResourceCaching(ResourceCaching template) {
        this(template.getCacheControl(), template.getETag()
           , template.getLastModified());
    }

    public ResourceCaching(String cacheControl, ETag etag
                         , ZonedDateTime lastModified) {
        this.cacheControl = cacheControl;
        this.etag = etag;
        this.lastModified = lastModified;
    }

    public ResourceCaching() {
        
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
            headers.setCacheControl(cacheControl);
        }
        if ( etag != null ) { 
            headers.setETag(etag.format());
        }
        if ( lastModified != null ) { 
            headers.setLastModified(lastModified); 
        }
    }

    public void getHeaders(HttpHeaders headers) {
        this.cacheControl = headers.getCacheControl();
        this.etag         = CachingUtils.parseETag(headers.getETag());
        this.lastModified = CachingUtils.getLastModified(headers.getLastModified());
    }

    public ResourceCaching clone() {
        return new ResourceCaching(this);
    }

    private static String dateToString(ZonedDateTime lastModified) {
        return lastModified.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}
