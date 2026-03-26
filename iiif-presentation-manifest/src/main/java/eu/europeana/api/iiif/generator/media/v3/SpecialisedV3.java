package eu.europeana.api.iiif.generator.media.v3;

import eu.europeana.api.iiif.generator.ManifestGeneratorUtils;
import eu.europeana.api.iiif.generator.ManifestSettings;

import eu.europeana.api.iiif.v3.model.Canvas;
import eu.europeana.api.iiif.v3.model.ContentResource;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.record.model.Resolution;
import eu.europeana.api.record.model.WebResource;

import org.springframework.stereotype.Component;

/**
 * Example:
 * {
 *   "id": "https://iiif.europeana.eu/presentation/09313/1395236/canvas/p1",
 *   "type": "Canvas",
 *   ...
 *   "width": 400,
 *   "height": 436,
 *   "items": [
 *     {
 *       "type": "AnnotationPage",
 *       "items": [
 *         {
 *            "type": "Annotation",
 *            "motivation": "painting",
 *            "body": {
 *              "id": "https://api.europeana.eu/thumbnail/v2/url.json?uri=http%3A%2F%2F67.111.179.146%2Fwebclient%2FDeliveryManager%3Fpid%3D1395238%26custom_att_1%3Ddirect_view&type=TEXT",
 *              "type": "Image"
 *            },
 *            "target": "https://iiif.europeana.eu/presentation/09313/1395236/canvas/p1"
 *          }
 *       ]
 *     }
 *   ],
 *   "rendering": [
 *     {
 *       "id": "http://67.111.179.146/webclient/DeliveryManager?pid=1395238&custom_att_1=direct_view",
 *       "type": "Image",
 *       "format": "image/tiff",
 *       "label": { "zxx": [ "TIFF" ] }
 *     }
 *   ]
 * }
 */
@Component
public class SpecialisedV3 extends AbsMediaGeneratorV3 {
    public SpecialisedV3(ManifestSettings settings) {
        super(settings);
    }
    @Override
    public Canvas generate(Canvas canvas, WebResource wr) {

        String thumbnailUrl = ManifestGeneratorUtils.getThumbnailV2(settings, wr);
        Resolution res = wr.getResolution().scaleToWidth(400);

        canvas.setWidth(res.width());
        canvas.setHeight(res.height());

        addCanvasMetadata(canvas, wr);

        // Create an Image resource since we will be displaying a thumbnail
        ContentResource annoBody = new Image(thumbnailUrl);
        newContentAnnotation(canvas).setBody(annoBody);
        addTechnicalMetadata(canvas, annoBody);

        // add rendering component
        addRendering(wr, canvas);
        handleIsFormatOf(wr, canvas);

        return canvas;
    }
}