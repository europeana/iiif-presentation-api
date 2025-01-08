package eu.europeana.api.iiif.utils;

import eu.europeana.api.iiif.v2.model.LanguageValue;
import eu.europeana.api.iiif.v3.model.LanguageMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for processing language maps or language objects;
 * @author Patrick Ehlert
 * Created on 27-06-2019
 */
public final class LanguageMapUtils {

    private LanguageMapUtils() {
        // empty constructor to prevent initialization
    }

    /**
     * This merges an array of languagemaps into a single languagemap. We also check for empty maps and return null if
     * the provided array is empty
     */
    public static LanguageMap mergeLanguageMaps(LanguageMap[] maps) {
        if (maps == null || maps.length == 0) {
            return null;
        } else if (maps.length == 1) {
            return maps[0];
        }
        LanguageMap result = new LanguageMap();
        for (LanguageMap map : maps) {
            result.put(map);
        }
        return result;
    }

    /**
     * This converts a LanguageMap array (v3) to a LanguageObject array (v2).
     * @param map language map to change into language object
     * @return array of language objects
     */
    public static LanguageValue langMapToObjects(LanguageMap map) {
        if (map == null) {
            return new LanguageValue();
        }
        List<LanguageValue> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String language = entry.getKey();
            List<String> values = entry.getValue();
            for (String value: values) {
                result.add(new LanguageValue(language, value));
            }
        }
        if (result.isEmpty()) {
            return new LanguageValue();
        }
        return result.get(0);
    }
}
