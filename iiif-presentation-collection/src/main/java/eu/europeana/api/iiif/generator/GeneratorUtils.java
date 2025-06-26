package eu.europeana.api.iiif.generator;

import eu.europeana.api.iiif.v3.model.LanguageMap;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Utils class for generating Collections
 * @author Srishti
 * @since 03 December 2024
 */
public class GeneratorUtils implements GeneratorConstants {

    private GeneratorUtils() {
        //private to hide implicit one
    }

    /**
     * Builds the url with setId : <url>/<setId>
     * @param uri url
     * @param setId set id
     * @return
     */
    public static String buildUrlWithSetId(String uri, String setId) {
        if (StringUtils.endsWith(uri, "/")) {
            return uri + setId;
        }
        return uri + "/" + setId;
    }

    /**
     * Creates the Language Map from a map
     * @param map map to create LanguageMap
     * @return LanguageMap
     */
    public static LanguageMap getLanguageMap(Map<String, String> map) {
        LanguageMap languageMap = new LanguageMap();
        for (Map.Entry<String, String> key : map.entrySet()) {
            languageMap.add(key.getKey(), key.getValue());
        }
        return languageMap;
    }
}
