package eu.europeana.api.iiif.generator.utils;

import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.utils.EdmManifestUtils;
import eu.europeana.api.iiif.v3.model.Canvas;
import eu.europeana.api.iiif.v3.model.ContentResource;
import eu.europeana.api.iiif.v3.model.LanguageMap;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.iiif.v3.model.content.Rendering;
import eu.europeana.api.record.model.Resolution;
import eu.europeana.api.record.model.WebResource;

import static eu.europeana.api.iiif.generator.GeneratorUtils.*;

/*

Example:
{
  "id": "https://iiif.europeana.eu/presentation/09313/1395236/canvas/p1",
  "type": "Canvas",
  ...
  "width": 400,
  "height": 436,
  "items": [
    {
      "type": "AnnotationPage",
      "items": [
        {
           "type": "Annotation",
           "motivation": "painting",
           "body": {
             "id": "https://api.europeana.eu/thumbnail/v2/url.json?uri=http%3A%2F%2F67.111.179.146%2Fwebclient%2FDeliveryManager%3Fpid%3D1395238%26custom_att_1%3Ddirect_view&type=TEXT",
             "type": "Image"
           },
           "target": "https://iiif.europeana.eu/presentation/09313/1395236/canvas/p1"
         }
      ]
    }
  ],
  "rendering": [
    {
      "id": "http://67.111.179.146/webclient/DeliveryManager?pid=1395238&custom_att_1=direct_view",
      "type": "Image",
      "format": "image/tiff",
      "label": { "zxx": [ "TIFF" ] }
    }
  ]
}

 */
public class SpecialisedV3 extends AbsMediaGeneratorV3 {

    @Override
    public Canvas generate(Canvas canvas, WebResource wr) {

        String thumbnailUrl = getThumbnailV2(wr);
        Resolution res = wr.getResolution().scaleToWidth(400);

        canvas.setWidth(res.width());
        canvas.setHeight(res.height());

        addCanvasMetadata(canvas, wr);

        // Create an Image resource since we will be displaying a thumbnail
        ContentResource annoBody = new Image(thumbnailUrl);
        newContentAnnotation(canvas).setBody(annoBody);
        addTechnicalMetadata(canvas, annoBody);

        // add rendering component
        MediaType mediaType = wr.getMediaType();
        Rendering renderingImage = new Rendering(wr.getId()
                                               , mediaType.getCategory().name());
        renderingImage.setFormat(mediaType.getMimeType());
        renderingImage.setLabel(new LanguageMap(EdmManifestUtils.LINGUISTIC
                                              , mediaType.getLabel()));
        canvas.getRendering().add(renderingImage);

        return canvas;
    }
}