package eu.europeana.api.iiif.service;

import java.time.ZonedDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import eu.europeana.api.commons_sb3.definitions.caching.ETag;
import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.exceptions.ResourceNotChangedException;

/**
 * @author Hugo
 * @since 21 Nov 2024
 */
public class ManifestCachingStrategy extends AbsChainCachingStrategy {

    private static CompoundETag molde = new CompoundETag(new ETag[3]);

    public ManifestCachingStrategy() {
    }

    protected CompoundETag getMolde() {
        return molde;
    }

   protected <T> ResponseEntity<T> handleTimeBasedCaching(
           ResourceCaching apiCaching, ZonedDateTime modSince
         , HttpHeaders rspHeaders, Service... services) 
                 throws EuropeanaApiException {

        Service recordService   = services[0];
        Service fulltextService = services[1];

        ZonedDateTime dateTime = apiCaching.getLastModified();
        // if the API has been modified since the Etag was generated than a 
        // fresh response is necessary
        if ( dateTime != null && modSince.isBefore(dateTime) ) {
            return handleNoCaching(apiCaching, rspHeaders
                                 , recordService, fulltextService);
        }

        ResourceCaching recordCaching   = new ResourceCaching();
        ResourceCaching fulltextCaching = new ResourceCaching();
        HttpHeaders headers = new HttpHeaders();
        headers.setIfModifiedSince(modSince);

        try {
            recordService.request(headers, recordCaching);
            /*
             * If the code reaches here then there has been changes in the record 
             * since the last modification date.
             * This means that, from this point forward, a fresh handling needs 
             * to be done and therefore all data needs to be obtained (without 
             * the use of caching) 
             * */
            fulltextService.request(null, fulltextCaching);

            //A fresh response will be generated
            newCaching(apiCaching, recordCaching, fulltextCaching).setHeaders(rspHeaders);
            return null;
        }
        catch(ResourceNotChangedException e) {
        }

        try {
            boolean success = fulltextService.request(headers, fulltextCaching);
            if (!success) {
                //happens in the case there is no fulltext
                return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
            }
        }
        catch(ResourceNotChangedException e) {
            //Nothing has changed
            return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
        }

        //A fresh response needs to be generated.

        /*
         * Since the record API responded with a not modified, it is necessary to 
         * get a fresh copy of the data.
         */
        recordService.request(null, recordCaching);

        newCaching(apiCaching, recordCaching, fulltextCaching).setHeaders(rspHeaders);
        return null;
    }
    
    
    protected <T> ResponseEntity<T> handleVersionBasedCaching(
            ResourceCaching apiCaching, CompoundETag etag, HttpHeaders rspHeaders
          , Service... services) throws EuropeanaApiException {

        Service recordService   = services[0];
        Service fulltextService = services[1];

        ResourceCaching recordCaching   = new ResourceCaching();
        ResourceCaching fulltextCaching = new ResourceCaching();

        HttpHeaders headers = new HttpHeaders();
        setIfNoneMatch(headers, etag.getEtag(1));
        try {
            recordService.request(headers, recordCaching);
            /*
             * If the code reaches here then there has been changes in the record.
             * This means that, from this point forward, a fresh handling needs 
             * to be done and therefore all data needs to be obtained (without 
             * the use of caching) 
             * */
            fulltextService.request(null, fulltextCaching);

            //A fresh response will be generated
            newCaching(apiCaching, recordCaching, fulltextCaching).setHeaders(rspHeaders);
            return null;
        }
        catch(ResourceNotChangedException e) {
        }

        setIfNoneMatch(headers, etag.getEtag(2));
        try {
            boolean success = fulltextService.request(headers, fulltextCaching);
            if (!success) {
                //happens in the case there is no fulltext
                return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
            }
        }
        catch(ResourceNotChangedException e) {
            //Nothing has changed
            return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
        }

        //A fresh response needs to be generated.

        /*
         * Since the record API responded with a not modified, it is necessary to 
         * get a fresh copy of the data.
         */
        recordService.request(null, recordCaching);

        newCaching(apiCaching, recordCaching, fulltextCaching).setHeaders(rspHeaders);
        return null;
    }



}