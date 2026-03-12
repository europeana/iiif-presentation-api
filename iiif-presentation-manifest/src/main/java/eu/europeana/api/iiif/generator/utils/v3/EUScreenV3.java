package eu.europeana.api.iiif.generator.utils.v3;

import eu.europeana.api.iiif.generator.ManifestGeneratorUtils;
import eu.europeana.api.iiif.v3.model.Annotation;
import eu.europeana.api.iiif.v3.model.Canvas;
import eu.europeana.api.iiif.v3.model.TimeMode;
import eu.europeana.api.iiif.v3.model.content.Video;
import eu.europeana.api.record.model.WebResource;
import org.springframework.stereotype.Component;



/*

Example:
{
  "id": "https://iiif.europeana.eu/presentation/2051918/data_euscreenXL_EUS_15541BBE705033639D4E06691D7A5D2E/canvas/p1",
  "type": "Canvas",
  ...
  "items": [
    {
      "type": "AnnotationPage",
      "items": [
        {
           "type": "Annotation",
           "motivation": "painting",
           "timeMode": "trim",
           "body": {
             "id": "http://www.euscreen.eu/item.html?id=EUS_15541BBE705033639D4E06691D7A5D2E",
             "type": "Video"
           },
           "target": "https://iiif.europeana.eu/presentation/2051918/data_euscreenXL_EUS_15541BBE705033639D4E06691D7A5D2E/canvas/p1"
         }
      ]
    }
  ]
}
 */

@Component
public class EUScreenV3 extends AbsMediaGeneratorV3 {

    public EUScreenV3(ManifestGeneratorUtils utils) {
        super(utils);
    }


    @Override
    public Canvas generate(Canvas canvas, WebResource wr) {

        addCanvasMetadata(canvas, wr);

        Annotation anno = newContentAnnotation(canvas);
        anno.setTimeMode(TimeMode.trim);
        anno.setBody(new Video(wr.getId()));

        return canvas;
    }
}