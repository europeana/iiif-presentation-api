package eu.europeana.api.iiif.generator.media.v3;

import eu.europeana.api.iiif.generator.ManifestSettings;

import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.v3.model.Annotation;
import eu.europeana.api.iiif.v3.model.Canvas;
import eu.europeana.api.iiif.v3.model.ContentResource;
import eu.europeana.api.iiif.v3.model.Service;
import eu.europeana.api.iiif.v3.model.TimeMode;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.record.model.SvcsService;
import eu.europeana.api.record.model.WebResource;

import org.springframework.stereotype.Component;


import static eu.europeana.api.iiif.generator.ManifestGeneratorUtils.*;
import static eu.europeana.api.iiif.generator.ManifestGeneratorConstants.*;


/**
 * Example:
 * {
 *   "id": "https://iiif.europeana.eu/presentation/08539/animalia_galerija_php_id_PMSL_DBIP_Media_00249/canvas/p1",
 *   "type": "Canvas",
 *   ...
 *   "items": [
 *     {
 *       "type": "AnnotationPage",
 *       "items": [
 *         {
 *            "type": "Annotation",
 *            "motivation": "painting",
 *            "body": {
 *              "id": "http://www1.pms-lj.si/animalia/media.php?id=PMSL-DBIP_Media-00249",
 *              "type": "Image",
 *              "format": "image/gif"
 *            },
 *            "target": "https://iiif.europeana.eu/presentation/08539/animalia_galerija_php_id_PMSL_DBIP_Media_00249/canvas/p1"
 *          }
 *       ]
 *     }
 *   ]
 * }
 */

@Component
public class BrowserSupportedV3 extends AbstractMediaGeneratorV3 {

    public BrowserSupportedV3(ManifestSettings settings) {
        super(settings);
    }

    @Override
    public Canvas generate(Canvas canvas, WebResource wr) {
        canvas.setWidth(wr.getWidth());
        canvas.setHeight(wr.getHeight());
        canvas.setDuration(wr.getDurationInSeconds());
        addCanvasMetadata(canvas, wr);

        // Add thumbnail but only if it is not a IIIF image
        if (!wr.hasServiceByConformsTo(SERVICE_TYPE_IMAGE) ) {
            String url = getThumbnail(settings, wr);
            canvas.getThumbnail().add(new Image(url));
        }
        Annotation anno = newContentAnnotation(canvas);
        MediaType mediaType = wr.getMediaType();
        if (mediaType.isAudioVisual()) {
            anno.setTimeMode(TimeMode.trim);
        }

        // Now create the annotation body based on the media type 
        // Note: An annotation has 1 annotationBody
        ContentResource annoBody = getAnnotationBody(wr.getId(), mediaType.getCategory());
        anno.setBody(annoBody);
        annoBody.setFormat(mediaType.getMimeType());
        addTechnicalMetadata(canvas, annoBody);

        handleServices(annoBody, wr);
        handleIsFormatOf(wr, canvas);

        return canvas;
    }

    protected void handleServices(ContentResource annoBody, WebResource wr) {
        if (!wr.hasServices()) {
            return;
        }

        for ( SvcsService s : wr.getServicesAsResources() ) {

            String type = getServiceType(s.getConformsTo());
            if (type == null) {
                continue;
            }

            Service service = new Service(s.getId(), type);
            service.setProfile(s.getImplements());
            service.setLabel(s.getLabel());
            annoBody.getServices().add(service);
        }
    }
}
