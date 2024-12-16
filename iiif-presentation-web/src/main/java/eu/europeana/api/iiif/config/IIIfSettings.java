package eu.europeana.api.iiif.config;

import eu.europeana.api.commons_sb3.definitions.iiif.IIIFDefinitions;
import eu.europeana.api.iiif.model.ManifestDefinitions;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static eu.europeana.api.iiif.model.ManifestDefinitions.getFulltextSummaryPath;

@Configuration
@PropertySource("classpath:iiif.properties")
@PropertySource(value = "classpath:iiif.user.properties", ignoreResourceNotFound = true)
public class IIIfSettings {

    private static final Logger LOG = LogManager.getLogger(IIIfSettings.class);

    @Value("${iiif-api.base.url:}")
    private String iiifApiBaseUrl;

    @Value("${iiif-api.presentation.path:}")
    private String iiifApiPresentationPath;

    @Value("${iiif-api.manifest.path:}")
    private String iiifApiManifestPath;

    @Value("${iiif-api.id.placeholder:}")
    private String iiifApiIdPlaceholder;

    @Value("${set.api.base.url:}")
    private String setApiBaseUrl;

    @Value("${gallery.root.uri.path:}")
    private String galleryRootUriPath;

    @Value("${gallery.uri.path:}")
    private String galleryUriPath;

    @Value("${gallery.landing.page:}")
    private String galleryLandingPage;

    @Value("${set.service.uri:}")
    private String setApiServiceUri;

    @Value("${set.api.key:}")
    private String setApiKey ;

    @Value("${oauth.service.uri}")
    private String oauthServiceUri;

    @Value("${oauth.token.request.params:}")
    private String oauthTokenRequestParams;

    @Value("${content-search-api.baseurl}")
    private String contentSearchBaseUrl;

    @Value("${fulltext-api.baseurl}")
    private String fullTextApiBaseUrl;

    @Value("${record-api.baseurl}")
    private String recordApiBaseUrl;

    @Value("${record-api.path}")
    private String recordApiPath;

    @Value("${thumbnail-api.baseurl}")
    private String thumbnailApiBaseUrl;

    @Value("${thumbnail-api.path}")
    private String thumbnailApiPath;

    @Value("${suppress-parse-exception}")
    private final Boolean suppressParseException = Boolean.FALSE; // default value if we run this outside of Spring (i.e. JUnit)

    @Value("${media.config}")
    private String mediaXMLConfig;

    public String getMediaXMLConfig() {
        return mediaXMLConfig;
    }

    /**
     * Get the value for IIIF API base URL
     * @return if defined, returns the value from iiif.properties
     * If not, returns the value for IIIF Europeana Base URL from IIIFDefinitions
     * if that's not found, returns the value for fulltext Base URL defined in iiif.properties
     */
    public String getIIIFApiBaseUrl() {
        if (StringUtils.isNotBlank(iiifApiBaseUrl)){
            LOG.debug("Using Manifest base URL from iiif.properties: {}", iiifApiBaseUrl);
            return iiifApiBaseUrl;
        } else if (StringUtils.isNotBlank(IIIFDefinitions.IIIF_EUROPENA_BASE_URL)){
            LOG.debug("Using IIIFDefinitions value for IIIF base URL: {}", IIIFDefinitions.IIIF_EUROPENA_BASE_URL);
            return IIIFDefinitions.IIIF_EUROPENA_BASE_URL;
        } else if (StringUtils.isNotBlank(fullTextApiBaseUrl)){
            LOG.warn("Falling back to Fulltext API base URL {} in iiif.properties for IIIF API base URL", fullTextApiBaseUrl);
            return fullTextApiBaseUrl;
        } else {
            LOG.error("No value found for Manifest base URL, Fulltext base URL or IIIFDefinitions.IIIF_EUROPENA_BASE_URL!");
            return null;
        }
    }

    /**
     * Get the value for IIIF API presentation path
     * @return the value from iiif.properties
     * If not defined, use the value from IIIFDefinitions
     */
    public String getIIIFApiPresentationPath() {
        if (StringUtils.isNotBlank(iiifApiPresentationPath)){
            LOG.debug("Using Presentation path found in iiif.properties: {}", iiifApiPresentationPath);
            return iiifApiPresentationPath;
        } else if (StringUtils.isNotBlank(IIIFDefinitions.PRESENTATION_PATH)){
            LOG.debug("Using Presentation path from IIIFDefinitions: {}", IIIFDefinitions.PRESENTATION_PATH);
            return IIIFDefinitions.PRESENTATION_PATH;
        } else {
            LOG.error("No value for presentation path found in iiif.properties or IIIFDefinitions!");
            return null;
        }
    }

    /**
     * Get the value for ID PLACEHOLDER
     * @return the value from iiif.properties
     * If not defined, return the hard-coded value in ManifestDefinitions
     */
    public String getIIIFApiIdPlaceholder() {
        if (StringUtils.isNotBlank(iiifApiIdPlaceholder)){
            LOG.debug("Using ID PLACEHOLDER from iiif.properties: {}", iiifApiIdPlaceholder);
            return iiifApiIdPlaceholder;
        } else if (StringUtils.isNotBlank(ManifestDefinitions.ID_PLACEHOLDER)){
            LOG.debug("Using ID PLACEHOLDER hard-coded in ManifestDefinitions: {}", ManifestDefinitions.ID_PLACEHOLDER);
            return IIIFDefinitions.PRESENTATION_PATH;
        } else {
            LOG.error("No value found for ID_PLACEHOLDER!");
            return null;
        }
    }

    /**
     * Get the value for Content Search Base URL
     * @return the value from iiif.properties
     * If not defined, return the Fulltext Base URL from iiif.properties
     * TODO check if this is the right order, or if we could also fallback to Manifest API Base URL?
     */
    public String getContentSearchBaseUrl() {
        if (StringUtils.isNotBlank(contentSearchBaseUrl)){
            LOG.debug("Using Content Search Base URL from iiif.properties: {}", contentSearchBaseUrl);
            return contentSearchBaseUrl;
        } else if (StringUtils.isNotBlank(fullTextApiBaseUrl)){
            LOG.debug("Using Fulltext Base URL for Context Search Base URL: {}", fullTextApiBaseUrl);
            return fullTextApiBaseUrl;
        } else {
            LOG.error("No value found for Content Search Base URL!");
            return null;
        }
    }

    /**
     * @return Fulltext Base URL defined in iiif.properties from where we can do a HEAD request to check if a full-text is available
     */
    public String getFullTextApiBaseUrl() {
        return fullTextApiBaseUrl;
    }

    /**
     * @return Record API Base URL from where we should retrieve record json data
     */
    public String getRecordApiBaseUrl() {
        return recordApiBaseUrl;
    }

    /**
     * @return Record API resource path (should be appended to the record API base url)
     */
    public String getRecordApiPath() {
        return recordApiPath;
    }

    /**
     * @return Record API endpoint: {recordApiBaseUrl} + {recordApiPath}
     */
    public String getRecordApiEndpoint() {
        return getRecordApiBaseUrl() + getRecordApiPath();
    }

    /**
     * @return Thumbnail url, concatenates base URL + path to endpoint; used to create canvas thumbnails
     */
    public String getThumbnailApiUrl() {
        return thumbnailApiBaseUrl + thumbnailApiPath;
    }

    /**
     * For production we want to suppress exceptions that arise from parsing record data, but for testing/debugging we
     * want to see those exceptions
     */
    public Boolean getSuppressParseException() {
        return suppressParseException;
    }

    /**
     * Base URL used for generation the various types of IDs
     *
     * @return  {iiifApiBaseUrl}/presentation/<DATASET_ID>/<RECORD_ID>
     */
    public String getIIIFPresentationBaseUrl(){
        return getIIIFApiBaseUrl() + getIIIFApiPresentationPath() + getIIIFApiIdPlaceholder();
    }

    /**
     * Url of all manifest IDs but with placeholder for the actual dataset and record ID
     *
     * @return   {iiifApiBaseUrl}/presentation/<DATASET_ID>/<RECORD_ID>/manifest
     */
    public String getManifestIdTemplate() {
        return getIIIFPresentationBaseUrl() + iiifApiManifestPath;
    }

    /**
     * Url of a sequence IDs but with placeholder for the actual dataset and record ID. Note that there the order number
     * is not included here (so first sequence should be /sequence/s1)
     */
    public String getSequenceIDTemplate() {
        return getIIIFPresentationBaseUrl() + "/sequence/s";
    }

    /**
     * Url for canvas IDs but with placeholder for the actual dataset and record ID Note that there the order number is
     * not included here (so first canvas should be /canvas/p1)
     */
    public String getCanvasIDTemplate(){
        return getIIIFPresentationBaseUrl() + "/canvas/p";
    }

    /**
     * Create the IIIF manifest ID
     *
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading
     *                    slash and not trailing slash)
     * @return string containing the IIIF manifest ID
     */
    public String getManifestId(String europeanaId) {
        return getManifestIdTemplate().replace(getIIIFApiIdPlaceholder(), europeanaId);
    }

    /**
     * Create a canvas ID
     * replace <DATASET_ID>/<RECORD_ID> in the given url pattern with europeana Id
     *               -   {iiifApiBaseUrl}/presentation/<DATASET_ID>/<RECORD_ID>/manifest
     *
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading
     *                    slash and not trailing slash)
     * @param order       number
     * @return String containing the canvas ID
     */
    public String getCanvasId(String europeanaId, int order) {
        return getCanvasIDTemplate().replace(getIIIFApiIdPlaceholder(), europeanaId).concat(
                Integer.toString(order));
    }

    /**
     * Create a dataset ID (datasets information are part of the manifest)
     *
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading
     *                    slash and not trailing slash)
     * @return string containing the dataset ID consisting of a base url, Europeana ID and postfix (rdf/xml, json or
     * json-ld)
     */
    public String getDatasetId(String europeanaId, String postFix) {
        return getRecordApiEndpoint() + europeanaId + postFix;
    }

    /**
     * Get the Content Search URL used in the Manifest Service Description
     * @param europeanaId
     * @return URL built from Content search base URL, manifest API presentation path, Europeana ID and Fulltext search path
     */
    public String getContentSearchURL(String europeanaId){
        return getContentSearchBaseUrl() + getIIIFApiPresentationPath() + europeanaId + IIIFDefinitions.FULLTEXT_SEARCH_PATH;
    }


    /**
     * Note: this does not work when running the exploded build from the IDE because the values in the build.properties
     * are substituted only in the .war file. It returns 'default' in that case.
     * @return String containing app version, used in the eTag SHA hash generation
     */
    public String getAppVersion() {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/build.properties");
        if (resourceAsStream == null) {
            return "no version set";
        }
        try {
            Properties buildProperties = new Properties();
            buildProperties.load(resourceAsStream);
            return buildProperties.getProperty("info.app.version");
        } catch (IOException | RuntimeException e) {
            LOG.warn("Error reading version from build.properties file", e);
            return "no version set";
        }
    }

    public String getSetApiServiceUri() {
        return setApiServiceUri;
    }

    public String getSetApiKey() {
        return setApiKey;
    }

    public String getOauthServiceUri() {
        return oauthServiceUri;
    }

    public String getOauthTokenRequestParams() {
        return oauthTokenRequestParams;
    }


    @PostConstruct
    private void logImportantSettings() {
        LOG.info("Manifest settings:");
        if (StringUtils.isNotBlank(iiifApiBaseUrl)){
            LOG.info("  IIIF API Base Url set to {} ", iiifApiBaseUrl);
        }
        if (StringUtils.isNotBlank(iiifApiPresentationPath)){
            LOG.info("  IIIF API presentation path set to {} ", iiifApiPresentationPath);
        }
        if (StringUtils.isNotBlank(iiifApiIdPlaceholder)){
            LOG.info("  IIIF API ID placeholder set to {} ", iiifApiIdPlaceholder);
        }
        if (StringUtils.isNotBlank(contentSearchBaseUrl)){
            LOG.info(" Content Search API base URL set to {} ", contentSearchBaseUrl);
        }
        LOG.info("  Record API endpoint = {} ", getRecordApiEndpoint());
        LOG.info("  Thumbnail API Url = {} ", this.getThumbnailApiUrl());
        LOG.info("  Full-Text Summary Url = {}{} ", this.getFullTextApiBaseUrl(), getFulltextSummaryPath("/<collectionId>/<itemId>"));
        LOG.info("  Suppress parse exceptions = {}", this.getSuppressParseException());
    }

}
