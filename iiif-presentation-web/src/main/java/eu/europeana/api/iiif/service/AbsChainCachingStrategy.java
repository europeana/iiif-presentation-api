package eu.europeana.api.iiif.service;

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import eu.europeana.api.caching.CachingHeaders;
import eu.europeana.api.caching.ETag;
import eu.europeana.api.caching.ResourceCaching;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;

import static eu.europeana.api.caching.CachingUtils.*;

/**
 * @author Hugo
 * @since 21 Nov 2024
 */
public abstract class AbsChainCachingStrategy implements CachingHeaders {

    //a nested service which the API relies on to get the data it needs to build
    //a response
    public static interface Service {
        boolean request(HttpHeaders headers, ResourceCaching caching) throws EuropeanaApiException;
    }
    
    public <T> ResponseEntity<T> applyForReadAccess(
                                   ResourceCaching apiCaching
                                 , HttpServletRequest request
                                 , HttpHeaders rspHeaders
                                 , Service... services) throws EuropeanaApiException {

        CompoundETag  etag     = getMatchingEtag(request, apiCaching.getETag());
        ZonedDateTime modSince = getIfModifiedSince(request);

        //version based (using Etag)
        if ( etag != null ) { 
            return handleVersionBasedCaching(apiCaching, etag, rspHeaders
                                           , services);
        }

        //time based (using LastModified)
        if ( modSince != null ) { 
            return handleTimeBasedCaching(apiCaching, modSince, rspHeaders
                                        , services);
        }

        // No caching was available, therefore a fresh handling is required
        return handleNoCaching(apiCaching, rspHeaders, services);
    }

    /*
     * Calls all services in sequence without caching and constructs a 
     * compound ETag composed of all caching headers received from the delegating
     * services.
     */
    protected <T> ResponseEntity<T> handleNoCaching(
            ResourceCaching apiCaching, HttpHeaders rspHeaders
          , Service... services) throws EuropeanaApiException {
        
        ResourceCaching[] cachings = new ResourceCaching[services.length+1];
        cachings[0] = apiCaching;
        int i = 1;
        for ( Service service : services ) {
            ResourceCaching caching = new ResourceCaching();
            service.request(null, caching);
            cachings[i++] = caching;
        }
        newCaching(cachings).setHeaders(rspHeaders);
        return null;

    }

    protected abstract <T> ResponseEntity<T> handleTimeBasedCaching(
           ResourceCaching apiCaching, ZonedDateTime modSince
         , HttpHeaders rspHeaders, Service... services) 
                 throws EuropeanaApiException;

    
    protected abstract <T> ResponseEntity<T> handleVersionBasedCaching(
            ResourceCaching apiCaching, CompoundETag etag, HttpHeaders rspHeaders
          , Service... services) throws EuropeanaApiException;

    protected abstract CompoundETag getMolde();

    protected void setIfNoneMatch(HttpHeaders headers, ETag etag) {
        if ( etag == null ) { return; }
        headers.setIfNoneMatch(etag.format());
    }

    protected ResourceCaching newCaching(ResourceCaching... cachings) {
        ResourceCaching ret = new ResourceCaching();
        ret.setETag(getEtag(cachings));
        ret.setLastModified(getLastModified(cachings));

        /* sets the cache control with the first one it finds.
         * if this API defines a specific cache-contrl then set it on the 
         * ResourceCaching that corresponds to this API.
         */
        ret.setCacheControl(getFirstCacheControl(cachings));
        return ret;
    }

    //Constructs a compound etag made out of all resource cachings
    private CompoundETag getEtag(ResourceCaching... cachings) {
        ETag[] tags = new ETag[cachings.length];
        int i = 0;
        for ( ResourceCaching c : cachings ) {
            tags[i++] = c.getETag();
        }
        return new CompoundETag(tags);
    }

    //Gets the latest last modified from all resource cachings
    private ZonedDateTime getLastModified(ResourceCaching... cachings) {
        ZonedDateTime latest = null;
        for ( ResourceCaching c : cachings ) {
            ZonedDateTime dt = c.getLastModified();
            if ( latest == null 
             || (dt != null && latest.isBefore(dt)) ) { latest = dt; }
        }
        return latest;
    }

    //Gets the first available cache control from all resource cachings
    private String getFirstCacheControl(ResourceCaching... cachings) {
        for ( ResourceCaching c : cachings ) {
            String cacheControl = c.getCacheControl();
            if ( cacheControl != null ) { return cacheControl; }
        }
        return null;
    }

    //Gets all Etags that match the version of this API
    public CompoundETag getMatchingEtag(HttpServletRequest request, ETag prefix) {
        List<CompoundETag> list = getIfNoneMatch(request, getMolde());
        if ( list == null ) { return null; }

        for ( CompoundETag etag : list ) {
            if ( etag.getFirstEtag().equals(prefix) ) { return etag; }
        }
        return null;
    }
}