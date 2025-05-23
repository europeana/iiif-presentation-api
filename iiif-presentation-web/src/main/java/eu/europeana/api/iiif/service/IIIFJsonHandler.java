package eu.europeana.api.iiif.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.iiif.io.ClassContextWrapper;
import eu.europeana.api.iiif.io.ResourceContext;
import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.utils.IIIFConstants;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Hugo
 * @since 28 Oct 2024
 * Refractored by Srishti Singh
 */
public class IIIFJsonHandler {

    private static String BASE_CONTEXT = "http://iiif.io/api/presentation/";

    private static Map<String, Class<?>> uri2mapper = new HashMap<>();
    private static Map<Class<?>, ObjectMapper> class2mapper = new HashMap<>();
    private static Map<Class<?>, ResourceContext> class2context = new HashMap<>();

    public IIIFJsonHandler(@Qualifier(value = IIIFConstants.BEAN_V2_JSON_MAPPER) ObjectMapper v2,
                           @Qualifier(value = IIIFConstants.BEAN_V3_JSON_MAPPER) ObjectMapper v3) {
        // VERSION 2 (and 2.1)
        ResourceContext v2col = new ResourceContext(null, eu.europeana.api.iiif.v2.io.JsonConstants.CONTEXT_URI);
        ResourceContext v2man = new ResourceContext(null, eu.europeana.api.iiif.v2.io.JsonConstants.CONTEXT_URI);

        uri2mapper.put(eu.europeana.api.iiif.v2.io.JsonConstants.CONTEXT_URI
                , eu.europeana.api.iiif.v2.model.PresentationResource.class);

        class2mapper.put(eu.europeana.api.iiif.v2.model.Collection.class, v2);
        class2mapper.put(eu.europeana.api.iiif.v2.model.Manifest.class, v2);
        class2mapper.put(eu.europeana.api.iiif.v2.model.Sequence.class, v2);
        class2mapper.put(eu.europeana.api.iiif.v2.model.Canvas.class, v2);
        class2mapper.put(eu.europeana.api.iiif.v2.model.PresentationResource.class, v2);

        class2context.put(eu.europeana.api.iiif.v2.model.Collection.class, v2col);
        class2context.put(eu.europeana.api.iiif.v2.model.Manifest.class, v2man);
        class2context.put(eu.europeana.api.iiif.v2.model.Sequence.class, v2man);
        class2context.put(eu.europeana.api.iiif.v2.model.Canvas.class, v2man);

        //VERSION 3
        ResourceContext v3col = new ResourceContext(null, eu.europeana.api.iiif.v3.io.JsonConstants.CONTEXT_URI);
        ResourceContext v3man = new ResourceContext(null, eu.europeana.api.iiif.v3.io.JsonConstants.CONTEXT_URI
                , eu.europeana.api.iiif.v3.io.JsonConstants.CONTEXT_URI_ANNO
                , eu.europeana.api.iiif.v3.io.JsonConstants.CONTEXT_URI_TEXT);

        uri2mapper.put(eu.europeana.api.iiif.v3.io.JsonConstants.CONTEXT_URI
                , eu.europeana.api.iiif.v3.model.PresentationResource.class);

        class2mapper.put(eu.europeana.api.iiif.v3.model.Collection.class, v3);
        class2mapper.put(eu.europeana.api.iiif.v3.model.Manifest.class, v3);
        class2mapper.put(eu.europeana.api.iiif.v3.model.Canvas.class, v3);
        class2mapper.put(eu.europeana.api.iiif.v3.model.PresentationResource.class, v3);

        class2context.put(eu.europeana.api.iiif.v3.model.Collection.class, v3col);
        class2context.put(eu.europeana.api.iiif.v3.model.Manifest.class, v3man);
        class2context.put(eu.europeana.api.iiif.v3.model.Canvas.class, v3man);
    }

//    public IIIFResource read(InputStream is) throws  IOException {
//        return read(def.readTree(is));
//    }
//
//    public IIIFResource read(Reader reader) throws  IOException {
//        return read(def.readTree(reader));
//    }
//
//    public IIIFResource read(File file) throws IOException {
//        return read(def.readTree(file));
//    }
//
//    public IIIFResource read(URL url) throws IOException {
//        return read(def.readTree(url));
//    }

    public <T extends IIIFResource> T read(InputStream is, Class<T> clazz) throws IOException {
        return class2mapper.get(clazz).readValue(is, clazz);
    }

    public <T extends IIIFResource> T read(Reader reader, Class<T> clazz) throws IOException {
        return class2mapper.get(clazz).readValue(reader, clazz);
    }

    public <T extends IIIFResource> T read(File file, Class<T> clazz) throws IOException {
        return class2mapper.get(clazz).readValue(file, clazz);
    }

    public <T extends IIIFResource> T read(URL url, Class<T> clazz) throws IOException {
        return class2mapper.get(clazz).readValue(url, clazz);
    }

    public <T extends IIIFResource> void write(T obj, OutputStream out) throws IOException {
        if (obj == null) {
            return;
        }
        class2mapper.get(obj.getClass()).writeValue(out, newWrapper(obj));
    }

    public <T extends IIIFResource> void write(T obj, Writer writer) throws IOException {
        if (obj == null) {
            return;
        }
        class2mapper.get(obj.getClass()).writeValue(writer, newWrapper(obj));
    }

    public <T extends IIIFResource> void write(T obj, File file) throws IOException {
        if (obj == null) {
            return;
        }
        class2mapper.get(obj.getClass()).writeValue(file, newWrapper(obj));
    }

    public <T extends IIIFResource> void write(T obj, DataOutput data) throws IOException {
        if (obj == null) {
            return;
        }
        class2mapper.get(obj.getClass()).writeValue(data, newWrapper(obj));
    }

    public <T extends IIIFResource> void write(T obj, JsonGenerator jgen) throws IOException {
        if (obj == null) {
            return;
        }
        class2mapper.get(obj.getClass()).writeValue(jgen, newWrapper(obj));
    }

    protected <T extends IIIFResource> Object newWrapper(T obj) {
        return new ClassContextWrapper<T>(class2context.get(obj.getClass()), obj);
    }

    protected IIIFResource read(JsonNode node) throws IOException {
        Class<IIIFResource> c = getClass(node);
        return class2mapper.get(c).readValue(node.traverse(), c);

    }

    @SuppressWarnings("unchecked")
    private <T extends IIIFResource> Class<T> getClass(JsonNode node) {
        if (!node.isObject()) {
            return null;
        }

        String context = getContextURI(node.findValue("@context"));
        return (Class<T>) uri2mapper.get(context);
    }

    private String getContextURI(JsonNode node) {
        if (node.isTextual()) {
            String str = node.asText();
            return (str.startsWith(BASE_CONTEXT) ? str : null);
        }

        if (node.isArray()) {
            Iterator<JsonNode> iter = node.elements();
            while (iter.hasNext()) {
                String context = getContextURI(iter.next());
                if (context != null) {
                    return context;
                }
            }
        }

        return null;
    }
}
