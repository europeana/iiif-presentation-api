package eu.europeana.api.iiif.generator.media;

import org.springframework.stereotype.Component;

import eu.europeana.api.record.model.WebResource;

@Component
public class NoCanvas<T> implements MediaGenerator<T> {
    @Override
    public T generate(T canvas, WebResource webResource) {
        return null;
    }
}
