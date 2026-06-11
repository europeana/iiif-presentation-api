package eu.europeana.api.iiif.v3.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.v3.io.JsonConstants;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ID, TYPE})
public abstract class IIIFv3Resource implements IIIFResource {

    @JsonProperty(JsonConstants.ID)
    private String id;

    public IIIFv3Resource(String id) {
        this.id = id;
    }

    protected IIIFv3Resource() {}

    public String getID() {
        return this.id;
    }

    @JsonProperty(JsonConstants.TYPE)
    public abstract String getType();
}
