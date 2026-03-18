package eu.europeana.api.iiif.generator.media;

import eu.europeana.api.record.model.WebResource;

public interface MediaGenerator<Canvas> {

    Canvas generate(Canvas canvas, WebResource webResource);
}