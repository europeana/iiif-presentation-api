package eu.europeana.api.iiif.generator.media;

import eu.europeana.api.record.model.WebResource;

public interface MediaGenerator<T> {
    T generate(T canvas, WebResource webResource);
}
