package eu.europeana.api.iiif.config;

import eu.europeana.api.commons_sb3.definitions.iiif.IIIFDefinitions;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:iiif.properties")
@PropertySource(value = "classpath:iiif.user.properties", ignoreResourceNotFound = true)
public class IIIfSettings {

    private static final Logger LOG = LogManager.getLogger(IIIfSettings.class);

    @Value("${iiif.api.base.url:}")
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


    public String getIIIFApiBaseUrl() {
        return iiifApiBaseUrl;
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

    public String getSetApiBaseUrl() {
        return setApiBaseUrl;
    }

    public String getGalleryRootUriPath() {
        return galleryRootUriPath;
    }

    public String getGalleryUriPath() {
        return galleryUriPath;
    }

    /**
     * Get the value for Manifest API presentation path
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

    public String getIIIFApiIdPlaceholder() {
        return iiifApiIdPlaceholder;
    }


    /**
     * ex -  https://iiif.europeana.eu/collection
     * @return <IIIF Base Url> + '/collection'
     *
     */
    public String getCollectionRootURI() {
        return iiifApiBaseUrl + galleryRootUriPath;
    }

    /**
     *
     * @return <IIIF Base Url> + '/collection' + '/gallery'
     */
    public String getGalleryRootURI() {
        return iiifApiBaseUrl + galleryRootUriPath + galleryUriPath;
    }

    public String getGalleryLandingPage() {
        return galleryLandingPage;
    }

    /**
     * {iiifApiBaseUrl}/presentation/<DATASET_ID>/<RECORD_ID>/manifest
     * @return
     */
    public String getIIIfManifestUrl() {
        return iiifApiBaseUrl + iiifApiPresentationPath + iiifApiIdPlaceholder + iiifApiManifestPath;
    }
}
