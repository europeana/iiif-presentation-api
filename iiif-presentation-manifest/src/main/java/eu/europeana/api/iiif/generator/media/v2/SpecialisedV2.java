package eu.europeana.api.iiif.generator.media.v2;

import eu.europeana.api.iiif.generator.ManifestGeneratorUtils;
import eu.europeana.api.iiif.generator.ManifestSettings;
import eu.europeana.api.iiif.v2.model.AnnotationBody;
import eu.europeana.api.iiif.v2.model.Canvas;
import eu.europeana.api.record.model.Resolution;
import eu.europeana.api.record.model.WebResource;

import org.springframework.stereotype.Component;

/**
 Handles Canvas generation for the media belonging to specialized type
 when manifest is requested with format version 2.
 Example:
 <pre>
 {@code
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
 </pre>
 }
 **/
@Component
public class SpecialisedV2 extends AbstractMediaGeneratorV2 {

    public SpecialisedV2(ManifestSettings settings) {
        super(settings);
    }

    @Override
    public Canvas generate(Canvas canvas, WebResource wr) {

        String thumbnailUrl = ManifestGeneratorUtils.getThumbnailV2(settings, wr);
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
        addRendering(wr, canvas);
        handleIsFormatOf(wr, canvas);

        return canvas;
    }
}
