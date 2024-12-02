/**
 * 
 */
package eu.europeana.api.iiif.generator;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public class GeneratorUtils implements GeneratorConstants {

    public static String buildGalleryUrl(String galleryRootUri, String setId) {
        if (StringUtils.endsWith(galleryRootUri, "/")) {
            return galleryRootUri + setId;
        }
        return galleryRootUri + "/" + setId;
    }

    public static String getRootURI(String iiifBaseUrl) {
        return String.format(rootURI, iiifBaseUrl);
    }

    public static String getGalleryRootURI(String iiifBaseUrl) {
        return String.format(galleryRootURI, iiifBaseUrl);
    }

    public static String getGalleryURI(String iiifBaseUrl, String setId) {
        return String.format(galleryURI, iiifBaseUrl, setId);
    }

    public static String getManifestURI(String iiifBaseUrl, String itemId) {
        return String.format(manifestURI, iiifBaseUrl, itemId);
    }

    public static String getSetURL(String setId, String format) {
        return String.format(setURL, setId, format);
    }
}