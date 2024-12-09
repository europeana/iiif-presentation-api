package eu.europeana.api.iiif.v3.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.*;

/**
 * Hash map that stores languages as key and text strings in that language as values
 * The map supports automatically merging existing key and values using the 'put' method
 *
 * @author Patrick Ehlert
 * Created on 24-01-2018
 */
public class LanguageMap extends LinkedHashMap<String, List<String>> {

    private static final long serialVersionUID = -7678917507346373456L;

    public static final String NO_LANGUAGE_KEY = "@none";
    public static final String DEFAULT_METADATA_KEY = "en";

    public LanguageMap() {
        super();
        // empty constructor to allow deserializing
    }

    @JsonCreator
    public LanguageMap(String value) {
        super();
        this.add(NO_LANGUAGE_KEY, value);
    }

    /**
     * Adds language and single value in the map
     *
     * @param language
     * @param value
     */
    public LanguageMap(String language, String value) {
        super(1);
        this.add(language, value);
    }

    /**
     * Adds language and list of values in the map
     *
     * @param language
     * @param values
     */
    public LanguageMap(String language, List<String> values) {
        super(1);
        this.add(language, values);
    }

    /**
     * Adds the multilingual given map
     *
     * @param map
     */
    public LanguageMap(Map<String, List<String>> map) {
        super(map.size());
        for (Map.Entry<String, List<String>> key : map.entrySet()) {
            add(key.getKey(), key.getValue());
        }
    }

    public List<String> add(String key, String value) {
        return add(key, new ArrayList<>(Arrays.asList(value)));
    }

    /**
     * If the provided key isn't in the map yet, then this works as a normal map 'put' operation, otherwise the
     * existing and new values for the key are merged.
     * Also if the key is null or empty or then we use the default '@None' key instead
     *
     * @param key    key to insert
     * @param values values to insert (or merge with existing values if the key already exists)
     */
    // Note that we need to override the base put-method because we need to make sure we check the key and modify it
    // if necessary when deserializing record json data
    public final List<String> add(String key, List<String> values) {
        key = checkKey(key);
        List<String> existingValues = super.get(key);
        if (existingValues != null) {
            existingValues.addAll(values);
        } else {
            existingValues = new ArrayList<>(values);
            super.put(key, existingValues);
        }
        return existingValues;
    }

    /**
     * Merges al key-value pairs from the provided map into this map
     *
     * @param map languagemap to merge into this map
     */
    public void add(LanguageMap map) {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @return textual representation of the contents of the language map (for debugging purposes)
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append('(');
        for (Map.Entry<String, List<String>> entry : this.entrySet()) {
            if (s.length() > 1) {
                s.append(", ");
            }
            s.append('{').append(entry.getKey())
                    .append('=').append(entry.getValue().toString())
                    .append('}');
        }
        s.append(')');
        return s.toString();
    }

    /**
     * Can we use the provided key, or should we use @none?
     *
     * @return key that should be used for storing values
     */
    private String checkKey(String key) {
        return (key == null || key.isEmpty() ? NO_LANGUAGE_KEY : key);
    }
}
