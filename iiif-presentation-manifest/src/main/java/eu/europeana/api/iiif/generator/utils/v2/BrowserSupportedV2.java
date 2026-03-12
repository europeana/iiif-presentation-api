package eu.europeana.api.iiif.generator.utils.v2;

import static eu.europeana.api.record.model.RecordConstants.*;
import eu.europeana.api.iiif.generator.ManifestGeneratorUtils;
import eu.europeana.api.iiif.generator.ManifestGeneratorConstants;

import java.util.Collection;

import eu.europeana.api.iiif.v2.model.AnnotationBody;
import eu.europeana.api.iiif.v2.model.Canvas;
import eu.europeana.api.iiif.v2.model.Image;
import eu.europeana.api.iiif.v2.model.Service;
import eu.europeana.api.record.model.SvcsService;
import eu.europeana.api.record.model.WebResource;
import org.springframework.stereotype.Component;

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
    public BrowserSupportedV2(ManifestGeneratorUtils utils) {
        super(utils);
    }
    @Override
    public Canvas generate(Canvas canvas, WebResource wr) {
        canvas.setWidth(wr.getWidth());
        canvas.setHeight(wr.getHeight());
        addCanvasMetadata(canvas, wr);

        if ( wr.hasService(SERVICE_TYPE_IMAGE) ) {
            canvas.setThumbnail(new Image(utils.getThumbnailV2(wr)));
        }
        // Now create the annotation body based on the media type 
        // Note: An annotation has 1 annotationBody
        AnnotationBody annoBody = new AnnotationBody(wr.getId());
        newContentAnnotation(canvas).setBody(annoBody);
        annoBody.setFormat(wr.getMediaType().getMimeType());
        addTechnicalMetadata(canvas, annoBody);
        handleServices(annoBody, wr.getServices());
        return canvas;
    }
    protected void handleServices(AnnotationBody annoBody
                                , Collection<SvcsService> services) {
        if(services != null) {
            for (SvcsService s : services) {
                Service service = processService(s);
                if (service == null) {
                    continue;
                }
                annoBody.setService(service);
                return;
            }
        }
    }
    protected Service processService(SvcsService service) {
        String conformsTo = service.getConformsTo();
        if ( SERVICE_TYPE_IMAGE.equals(conformsTo) ) {
            Service s = new Service(service.getId(), ManifestGeneratorConstants.IMAGE_CONTEXT_VALUE);
            s.setProfile(service.getImplements());
            return s;
        }
        return null;
    }
}