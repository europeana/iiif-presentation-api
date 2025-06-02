package eu.europeana.api.iiif.service;

import java.time.ZonedDateTime;

import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import eu.europeana.api.commons_sb3.definitions.caching.ETag;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.exceptions.ResourceNotChangedException;

/**
 * @author Hugo
 * @since 21 Nov 2024
 */
public class CollectionCachingStrategy extends AbsChainCachingStrategy {

    private static CompoundETag molde = new CompoundETag(new ETag[2]);

    public CollectionCachingStrategy() {
    }

    protected CompoundETag getMolde() {
        return molde;
    }

   protected <T> ResponseEntity<T> handleTimeBasedCaching(
           ResourceCaching apiCaching, ZonedDateTime modSince
         , HttpHeaders rspHeaders, Service... services) 
                 throws EuropeanaApiException {

       Service setService = services[0];

        ZonedDateTime dateTime = apiCaching.getLastModified();
        // if the API has been modified since the Etag was generated than a 
        // fresh response is necessary
        if ( dateTime != null && modSince.isBefore(dateTime) ) {
            return handleNoCaching(apiCaching, rspHeaders, setService);
        }

        ResourceCaching setCaching = new ResourceCaching();
        HttpHeaders headers = new HttpHeaders();
        headers.setIfModifiedSince(modSince);

        try {
            setService.request(headers, setCaching);

            //A fresh response will be generated
            newCaching(apiCaching, setCaching).setHeaders(rspHeaders);
            return null;
        }
        catch(ResourceNotChangedException e) {
            return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
        }
    }
    
    
    protected <T> ResponseEntity<T> handleVersionBasedCaching(
            ResourceCaching apiCaching, CompoundETag etag, HttpHeaders rspHeaders
          , Service... services) throws EuropeanaApiException {

        Service setService = services[0];
        ResourceCaching setCaching   = new ResourceCaching();
        
        HttpHeaders headers = new HttpHeaders();
        setIfNoneMatch(headers, etag.getEtag(1));
        try {
            setService.request(headers, setCaching);

            //A fresh response will be generated
            newCaching(apiCaching, setCaching).setHeaders(rspHeaders);
            return null;
        }
        catch(ResourceNotChangedException e) {
            return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
        }
    }
}