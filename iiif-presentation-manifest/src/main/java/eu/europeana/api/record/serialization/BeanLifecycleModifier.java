package eu.europeana.api.record.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

public class BeanLifecycleModifier extends BeanDeserializerModifier {

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config
    		                                    , BeanDescription beanDesc
    		                                    , JsonDeserializer<?> deserializer) {
        if (deserializer instanceof BeanDeserializer) {
            return new BeanLifecycleDeserializer((BeanDeserializer) deserializer);
        }

        return deserializer;
    }

    @SuppressWarnings("serial")
	public static class BeanLifecycleDeserializer extends BeanDeserializer { 

        public BeanLifecycleDeserializer(BeanDeserializerBase src) {
            super(src);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Object obj = super.deserialize(p, ctxt);
            if ( obj instanceof SerializationLifecycle ) {
                ((SerializationLifecycle)obj).postDeserialize();
            }
            return obj;
        }
    }
}
