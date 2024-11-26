/**
 * 
 */
package eu.europeana.api.caching;

import java.time.ZonedDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;


/**
 * @author Hugo
 * @since 21 Nov 2024
 */
public class ChainingCachingStrategy implements CachingStrategy {

    private static ChainingEtag molde = new ChainingEtag();

    @Override
    public <T> ResponseEntity<T> applyForReadAccess(ResourceCaching caching
                                                  , HttpServletRequest request, HttpHeaders headers) {

        /* In case “If-None-Match” is present:
           1) Decode the value into pairs of API version and Etag of the underlying API;
           2) Check if any of the API version(s) matches the current version, 
              if none of them match make a request to the underlying API with no 
              caching headers and process the response as defined in P2;
           3) If one of versions match, obtain the “Etag” of the underlying API and 
              use it as the value of “If-None-Match” for the request to the 
              underlying API and process the response as defined in P2.
        */
        List<ChainingEtag> list = CachingUtils.getIfNoneMatch(request, molde);
        if ( list != null ) {
            ChainingEtag match = getFirstParentEtagMatch(caching.getETag(), list);
            if ( match == null ) { return null; }

            headers.set(IF_NONE_MATCH, WeakETag.formatAsWeakEtag(match.nestedEtag));
            return null;
        }

        /*
         * In the case "If-None-Match" headers are present but "If-Modified-Since" header is:
           1) If the date is earlier than the release date of parent API, 
              make a request to the underlying API with no cache headers
           2) Otherwise, make a request to the underlying API reusing 
              the "If-Modified-Since" header
         */
        ZonedDateTime ifModifiedSince = CachingUtils.getIfModifiedSince(request);
        if ( ifModifiedSince != null ) {
            if ( ifModifiedSince.compareTo(caching.getLastModified()) > 0 ) {
                headers.set(IF_MODIFIED_SINCE, request.getHeader(IF_MODIFIED_SINCE));
            }
        }
        return null;
    }

    private ChainingEtag getFirstParentEtagMatch(ETag parentEtag, List<ChainingEtag> list) {
        for ( ChainingEtag etag : list ) {
            if ( etag.parentETag.equals(parentEtag.getValue()) ) { return etag; }
        }
        return null;
    }
    

    public static class ChainingEtag implements ETag {

        public String parentETag;
        public String nestedEtag;

        public ChainingEtag() {}

        public ChainingEtag(String parentETag, String nestedEtag) {
            this.parentETag = parentETag;
            this.nestedEtag = nestedEtag;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ChainingEtag parse(String etag) {
            int i = etag.lastIndexOf('|');
            if ( i < 0 ) { return null; }
            return new ChainingEtag(etag.substring(0,i)
                                  , etag.substring(i,etag.length()));
        }

        @Override
        public String format() {
            return String.format("W/\"%1$s|%2$s\"", parentETag, nestedEtag);
        }

        @Override
        public String getValue() {
            return String.format("%1$s|%2$s", parentETag, nestedEtag);
        }

        @Override
        public String toString() {
            return format();
        }
    }
}