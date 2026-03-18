package eu.europeana.api.record.serialization;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.util.StdConverter;

import eu.europeana.api.iiif.v3.model.LanguageMap;

public class ConverterUtils {
    
    public static class ToListString extends StdConverter<Object, List<String>> {

        @Override
        public List<String> convert(Object value) {
            return JsonUtils.asListString(value);
        }

    }

    public static class ToString extends StdConverter<Object, String> {

        @Override
        public String convert(Object value) {
            return JsonUtils.asString(value);
        }

    }

    public static class ToLanguageMap extends StdConverter<Object, LanguageMap> {

        @SuppressWarnings("unchecked")
        @Override
        public LanguageMap convert(Object value) {
            LanguageMap langMap = new LanguageMap();
            if ( !(value instanceof Map) ) { return langMap; }

            Map<String,Object> map = (Map<String,Object>)value;
            Object def = map.remove("def");
            for ( String key : map.keySet() ) {
                langMap.add(key, JsonUtils.asListString(map.get(key)));
            }
            if ( def != null ) { 
                langMap.add(LanguageMap.NO_LANGUAGE_KEY
                          , JsonUtils.asListString(def));
            }
            return langMap;
        }

    }
}
