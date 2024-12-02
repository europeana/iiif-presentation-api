package eu.europeana.api.iiif.service;

import eu.europeana.api.iiif.config.IIIfSettings;
import eu.europeana.api.iiif.generator.CollectionV2Generator;
import eu.europeana.api.iiif.generator.CollectionV3Generator;
import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.utils.IIIFConstants;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.definitions.model.UserSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.PROFILE_STANDARD;

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
        if (StringUtils.equals(version, "2")) {
            return (T) collectionV2Generator.generateRoot(settings.getCollectionRootURI(), settings.getGalleryRootURI());
        }
        return (T) collectionV3Generator.generateRoot(settings.getCollectionRootURI(), settings.getGalleryRootURI());
    }

    /**
     * Fetch all the published sets
     * Query : /set/search?query=visibility:published&pageSize=1000
     *
     * @param iiifVersion
     */
    public <T extends IIIFResource> T getGalleryCollection(String iiifVersion) {
        // get the response from Set Api
        try {
            List<? extends UserSet> publishedSets = userSetApiClient.getSearchUserSetApi().searchUserSet("visibility:published", null, null, 1,
                    100, null, 0, null);
        if (StringUtils.equals(iiifVersion, "2")) {
            return (T) collectionV2Generator.generateGalleryRoot(settings.getGalleryRootURI(), publishedSets);
        }
        return (T) collectionV3Generator.generateGalleryRoot(settings.getGalleryRootURI(), publishedSets);
        } catch (SetApiClientException e) {
            // todo handling other responses
            e.printStackTrace();
        }
        return null;
    }

    // TODO set client is not set up for pagination request in getUserset and return object is yet not decided.
    // https://set-api.acceptance.eanadev.org/set/15456?&page=1&pageSize=100&profile=items.meta
    public <T extends IIIFResource> T retrieveGallery(String iiifVersion) {
        // get the response from Set Api
        try {
            UserSet set = userSetApiClient.getWebUserSetApi().getUserSet("1160954", PROFILE_STANDARD);
            if (StringUtils.equals(iiifVersion, "2")) {
                return (T) collectionV2Generator.generateGallery(settings.getGalleryRootURI(), null);
            }
            return (T) collectionV3Generator.generateGallery(settings.getGalleryRootURI(), null);
        } catch (SetApiClientException e) {
            e.printStackTrace();
            // todo handling other responses

        }
        return null;
    }

}
