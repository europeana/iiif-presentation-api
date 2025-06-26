package eu.europeana.api.iiif.io;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Serializer class for @context
 * @author Hugo
 * @since 12 Sep 2023
 */
public class ContextSerializer extends JsonSerializer<ResourceContext> {

    @Override
    public void serialize(ResourceContext context, JsonGenerator jgen,
                          SerializerProvider serializers) throws IOException {

        if ( !context.hasBase() ) {

            if ( !context.hasImportURIs() ) {
                return;
            }

            String[] imports = context.getImportURIs();
            if ( imports.length == 1 ) {
                jgen.writeString(imports[0]);
                return;
            }

            jgen.writeStartArray();
            for ( String contextURI : imports) {
                jgen.writeString(contextURI);
            }
            jgen.writeEndArray();
            return;
        }

        if ( !context.hasImportURIs() ) { 
            jgen.writeStartObject();
            jgen.writeStringField("@base", context.getBase());
            jgen.writeEndObject();
            return;
        }

        String[] imports = context.getImportURIs();
        jgen.writeStartArray();
        jgen.writeStartObject();
        jgen.writeStringField("@base", context.getBase());
        jgen.writeEndObject();
        for ( String contextURI : imports) {
            jgen.writeString(contextURI);
        }
        jgen.writeEndArray();
    }
}
