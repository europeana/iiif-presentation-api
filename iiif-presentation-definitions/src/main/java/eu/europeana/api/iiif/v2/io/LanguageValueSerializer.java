package eu.europeana.api.iiif.v2.io;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import eu.europeana.api.iiif.v2.model.LanguageValue;

import static eu.europeana.api.iiif.v2.io.JsonConstants.*;

import java.io.IOException;
import java.util.List;

/**
 * @author Hugo
 * @since 12 Sep 2023
 */
public class LanguageValueSerializer extends JsonSerializer<LanguageValue> {

    public static final LanguageValueSerializer INSTANCE = new LanguageValueSerializer();

    @Override
    public void serialize(LanguageValue v, JsonGenerator jgen,
                          SerializerProvider serializers) throws IOException
    {
        if ( !v.hasLang() ) {
            jgen.writeString(v.getValue());
            return;
        }

        jgen.writeStartObject();
        jgen.writeStringField(value, v.getValue());
        jgen.writeStringField(lang, v.getLang());        
        jgen.writeEndObject();
    }
}
