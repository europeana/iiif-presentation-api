package eu.europeana.api.iiif.generator.media.v3;

import eu.europeana.api.iiif.generator.ManifestSettings;
import eu.europeana.api.iiif.generator.media.MediaGenerator;
import eu.europeana.api.iiif.media.MediaCategory;
import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.v3.model.Annotation;
import eu.europeana.api.iiif.v3.model.AnnotationPage;
import eu.europeana.api.iiif.v3.model.Canvas;
import eu.europeana.api.iiif.v3.model.ContentResource;
import eu.europeana.api.iiif.v3.model.LabelledValue;
import eu.europeana.api.iiif.v3.model.LanguageMap;
import eu.europeana.api.iiif.v3.model.content.EmbeddableResource;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.iiif.v3.model.content.Model;
import eu.europeana.api.iiif.v3.model.content.Rendering;
import eu.europeana.api.iiif.v3.model.content.Sound;
import eu.europeana.api.iiif.v3.model.content.Text;
import eu.europeana.api.iiif.v3.model.content.Video;
import eu.europeana.api.record.model.Aggregation;
import eu.europeana.api.record.model.Record;
import eu.europeana.api.record.model.WebResource;

import org.apache.commons.lang3.StringUtils;

import static eu.europeana.api.iiif.generator.ManifestGeneratorConstants.*;

public abstract class AbsMediaGeneratorV3 implements  MediaGenerator<Canvas> {

    protected ManifestSettings settings;

    protected AbsMediaGeneratorV3(ManifestSettings settings) {
        this.settings = settings;
    }

    public void addCanvasMetadata(Canvas canvas, WebResource webResource) {
        canvas.setRequiredStatement(createRequiredStatementMap(webResource.getTextAttributionSnippet()));
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
        return switch (mediaCategory) {
            case VIDEO -> new Video(id);
            case SOUND -> new Sound(id);
            case TEXT -> new Text(id);
            case MODEL -> new Model(id);
            case EMBEDDABLE_RESOURCE -> new EmbeddableResource(id);
            default -> new Image(id);
        };
     }

    /**
     * Adds the technical metadata in the annotation body of the canvas
     * @param canvas
     * @param body
     */
    protected void addTechnicalMetadata(Canvas canvas, ContentResource body) {
        if (body instanceof Image img) {
            img.setHeight(canvas.getHeight());
            img.setWidth(canvas.getWidth());
        }

        if (body instanceof Video video) {
            video.setHeight(canvas.getHeight());
            video.setWidth(canvas.getWidth());
            video.setDuration(canvas.getDuration());
        }

        if (body instanceof Sound sound) {
            sound.setDuration(canvas.getDuration());
        }
    }

    protected void addRendering(WebResource wr, Canvas canvas) {
        if ( wr == null ) { return; }

        MediaType mediaType = wr.getMediaType();
        Rendering renderingImage = new Rendering(wr.getId()
                                               , mediaType.getCategory().name());
        renderingImage.setFormat(mediaType.getMimeType());
        String label = mediaType.getLabel();
        if ( label != null ) {
            renderingImage.setLabel(new LanguageMap(LINGUISTIC, label));
        }
        canvas.getRendering().add(renderingImage);
    }

    protected void handleIsFormatOf(WebResource wr, Canvas canvas) {
        if ( !wr.hasIsFormatOf() ) { return; }

        Aggregation aggr = wr.getRecord().getProviderAggregation();
        for ( String isFormatOf : wr.getIsFormatOf() ) {
            if ( aggr.getView(isFormatOf) != null ) { continue; }

            addRendering(aggr.getWebResource(isFormatOf), canvas);
        }
    }
}