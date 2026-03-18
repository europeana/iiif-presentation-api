package eu.europeana.api.iiif.generator.media.v2;

import static eu.europeana.api.record.model.RecordConstants.*;
import eu.europeana.api.iiif.generator.ManifestGeneratorUtils;
import eu.europeana.api.iiif.generator.ManifestSettings;

import java.util.Collection;

import eu.europeana.api.iiif.v2.model.AnnotationBody;
import eu.europeana.api.iiif.v2.model.Canvas;
import eu.europeana.api.iiif.v2.model.Image;
import eu.europeana.api.iiif.v2.model.Service;
import eu.europeana.api.record.model.SvcsService;
import eu.europeana.api.record.model.WebResource;

import org.springframework.stereotype.Component;

import static eu.europeana.api.iiif.generator.ManifestGeneratorConstants.*;

/*
Example:
{
  "@id": "https://iiif.europeana.eu/presentation/08539/animalia_galerija_php_id_PMSL_DBIP_Media_00249/canvas/p1",
  "@type": "sc:Canvas",
  ...
  "images": [
    {
      "@type": "oa:Annotation",
      "motivation": "sc:painting",
      "resource": {
        "@type": "dctypes:Image",
        "@id": "http://www1.pms-lj.si/animalia/media.php?id=PMSL-DBIP_Media-00249",
        "format": "image/gif"
      },
      "on": "https://iiif.europeana.eu/presentation/08539/animalia_galerija_php_id_PMSL_DBIP_Media_00249/canvas/p1"
    }
  ]
}
 */
@Component
public class BrowserSupportedV2 extends AbsMediaGeneratorV2 {

    public BrowserSupportedV2(ManifestSettings settings) {
        super(settings);
    }

    @Override
    public Canvas generate(Canvas canvas, WebResource wr) {
        canvas.setWidth(wr.getWidth());
        canvas.setHeight(wr.getHeight());
        addCanvasMetadata(canvas, wr);

        if ( wr.hasServiceByConformsTo(SERVICE_TYPE_IMAGE) ) {
            String url = ManifestGeneratorUtils.getThumbnailV2(settings, wr);
            canvas.setThumbnail(new Image(url));
        }

        // Now create the annotation body based on the media type 
        // Note: An annotation has 1 annotationBody
        AnnotationBody annoBody = new AnnotationBody(wr.getId());
        newContentAnnotation(canvas).setBody(annoBody);
        annoBody.setFormat(wr.getMediaType().getMimeType());
        addTechnicalMetadata(canvas, annoBody);

        handleServices(annoBody, wr);
        handleIsFormatOf(wr, canvas);

        return canvas;
    }

    protected void handleServices(AnnotationBody annoBody, WebResource wr) {
    	SvcsService service = wr.getServiceByConformsTo(SERVICE_TYPE_IMAGE);
        if( service == null) { return; }

        Service s = new Service(service.getId(), IMAGE_CONTEXT_VALUE);
        s.setProfile(service.getImplements());
        annoBody.setService(s);
    }
}