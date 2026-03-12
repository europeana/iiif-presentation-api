package eu.europeana.api.iiif.generator.utils.v2;

import eu.europeana.api.iiif.generator.ManifestGeneratorUtils;
import eu.europeana.api.iiif.generator.utils.MediaGenerator;
import eu.europeana.api.iiif.v2.model.Annotation;
import eu.europeana.api.iiif.v2.model.AnnotationBody;
import eu.europeana.api.iiif.v2.model.Canvas;
import eu.europeana.api.record.model.WebResource;
import java.util.Collections;

public abstract class AbsMediaGeneratorV2 implements MediaGenerator<Canvas> {
  protected ManifestGeneratorUtils utils;
  protected AbsMediaGeneratorV2(ManifestGeneratorUtils utils) {
    this.utils = utils;
  }
  /**
   * Adds the technical metadata in the annotation body of the canvas
   * @param canvas
   * @param body
   */
  protected void addTechnicalMetadata(Canvas canvas, AnnotationBody body) {
    body.setHeight(canvas.getHeight());
    body.setWidth(canvas.getWidth());
  }

  protected void addCanvasMetadata(Canvas canvas, WebResource wr) {
    canvas.setAttribution(wr.getAttributionText());
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
}