package eu.europeana.api.iiif.generator.media;

import org.springframework.stereotype.Component;

import eu.europeana.api.record.model.WebResource;

@Component
public class NoCanvas<Canvas> implements MediaGenerator<Canvas> {
    @Override
    public Canvas generate(Canvas canvas, WebResource webResource) {
        return null;
    }
}