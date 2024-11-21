/**
 * 
 */
package eu.europeana.api.iiif.v3.io;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public interface JsonConstants {

    public static final String CONTEXT_URI      = "http://iiif.io/api/presentation/3/context.json";
    public static final String CONTEXT_URI_ANNO = "http://www.w3.org/ns/anno.jsonld";
    public static final String CONTEXT_URI_TEXT = "http://iiif.io/api/extension/text-granularity/context.json";

        
    public static final String context           = "@context";

    public static final String Collection        = "Collection";
    public static final String Manifest          = "Manifest";
    public static final String Canvas            = "Canvas";
    public static final String AnnotationPage    = "AnnotationPage";
    public static final String Annotation        = "Annotation";
    public static final String Agent             = "Agent";

    public static final String Dataset           = "Dataset";
    public static final String Image             = "Image";
    public static final String Model             = "Model";
    public static final String Sound             = "Sound";
    public static final String Text              = "Text";
    public static final String Video             = "Video";

    public static final String ImageService3     = "ImageService3";

    public static final String id                = "id";
    public static final String type              = "type";
    public static final String label             = "label";
    public static final String value             = "value";
    public static final String summary           = "summary";
    public static final String metadata          = "metadata";
    public static final String requiredStatement = "requiredStatement";
    public static final String rights            = "rights";
    public static final String provider          = "provider";
    public static final String homepage          = "homepage";
    public static final String seeAlso           = "seeAlso";
    public static final String service           = "service";
    public static final String thumbnail         = "thumbnail";
    public static final String navDate           = "navDate";
    public static final String behavior          = "behavior";
    public static final String viewingDirection  = "viewingDirection";
    public static final String start             = "start";
    public static final String partOf            = "partOf";
    public static final String rendering         = "rendering";
    public static final String placeholderCanvas = "placeholderCanvas";

    public static final String items             = "items";
    public static final String annotations       = "annotations";

    public static final String logo              = "logo";
    public static final String format            = "format";
    public static final String profile           = "profile";
    public static final String width             = "width";
    public static final String height            = "height";
    public static final String duration          = "duration";
    public static final String language          = "language";

    public static final String motivation        = "motivation";
    public static final String body              = "body";
    public static final String target            = "target";
    public static final String timeMode          = "timeMode";

    public static final String textGranularity   = "textGranularity";
    public static final String source            = "source";
}
