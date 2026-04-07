package eu.europeana.api.iiif.generator.media.v2;

import eu.europeana.api.iiif.generator.ManifestSettings;
import eu.europeana.api.iiif.generator.media.MediaGenerator;
import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.v2.model.Annotation;
import eu.europeana.api.iiif.v2.model.AnnotationBody;
import eu.europeana.api.iiif.v2.model.Canvas;
import eu.europeana.api.iiif.v2.model.Image;
import eu.europeana.api.iiif.v2.model.LanguageValue;
import eu.europeana.api.record.model.Aggregation;
import eu.europeana.api.record.model.WebResource;

import java.util.Collections;

public abstract class AbstractMediaGeneratorV2 implements MediaGenerator<Canvas> {

    protected ManifestSettings settings;

    protected AbstractMediaGeneratorV2(ManifestSettings settings) {
        this.settings = settings;
    }

    /**
     * Adds the technical metadata in the annotation body of the canvas
     * @param canvas Canvas
     * @param body AnnotationBody
     */
    protected void addTechnicalMetadata(Canvas canvas, AnnotationBody body) {
        body.setHeight(canvas.getHeight());
        body.setWidth(canvas.getWidth());
    }

    protected void addCanvasMetadata(Canvas canvas, WebResource wr) {
        canvas.setAttribution(wr.getTextAttributionSnippet());
        canvas.setLicense(wr.getLicense());
    }

    /**
     * Auxiliary method to help create an annotation page with only one annotation
     */
    protected Annotation newContentAnnotation(Canvas canvas) {
        // A canvas has only 1 annotation (image field)
        Annotation anno = new Annotation(null, "sc:painting");
        canvas.setImages(Collections.singletonList(anno));
        anno.setOn(canvas.getID());
        return anno;
    }

    protected void addRendering(WebResource wr, Canvas canvas) {
        if (wr == null) {
            return;
        }
        MediaType mediaType = wr.getMediaType();
        Image renderingImage = new Image(wr.getId());
        renderingImage.setFormat(mediaType.getMimeType());
        String label = mediaType.getLabel();
        if (label != null) {
            renderingImage.setLabel(new LanguageValue(label));
        }
        canvas.getRendering().add(renderingImage);
    }

    protected void handleIsFormatOf(WebResource wr, Canvas canvas) {
        if (!wr.hasIsFormatOf()) {
            return;
        }

        Aggregation aggr = wr.getRecord().getProviderAggregation();
        for ( String isFormatOf : wr.getIsFormatOf() ) {
            if (aggr.getView(isFormatOf) != null) {
                continue;
            }

            addRendering(aggr.getWebResource(isFormatOf), canvas);
        }
    }
}
