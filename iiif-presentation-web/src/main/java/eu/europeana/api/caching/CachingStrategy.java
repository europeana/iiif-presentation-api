package eu.europeana.api.caching;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/**
 * @author Hugo
 * @since 22 Nov 2024
 */
public interface CachingStrategy extends CachingHeaders {

    public <T> ResponseEntity<T> applyForReadAccess(ResourceCaching caching
                                                  , HttpServletRequest request
                                                  , HttpHeaders headers);

}
