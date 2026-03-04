package eu.europeana.api.iiif.generator.utils;

import java.util.Collection;

import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.v3.model.Annotation;
import eu.europeana.api.iiif.v3.model.Canvas;
import eu.europeana.api.iiif.v3.model.ContentResource;
import eu.europeana.api.iiif.v3.model.LanguageMap;
import eu.europeana.api.iiif.v3.model.Service;
import eu.europeana.api.iiif.v3.model.TimeMode;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.record.model.SvcsService;
import eu.europeana.api.record.model.WebResource;

import static eu.europeana.api.iiif.generator.GeneratorUtils.*;
import static eu.europeana.api.iiif.generator.GeneratorConstants.*;



/*

Example:
{
  "id": "https://iiif.europeana.eu/presentation/08539/animalia_galerija_php_id_PMSL_DBIP_Media_00249/canvas/p1",
  "type": "Canvas",
  ...
  "items": [
    {
      "type": "AnnotationPage",
      "items": [
        {
           "type": "Annotation",
           "motivation": "painting",
           "body": {
             "id": "http://www1.pms-lj.si/animalia/media.php?id=PMSL-DBIP_Media-00249",
             "type": "Image",
             "format": "image/gif"
           },
           "target": "https://iiif.europeana.eu/presentation/08539/animalia_galerija_php_id_PMSL_DBIP_Media_00249/canvas/p1"
         }
      ]
    }
  ]
}
 */
public class BrowserSupportedV3 extends AbsMediaGeneratorV3 {

    @Override
    public Canvas generate(Canvas canvas, WebResource wr) {

        canvas.setWidth(wr.getWidth());
        canvas.setHeight(wr.getHeight());
        canvas.setDuration(wr.getDurationInSeconds());

        addCanvasMetadata(canvas, wr);

        // Add thumbnail but only if it is not a IIIF image
        if ( wr.hasService(SERVICE_TYPE_IMAGE) ) {
            canvas.getThumbnail().add(new Image(getThumbnailV2(wr)));
        }

        Annotation anno = newContentAnnotation(canvas);

        MediaType mediaType = wr.getMediaType();
        if (mediaType.isAudioVisual()) { anno.setTimeMode(TimeMode.trim); }

        // Now create the annotation body based on the media type 
        // Note: An annotation has 1 annotationBody
        ContentResource annoBody = getAnnotationBody(wr.getId(), mediaType.getCategory());
        anno.setBody(annoBody);

        annoBody.setFormat(mediaType.getMimeType());
        addTechnicalMetadata(canvas, annoBody);

        handleServices(annoBody, wr.getServices());

        return canvas;
    }


    protected void handleServices(ContentResource annoBody
                                , Collection<SvcsService> services) {
        for ( SvcsService s : services ) {
            Service service = processService(s);
            if ( service == null ) { continue; }

            annoBody.getServices().add(service);
            return;
        }
    }

    protected Service processService(SvcsService service) {
        String type = CONFORMS_TO_SERVICE.get(service.getConformsTo());
        if ( type == null ) { return null; }

        Service s = new Service(service.getId(), type);
        s.setProfile(service.getImplements());
        String label = service.getLabel();
        if ( label != null) { s.setLabel(new LanguageMap(label)); }
        return s;
    }
}