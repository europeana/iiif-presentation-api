package eu.europeana.api.iiif.config;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static eu.europeana.api.iiif.utils.IIIFConstants.BEAN_FALLBACK_AUTHORIZATION;
import static eu.europeana.api.iiif.utils.IIIFConstants.BEAN_IIIF_JSON_HANDLER;
import static eu.europeana.api.iiif.utils.IIIFConstants.BEAN_IIIF_VERSION_SUPPORT;
import static eu.europeana.api.iiif.utils.IIIFConstants.BEAN_MEDIA_TYPES;
import static eu.europeana.api.iiif.utils.IIIFConstants.BEAN_USER_SET_API_CLIENT;
import static eu.europeana.api.iiif.utils.IIIFConstants.BEAN_V2_JSON_MAPPER;
import static eu.europeana.api.iiif.utils.IIIFConstants.BEAN_V3_JSON_MAPPER;
import static eu.europeana.api.iiif.utils.IIIFConstants.BEAN_XML_MAPPER;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.api.commons_sb3.auth.AuthenticationBuilder;
import eu.europeana.api.commons_sb3.auth.AuthenticationConfig;
import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.error.config.ErrorConfig;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidConfigurationException;
import eu.europeana.api.commons_sb3.error.i18n.I18nService;
import eu.europeana.api.commons_sb3.error.i18n.I18nServiceImpl;
import eu.europeana.api.iiif.generator.CollectionSettings;
import eu.europeana.api.iiif.generator.CollectionV2Generator;
import eu.europeana.api.iiif.generator.CollectionV3Generator;
import eu.europeana.api.iiif.generator.EdmManifestMappingV2;
import eu.europeana.api.iiif.generator.EdmManifestMappingV3;
import eu.europeana.api.iiif.generator.ManifestSettings;
import eu.europeana.api.iiif.generator.media.MediaGeneratorRegistry;
import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.media.MediaTypeCatalog;
import eu.europeana.api.iiif.service.IIIFJsonHandler;
import eu.europeana.api.iiif.service.IIIFVersionSupport;
import eu.europeana.api.iiif.service.IIIFVersionSupportHandler;
import eu.europeana.api.iiif.v2.io.LanguageValueSerializer;
import eu.europeana.api.iiif.v2.model.LanguageValue;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.client.exception.SetApiClientException;
import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class IIIFAppConfig {
    private static final Logger LOG = LogManager.getLogger(IIIFAppConfig.class);
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");
    @Resource
    private ManifestSettings settings;
    @Resource
    private CollectionSettings colSettings;
    @Resource
    private MediaGeneratorRegistry registry;
    @Bean(name = BEAN_MEDIA_TYPES)
    public MediaTypeCatalog getMediaTypes() throws IOException {
        String mediaTypeXMLConfigFile = settings.getMediaXMLConfig();
        MediaTypeCatalog mediaTypes;
        try (InputStream is = getClass().getResourceAsStream(mediaTypeXMLConfigFile)) {
            assert is != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String contents = reader.lines().collect(
                        Collectors.joining(System.lineSeparator()));
                mediaTypes = xmlMapper().readValue(contents, MediaTypeCatalog.class);
            }
        }
        if (!mediaTypes.mediaTypeCategories.isEmpty()) {
            mediaTypes.getMap().putAll(mediaTypes.mediaTypeCategories.stream().collect(Collectors.toMap(MediaType::getMimeType, e-> e)));
        } else {
            LOG.error("media Categories not configured at startup. " +mediaTypeXMLConfigFile+" file not added or is empty");
        }
        return mediaTypes;
    }

    @Bean(BEAN_XML_MAPPER)
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setDateFormat(dateFormat);
        return xmlMapper;
    }
    @Bean(name = BEAN_IIIF_JSON_HANDLER)
    public IIIFJsonHandler iiifJsonHandler() {
        return new IIIFJsonHandler(v2Mapper(), v3Mapper());
    }

    @Bean(name = BEAN_FALLBACK_AUTHORIZATION)
    public AuthenticationHandler getFallbackAuthorization() {
        if (StringUtils.isNotEmpty(settings.getKeycloakTokenEndpoint()) && StringUtils.isNotEmpty(settings.getIIIFGrantParams())) {
            AuthenticationConfig config = new AuthenticationConfig(settings.getKeycloakTokenEndpoint(), settings.getIIIFGrantParams());
            return AuthenticationBuilder.newAuthentication(config);
        } else {
            LOG.error("Keycloak token endpoint and IIIF grant params not set !!!");
        }
        return null;
    }

    @Primary
    @Bean(name = BEAN_V2_JSON_MAPPER)
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


    @Bean(name = BEAN_V3_JSON_MAPPER)
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

    @Bean(name = BEAN_IIIF_VERSION_SUPPORT)
    public IIIFVersionSupportHandler versionSupportHandler(
            @Qualifier(value = BEAN_MEDIA_TYPES) MediaTypeCatalog mediaTypes) {
        IIIFVersionSupportHandler handler = new IIIFVersionSupportHandler();
        handler.register(
                new IIIFVersionSupport(
                        eu.europeana.api.iiif.v2.io.JsonConstants.CONTEXT_URI
                      , "2"
                      , new EdmManifestMappingV2(settings, mediaTypes,registry)
                      , new CollectionV2Generator(colSettings)));

        handler.register(
                new IIIFVersionSupport(
                        eu.europeana.api.iiif.v3.io.JsonConstants.CONTEXT_URI
                      , "3"
                      , new EdmManifestMappingV3(settings, mediaTypes,registry)
                      , new CollectionV3Generator(colSettings)));
        return handler;
    }

    @Bean(name = BEAN_USER_SET_API_CLIENT)
    public UserSetApiClient userSetApiClient() throws InvalidConfigurationException {
        try {
            return new UserSetApiClient(settings.getSetApiServiceUri(), null);
        } catch (SetApiClientException e) {
            throw new InvalidConfigurationException("Set API endpoint not provided !!!");
        }
    }

    @Bean(name = ErrorConfig.BEAN_I18nService)
    public I18nService getI18nService() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(ErrorConfig.COMMON_MESSAGE_SOURCE);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        return new I18nServiceImpl(messageSource);
    }
}