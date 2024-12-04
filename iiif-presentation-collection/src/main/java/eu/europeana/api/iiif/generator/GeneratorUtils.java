package eu.europeana.api.iiif.generator;

import eu.europeana.api.iiif.v3.model.LanguageMap;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author Srishti
 * @since 03 December 2024
 */
public class GeneratorUtils implements GeneratorConstants {

    public static String buildUrlWithSetId(String uri, String setId) {
        if (StringUtils.endsWith(uri, "/")) {
            return uri + setId;
        }
        return uri + "/" + setId;
    }

    public static LanguageMap getLanguageMap(Map<String, String> map) {
        LanguageMap languageMap = new LanguageMap();
        for (Map.Entry<String, String> key : map.entrySet()) {
            languageMap.add(key.getKey(), key.getValue());
        }
        return languageMap;
    }
}