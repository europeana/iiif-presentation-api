/**
 * 
 */
package eu.europeana.api.iiif.v2.io;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public interface JsonConstants {

    public static final String CONTEXT_URI = "http://iiif.io/api/presentation/2/context.json";

    public static final String context     = "@context";

    public static final String Collection  = "sc:Collection";
    public static final String Manifest    = "sc:Manifest";
    public static final String Sequence    = "sc:Sequence";
    public static final String Canvas      = "sc:Canvas";
    public static final String Annotation  = "oa:Annotation";
    
    public static final String Image       = "dctypes:Image";

    public static final String id          = "@id";
    public static final String type        = "@type";
    public static final String lang        = "@language";

    public static final String label       = "label";
    public static final String value       = "value";
    public static final String description = "description";
    public static final String metadata    = "metadata";
    public static final String navDate     = "navDate";
    public static final String viewingHint = "viewingHint";
    public static final String viewingDirection = "viewingDirection";
    public static final String attribution = "attribution";
    public static final String license     = "license";
    public static final String logo        = "logo";
    public static final String related     = "related";
    public static final String within      = "within";
    public static final String rendering   = "rendering";
    public static final String seeAlso     = "seeAlso";
    public static final String thumbnail   = "thumbnail";

    public static final String collections = "collections";
    public static final String manifests   = "manifests";
    public static final String sequences   = "sequences";
    public static final String canvases    = "canvases";
    public static final String images      = "images";
    public static final String otherContent = "otherContent";

    public static final String startCanvas = "startCanvas";
    public static final String motivation  = "motivation";
    public static final String resource    = "resource";
    public static final String on          = "on";

    public static final String format      = "format";
    public static final String profile     = "profile";
    public static final String width       = "width";
    public static final String height      = "height";
    public static final String language    = "language";
    public static final String service     = "service";
}
