package eu.europeana.api.iiif.generator.utils;

import static eu.europeana.api.iiif.generator.GeneratorConstants.ATTRIBUTION_STRING;

import org.apache.commons.lang3.StringUtils;

import eu.europeana.api.iiif.media.MediaCategory;
import eu.europeana.api.iiif.v3.model.Annotation;
import eu.europeana.api.iiif.v3.model.AnnotationPage;
import eu.europeana.api.iiif.v3.model.Canvas;
import eu.europeana.api.iiif.v3.model.ContentResource;
import eu.europeana.api.iiif.v3.model.LabelledValue;
import eu.europeana.api.iiif.v3.model.LanguageMap;
import eu.europeana.api.iiif.v3.model.content.EmbeddableResource;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.iiif.v3.model.content.Model;
import eu.europeana.api.iiif.v3.model.content.Sound;
import eu.europeana.api.iiif.v3.model.content.Text;
import eu.europeana.api.iiif.v3.model.content.Video;
import eu.europeana.api.record.model.RecordConstants;
import eu.europeana.api.record.model.WebResource;

public abstract class AbsMediaGeneratorV3 implements RecordConstants
                                                   , MediaGenerator<Canvas> {

	protected void addCanvasMetadata(Canvas canvas, WebResource webResource) {
        canvas.setRequiredStatement(createRequiredStatementMap(webResource.getAttributionText()));
        canvas.setRights(createLicense(webResource.getLicense()));
	}

    protected Text createLicense(String license) {
        return ( license == null ? null 
                                 : new Text(license, null, "text/html") );
    }

    protected LabelledValue createRequiredStatementMap(String attribution){
        if (StringUtils.isEmpty(attribution)) {
            return null;
        }
        return new LabelledValue(new LanguageMap(LanguageMap.DEFAULT_METADATA_KEY
                                               , ATTRIBUTION_STRING)
                               , new LanguageMap(LanguageMap.DEFAULT_METADATA_KEY
                                               , attribution));
    }

    /**
     * Auxiliary method to help create an annotation page with only one annotation
     */
    protected Annotation newContentAnnotation(Canvas canvas) {
        // a canvas has 1 annotation page by default 
        // Note: An extra annotation page is added later 
        // when there is a full text available)
        AnnotationPage annoPage = new AnnotationPage(null); // id is not really necessary in this case
        canvas.getItems().add(annoPage);

        // A canvas has only 1 annotation (image field)
        Annotation anno = new Annotation(null);
        annoPage.getItems().add(anno);
        anno.setTarget(canvas.getID());

        return anno;
    }

    protected ContentResource getAnnotationBody(String id, MediaCategory mediaCategory) {
        switch (mediaCategory) {
            case Image: return new Image(id);
            case Video: return new Video(id);
            case Sound: return new Sound(id);
            case Text : return new Text(id);
            case Model: return new Model(id);
            case EmbeddableResource: return new EmbeddableResource(id);
            default: return new Image(id);
        }
     }

    /**
     * Adds the technical metadata in the annotation body of the canvas
     * @param canvas
     * @param body
     */
    protected void addTechnicalMetadata(Canvas canvas, ContentResource body) {
        if (body instanceof Image) {
            ((Image)body).setHeight(canvas.getHeight());
            ((Image)body).setWidth(canvas.getWidth());
        }
        if (body instanceof Video) {
            ((Video)body).setHeight(canvas.getHeight());
            ((Video)body).setWidth(canvas.getWidth());
            ((Video)body).setDuration(canvas.getDuration());
        }
        if (body instanceof Sound) {
            ((Sound)body).setDuration(canvas.getDuration());
        }
    }
}
