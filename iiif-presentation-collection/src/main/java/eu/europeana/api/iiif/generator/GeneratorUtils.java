/**
 * 
 */
package eu.europeana.api.iiif.generator;

import eu.europeana.api.item.Item;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public class GeneratorUtils implements GeneratorConstants
{
    private static String host = "iiif.europeana.eu";
    private static String host_rnd = "rnd-2.eanadev.org/share/iiif/v3";

    public static String getRootURI() {
        return String.format(rootURI, host_rnd);
    }

    public static String getGalleryRootURI() {
        return String.format(galleryRootURI, host_rnd);
    }

    public static String getGalleryURI(String setId) {
        return String.format(galleryURI, host_rnd, setId);
    }

    public static String getManifestURI(String itemId) {
        return String.format(manifestURI, host, itemId);
    }

    public static String getSetURL(String setId, String format) {
        return String.format(setURL, setId, format);
    }
}