package eu.europeana.api.iiif.generator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:iiif.properties")
@PropertySource(value = "classpath:iiif.user.properties", ignoreResourceNotFound = true)
public class GeneratorSettings {

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

    public String getSetApiBaseUrl() {
        return setApiBaseUrl;
    }

    public String getGalleryRootUriPath() {
        return galleryRootUriPath;
    }

    public String getGalleryUriPath() {
        return galleryUriPath;
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
