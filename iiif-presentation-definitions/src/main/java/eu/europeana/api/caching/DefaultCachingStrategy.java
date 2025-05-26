package eu.europeana.api.caching;

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


/**
 * @author Hugo
 * @since 21 Nov 2024
 */
public class DefaultCachingStrategy implements CachingStrategy {

    private static WeakETag molde = new WeakETag();

    @Override
    public <T> ResponseEntity<T> applyForReadAccess(ResourceCaching caching
                                                  , HttpServletRequest request, HttpHeaders headers) {

        caching.setHeaders(headers);

        /* In case “If-None-Match” is present:
         * Check if any of the etags match the etag of the resource
         * If true then reply with 304, otherwise handle the request
         */
        List<WeakETag> list = CachingUtils.getIfNoneMatch(request, molde);
        if ( list != null ) {
            if ( list.contains(caching.getETag()) ) { 
                return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
            }

            return null;
        }

        /* In the case "If-None-Match" headers is not present but 
           "If-Modified-Since" header is:
           Check if the date sent in the request is more recent than the date of 
           the resource. If true then reply with 304, otherwise handle the request
         */
        ZonedDateTime ifModifiedSince = CachingUtils.getIfModifiedSince(request);
        if ( ifModifiedSince != null ) {
            if ( ifModifiedSince.compareTo(caching.getLastModified()) > 0 ) {
                return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
            }
        }

        return null;
    }
}