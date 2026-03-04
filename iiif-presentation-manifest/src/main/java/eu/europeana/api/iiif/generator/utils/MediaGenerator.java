package eu.europeana.api.iiif.generator.utils;

import eu.europeana.api.record.model.WebResource;

public interface MediaGenerator<Canvas> {

    public Canvas generate(Canvas canvas, WebResource webResource);
}
