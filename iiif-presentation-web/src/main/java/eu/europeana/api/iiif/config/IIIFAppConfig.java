package eu.europeana.api.iiif.config;


import eu.europeana.api.iiif.exceptions.InvalidConfigurationException;
import eu.europeana.api.iiif.generator.CollectionV2Generator;
import eu.europeana.api.iiif.generator.CollectionV3Generator;
import eu.europeana.api.iiif.utils.IIIFConstants;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.exception.SetApiClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Properties;

@Configuration
@PropertySource("classpath:iiif.user.properties")
public class IIIFAppConfig {

    private static final Logger LOG = LogManager.getLogger(IIIFAppConfig.class);

    @Value("${set.service.uri:}")
    private String setApiServiceUri;

    @Value("${set.api.key:}")
    private String setApiKey ;

    @Value("${oauth.service.uri}")
    private String oauthServiceUri;

    @Value("${oauth.token.request.params:}")
    private String oauthTokenRequestParams;

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
        properties.put(ClientConfiguration.PROP_SET_SERVICE_URI, setApiServiceUri);
        properties.put(ClientConfiguration.PROP_SET_API_KEY, setApiKey);
        properties.put(ClientConfiguration.PROP_OAUTH_SERVICE_URI, oauthServiceUri);
        properties.put(ClientConfiguration.PROP_OAUTH_REQUEST_PARAMS, oauthTokenRequestParams);

        return properties;
    }


}
