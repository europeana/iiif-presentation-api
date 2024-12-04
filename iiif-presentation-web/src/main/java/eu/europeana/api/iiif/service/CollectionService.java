package eu.europeana.api.iiif.service;

import eu.europeana.api.iiif.config.IIIfSettings;
import eu.europeana.api.iiif.generator.CollectionV2Generator;
import eu.europeana.api.iiif.generator.CollectionV3Generator;
import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.utils.IIIFConstants;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.client.model.result.RecordPreview;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.ProfileConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

import static eu.europeana.api.iiif.utils.IIIFConstants.QUERY_VISIBILITY_PUBLISHED;
import static eu.europeana.api.iiif.utils.IIIFConstants.V2;

@Service
public class CollectionService {

    private static final Logger LOGGER = LogManager.getLogger(CollectionService.class);

    private final UserSetApiClient userSetApiClient;
    private final CollectionV2Generator collectionV2Generator;
    private final CollectionV3Generator collectionV3Generator;
    private final IIIfSettings settings;

    public CollectionService(@Qualifier(IIIFConstants.BEAN_USER_SET_API_CLIENT) UserSetApiClient userSetApiClient,
                             @Qualifier(IIIFConstants.BEAN_COLLECTION_V2_GENERATOR) CollectionV2Generator collectionV2Generator,
                             @Qualifier(IIIFConstants.BEAN_COLLECTION_V3_GENERATOR) CollectionV3Generator collectionV3Generator, IIIfSettings settings) {
        this.userSetApiClient = userSetApiClient;
        this.collectionV2Generator = collectionV2Generator;
        this.collectionV3Generator = collectionV3Generator;
        this.settings = settings;
    }


    public <T extends IIIFResource> T retrieveCollection(String version) {
        if (StringUtils.equals(version, V2)) {
            return (T) collectionV2Generator.generateRoot();
        }
        return (T) collectionV3Generator.generateRoot();
    }

    /**
     * Fetch all the published sets
     * Query : /set/search?query=visibility:published&pageSize=1000
     *
     * @param iiifVersion
     */
    public <T extends IIIFResource> T getGalleryCollection(String iiifVersion) {
        try {
            List<? extends UserSet> publishedSets = userSetApiClient.getSearchUserSetApi().searchUserSet(
                    QUERY_VISIBILITY_PUBLISHED, null, null, 1, 100, null, 0, null);

            if (StringUtils.equals(iiifVersion, V2)) {
                return (T) collectionV2Generator.generateGalleryRoot(publishedSets);
            }
            return (T) collectionV3Generator.generateGalleryRoot(publishedSets);
        } catch (SetApiClientException e) {
            // todo handling other responses
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetch the set and the items
     * items pagination - set/{setId}?&page=1&pageSize=100&profile=items.meta
     *
     * @param iiifVersion
     * @param setId
     * @param <T>
     * @return
     */
    public <T extends IIIFResource> T retrieveGallery(String iiifVersion, String setId) {
        try {
            UserSet set = userSetApiClient.getWebUserSetApi().getUserSet(setId, null);

            List<RecordPreview> items = userSetApiClient.getWebUserSetApi().getPaginationUserSet(
                    set.getIdentifier(), null, null, 1, 100, ProfileConstants.VALUE_PARAM_ITEMS_META);
            if (StringUtils.equals(iiifVersion, V2)) {
                return (T) collectionV2Generator.generateGallery(set, items);
            }
            return (T) collectionV3Generator.generateGallery(set, items);
        } catch (SetApiClientException e) {
            e.printStackTrace();
            // todo handling other responses

        }
        return null;
    }

}
