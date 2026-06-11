package eu.europeana.api.iiif.v3.model.content;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;
import static eu.europeana.api.iiif.v3.io.JsonConstants.SERVICE;

@JsonPropertyOrder({ID, TYPE, LABEL, LANGUAGE, FORMAT, HEIGHT, WIDTH, SERVICE})
public class Rendering extends Image {

    private String type;

    public Rendering(String id) {
        super(id);
    }

    public Rendering(String id, String type) {
        super(id);
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }
}
