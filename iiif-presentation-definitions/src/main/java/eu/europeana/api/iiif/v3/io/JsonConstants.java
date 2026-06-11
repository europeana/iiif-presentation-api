package eu.europeana.api.iiif.v3.io;

/**
 * Defines constants used for V3 json response elements
 *
 * @author Hugo
 * @since 14 Oct 2024
 */
public final class JsonConstants {

    public static final String CONTEXT_URI = "http://iiif.io/api/presentation/3/context.json";
    public static final String CONTEXT_URI_ANNO = "http://www.w3.org/ns/anno.jsonld";
    public static final String CONTEXT_URI_TEXT = "http://iiif.io/api/extension/text-granularity/context.json";


    public static final String CONTEXT = "@context";

    public static final String COLLECTION = "Collection";
    public static final String MANIFEST = "Manifest";
    public static final String CANVAS = "Canvas";
    public static final String ANNOTATION_PAGE = "AnnotationPage";
    public static final String ANNOTATION = "Annotation";
    public static final String AGENT = "Agent";

    public static final String DATASET = "Dataset";
    public static final String IMAGE = "Image";
    public static final String MODEL = "Model";
    public static final String SOUND = "Sound";
    public static final String TEXT = "Text";
    public static final String VIDEO = "Video";
    public static final String EMBEDDABLE_RESOURCE = "EmbeddableResource";

    public static final String IMAGE_SERVICE_3 = "ImageService3";

    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String LABEL = "label";
    public static final String VALUE = "value";
    public static final String SUMMARY = "summary";
    public static final String METADATA = "metadata";
    public static final String REQUIRED_STATEMENT = "requiredStatement";
    public static final String RIGHTS = "rights";
    public static final String PROVIDER = "provider";
    public static final String HOMEPAGE = "homepage";
    public static final String SEE_ALSO = "seeAlso";
    public static final String SERVICE = "service";
    public static final String THUMBNAIL = "thumbnail";
    public static final String NAV_DATE = "navDate";
    public static final String BEHAVIOR = "behavior";
    public static final String VIEWING_DIRECTION = "viewingDirection";
    public static final String START = "start";
    public static final String PART_OF = "partOf";
    public static final String RENDERING = "rendering";
    public static final String PLACEHOLDER_CANVAS = "placeholderCanvas";

    public static final String ITEMS = "items";
    public static final String ANNOTATIONS = "annotations";

    public static final String LOGO = "logo";
    public static final String FORMAT = "format";
    public static final String PROFILE = "profile";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String DURATION = "duration";
    public static final String LANGUAGE = "language";

    public static final String MOTIVATION = "motivation";
    public static final String BODY = "body";
    public static final String TARGET = "target";
    public static final String TIME_MODE = "timeMode";

    public static final String TEXT_GRANULARITY = "textGranularity";
    public static final String SOURCE = "source";

    private JsonConstants(){}
}
