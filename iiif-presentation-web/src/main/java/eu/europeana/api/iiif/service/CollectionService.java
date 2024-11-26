package eu.europeana.api.iiif.service;

import com.jayway.jsonpath.JsonPath;
import eu.europeana.api.iiif.generator.CollectionV2Generator;
import eu.europeana.api.iiif.generator.CollectionV3Generator;
import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.utils.IIIFConstants;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.impl.BaseUserSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CollectionService {

    private static final Logger LOGGER = LogManager.getLogger(CollectionService.class);

    private final UserSetApiClient userSetApiClient;
    private final CollectionV2Generator collectionV2Generator;
    private final CollectionV3Generator collectionV3Generator;

    public CollectionService(@Qualifier(IIIFConstants.BEAN_USER_SET_API_CLIENT) UserSetApiClient userSetApiClient,
                             @Qualifier(IIIFConstants.BEAN_COLLECTION_V2_GENERATOR) CollectionV2Generator collectionV2Generator,
                             @Qualifier(IIIFConstants.BEAN_COLLECTION_V3_GENERATOR) CollectionV3Generator collectionV3Generator) {
        this.userSetApiClient = userSetApiClient;
        this.collectionV2Generator = collectionV2Generator;
        this.collectionV3Generator = collectionV3Generator;
    }


    public <T extends IIIFResource> T retrieveCollection(String version) {
        if (StringUtils.equals(version, "2")) {
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
        // get the response from Set Api
        ResponseEntity<String> publishedSets = userSetApiClient.getSearchUserSetApi().searchUserSet("visibility:published", null, null, 0,
                1000, null, 0, "standard");

        // parse the response
        List<Map> items = JsonPath.read(publishedSets.getBody(), "$.items[*]");
        List<BaseUserSet> sets = new ArrayList<>(items.size());
        for (Map item : items) {
            String id = (String) item.get("id");
            BaseUserSet set = null;
            set.setTitle((Map) item.get("title"));
            set.setDescription((Map) item.get("description"));
            copyItems((List) item.get("items"), set);
            sets.add(set);
        }
        if (StringUtils.equals(iiifVersion, "2")) {
            return (T) collectionV2Generator.generateGalleryRoot(sets);
        }
        return (T) collectionV3Generator.generateGalleryRoot(sets);
    }

    protected void copyItems(List list, UserSet set) {
        for ( Object item : list ) {
            if ( item instanceof String ) {
                set.getItems().add((String)item);
            }
        }
    }

    public void retrieveGallery(String version) {

    }


}
