package eu.europeana.api.iiif.generator.utils;

import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.v2.model.AnnotationBody;
import eu.europeana.api.iiif.v2.model.Canvas;
import eu.europeana.api.iiif.v2.model.Image;
import eu.europeana.api.iiif.v2.model.LanguageValue;
import eu.europeana.api.record.model.Resolution;
import eu.europeana.api.record.model.WebResource;

import static eu.europeana.api.iiif.generator.GeneratorUtils.*;

/*

Example:
{
  "@id": "https://iiif.europeana.eu/presentation/09313/1395236/canvas/p1",
  "@type": "sc:Canvas",
  ...
  "width": 400,
  "height": 436,
  "images": [
    {
       "@type": "oa:Annotation",
       "motivation": "sc:painting",
       "resource": {
         "@id": "https://api.europeana.eu/thumbnail/v2/url.json?uri=http%3A%2F%2F67.111.179.146%2Fwebclient%2FDeliveryManager%3Fpid%3D1395238%26custom_att_1%3Ddirect_view&type=TEXT",
         "@type": "dctypes:Image"
       },
       "on": "https://iiif.europeana.eu/presentation/09313/1395236/canvas/p1"
    }
  ],
  "rendering": {
    "@id": "http://67.111.179.146/webclient/DeliveryManager?pid=1395238&custom_att_1=direct_view",
    "format": "image/tiff",
    "label": "TIFF"
  }
}

 */
public class SpecialisedV2 extends AbsMediaGeneratorV2 {

    @Override
    public Canvas generate(Canvas canvas, WebResource wr) {

        String thumbnailUrl = getThumbnailV2(wr);
        Resolution res = wr.getResolution().scaleToWidth(400);

        canvas.setWidth(res.width());
        canvas.setHeight(res.height());

        addCanvasMetadata(canvas, wr);

        // Create an annotation body for the thumbnail image
        AnnotationBody annoBody = new AnnotationBody(thumbnailUrl);
        newContentAnnotation(canvas).setBody(annoBody);
        //annoBody.setFormat(mediaType.getMimeType());
        addTechnicalMetadata(canvas, annoBody);

        // add rendering component
        MediaType mediaType = wr.getMediaType();
        Image renderingImage = new Image(wr.getId());
        renderingImage.setFormat(mediaType.getMimeType());
        renderingImage.setLabel(new LanguageValue(mediaType.getLabel()));
        canvas.getRendering().add(renderingImage);

        return canvas;
    }
}