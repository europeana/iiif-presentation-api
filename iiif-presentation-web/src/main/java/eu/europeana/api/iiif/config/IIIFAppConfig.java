package eu.europeana.api.iiif.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.api.caching.CachingStrategy;
import eu.europeana.api.caching.ChainingCachingStrategy;
import eu.europeana.api.iiif.exceptions.InvalidConfigurationException;
import eu.europeana.api.iiif.generator.CollectionV2Generator;
import eu.europeana.api.iiif.generator.CollectionV3Generator;
import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.service.IIIFJsonHandler;
import eu.europeana.api.iiif.utils.IIIFConstants;
import eu.europeana.api.iiif.v2.io.LanguageValueSerializer;
import eu.europeana.api.iiif.v2.model.LanguageValue;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.exception.SetApiClientException;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@Configuration
public class IIIFAppConfig {

    private static final Logger LOG = LogManager.getLogger(IIIFAppConfig.class);

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");

    @Resource
    private IIIfSettings settings;

    @Bean(name = IIIFConstants.BEAN_MEDIA_TYPES)
    public MediaTypes getMediaTypes() throws IOException {
        String mediaTypeXMLConfigFile = settings.getMediaXMLConfig();

        MediaTypes mediaTypes;
        try (InputStream inputStream = getClass().getResourceAsStream(mediaTypeXMLConfigFile)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                mediaTypes = xmlMapper().readValue(contents, MediaTypes.class);
            }
        }

        if (!mediaTypes.mediaTypeCategories.isEmpty()) {
            mediaTypes.getMap().putAll(mediaTypes.mediaTypeCategories.stream().filter(media -> !media.isEuScreen()).collect(Collectors.toMap(MediaType::getMimeType, e-> e)));
        } else {
            LOG.error("media Categories not configured at startup. mediacategories.xml file not added or is empty");
        }
        return mediaTypes;
    }

    @Bean(IIIFConstants.BEAN_XML_MAPPER)
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setDateFormat(dateFormat);
        return xmlMapper;
    }


    @Bean
    public CachingStrategy getCachingStrategy() {
        return new ChainingCachingStrategy();
    }

    @Bean(name = IIIFConstants.BEAN_IIIF_JSON_HANDLER)
    public IIIFJsonHandler iiifJsonHandler() {
        return new IIIFJsonHandler(v2Mapper(), v3Mapper());
    }

    @Primary
    @Bean(name = IIIFConstants.BEAN_V2_JSON_MAPPER)
    public ObjectMapper v2Mapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        module.addSerializer(LanguageValue.class, new LanguageValueSerializer());

        mapper.registerModule(module);
        mapper.setVisibility(
                mapper.getVisibilityChecker()
                        .withCreatorVisibility(NONE)
                        .withFieldVisibility(NONE)
                        .withGetterVisibility(NONE)
                        .withIsGetterVisibility(NONE)
                        .withSetterVisibility(NONE));


        mapper.findAndRegisterModules();
        return mapper;
    }


    @Bean(name = IIIFConstants.BEAN_V3_JSON_MAPPER)
    public ObjectMapper v3Mapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        mapper.registerModule(module);
        mapper.setVisibility(
                mapper.getVisibilityChecker()
                        .withCreatorVisibility(NONE)
                        .withFieldVisibility(NONE)
                        .withGetterVisibility(NONE)
                        .withIsGetterVisibility(NONE)
                        .withSetterVisibility(NONE));


        mapper.findAndRegisterModules();
        return mapper;
    }

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
            return new UserSetApiClient(settings.getSetApiServiceUri(), null);
        } catch (SetApiClientException e) {
            throw new InvalidConfigurationException(e.getLocalizedMessage());
        }
    }
}
