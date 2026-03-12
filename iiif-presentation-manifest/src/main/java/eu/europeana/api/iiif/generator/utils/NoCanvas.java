package eu.europeana.api.iiif.generator.utils;

import eu.europeana.api.record.model.WebResource;
import org.springframework.stereotype.Component;

@Component
public class NoCanvas<Canvas> implements MediaGenerator<Canvas> {


    @Override
    public Canvas generate(Canvas canvas, WebResource webResource) {
        return null;
    }
}