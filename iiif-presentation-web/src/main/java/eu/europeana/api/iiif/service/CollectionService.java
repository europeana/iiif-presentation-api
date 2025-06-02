package eu.europeana.api.iiif.service;

import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.definitions.caching.CachingUtils;
import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
import eu.europeana.api.iiif.config.BuildInfo;
import eu.europeana.api.iiif.exceptions.CollectionException;
import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.utils.IIIFConstants;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.client.model.result.RecordPreview;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.ProfileConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static eu.europeana.api.iiif.utils.IIIFConstants.*;

@Service
public class CollectionService {

    private static final Logger LOGGER = LogManager.getLogger(CollectionService.class);

    private final UserSetApiClient setClient;
    private final BuildInfo        buildInfo;

    public CollectionService(
            BuildInfo buildInfo
          , @Qualifier(IIIFConstants.BEAN_USER_SET_API_CLIENT) 
            UserSetApiClient setClient) {
        this.setClient = setClient;
        this.buildInfo = buildInfo;
    }


    public <T extends IIIFResource> T getCollectionRoot(
            IIIFVersionSupport version, ResourceCaching caching) {
        return (T)version.getCollectionGenerator().generateRoot();
    }

    /**
     * Fetch all the published sets
     * Query : /set/search?query=visibility:published&pageSize=100
     *
     * @param version
     */
    public <T extends IIIFResource> T getCollectionOfGalleries(
            IIIFVersionSupport version, AuthenticationHandler auth
          , ResourceCaching caching) 
                    throws CollectionException {
        try {
            setClient.setAuthenticationHandler(auth);
            List<? extends UserSet> sets = setClient.getSearchUserSetApi()
                    .searchUserSet(QUERY_VISIBILITY_PUBLISHED, null, null
                                 , "1", "100", null, 0, null);

            caching.setLastModified(getLastModified(sets));
            caching.setETag(CachingUtils.genWeakEtag(buildInfo.getBuildTimestamp()
                                       , sets.size()
                                       , version.getVersionNr()
                                       , buildInfo.getAppVersion()));

            return (T)version.getCollectionGenerator().generateGalleryRoot(sets);
        }
        catch (SetApiClientException e) {
            throw new CollectionException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Fetch the set and the items
     * items pagination - set/{setId}?&page=1&pageSize=100&profile=items.meta
     *
     * @param version
     * @param setId
     * @param <T>
     * @return
     */
    public <T extends IIIFResource> T getGalleryCollection(
            IIIFVersionSupport version, String setId, AuthenticationHandler auth
          , ResourceCaching caching)
                    throws CollectionException {
        try {
            setClient.setAuthenticationHandler(auth);
            Optional<UserSet> fetched = setClient.getWebUserSetApi().getUserSet(setId, Optional.empty(), Optional.of(caching));
            // 304 response
            if (!fetched.isPresent()) {
                return null;
            }

            UserSet set = fetched.get();
            // TODO The Set API must return the modified date as part of
            // the pagination requests so that it can be used for caching
            // another option is the methods receive a resource cache object
            List<RecordPreview> items = setClient.getWebUserSetApi().getPaginationUserSet(
                    set.getIdentifier(), null, null, "1", "100"
                  , ProfileConstants.VALUE_PARAM_ITEMS_META);

            return (T)version.getCollectionGenerator().generateGallery(set, items);
        }
        catch (SetApiClientException e) {
            throw new CollectionException(e.getLocalizedMessage(), e.getRemoteStatusCode(), e);
        }
    }

    private ZonedDateTime getLastModified(List<? extends UserSet> sets) {
        if ( sets.isEmpty() ) { return null; }

        Date lastModified = null;
        for ( UserSet set : sets ) {
            Date modified = set.getModified();
            if ( lastModified == null || lastModified.before(modified) ) {
                lastModified = modified;
            }
        }

        return ZonedDateTime.ofInstant(lastModified.toInstant()
                                     , ZoneId.systemDefault());
    }
}
