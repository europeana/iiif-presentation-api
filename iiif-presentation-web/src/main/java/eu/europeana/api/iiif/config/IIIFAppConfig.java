package eu.europeana.api.iiif.config;

import eu.europeana.api.iiif.exceptions.InvalidConfigurationException;
import eu.europeana.api.iiif.generator.CollectionV2Generator;
import eu.europeana.api.iiif.generator.CollectionV3Generator;
import eu.europeana.api.iiif.utils.IIIFConstants;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.exception.SetApiClientException;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class IIIFAppConfig {

    private static final Logger LOG = LogManager.getLogger(IIIFAppConfig.class);

    @Resource
    private IIIfSettings settings;

    @Bean(name = IIIFConstants.BEAN_COLLECTION_V2_GENERATOR)
    public CollectionV2Generator collectionV2Generator() {
        return new CollectionV2Generator();
    }

    @Bean(name = IIIFConstants.BEAN_COLLECTION_V3_GENERATOR)
    public CollectionV3Generator collectionV3Generator() {
        return new CollectionV3Generator();
    }

    @Bean(name = IIIFConstants.BEAN_USER_SET_API_CLIENT)
    public UserSetApiClient userSetApiClient() throws InvalidConfigurationException {
        try {
            return new UserSetApiClient(new ClientConfiguration(loadProperties()));
        } catch (SetApiClientException e) {
            throw new InvalidConfigurationException(e.getLocalizedMessage());
        }
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        properties.put(ClientConfiguration.PROP_SET_SERVICE_URI, settings.getSetApiServiceUri());
        properties.put(ClientConfiguration.PROP_SET_API_KEY, settings.getSetApiKey());
        properties.put(ClientConfiguration.PROP_OAUTH_SERVICE_URI, settings.getOauthServiceUri());
        properties.put(ClientConfiguration.PROP_OAUTH_REQUEST_PARAMS, settings.getOauthTokenRequestParams());

        return properties;
    }
}
