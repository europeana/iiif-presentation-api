/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.v3.io.JsonConstants;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public abstract class IIIFv3Resource implements IIIFResource {

    @JsonProperty(JsonConstants.id)
    private String id;

    public IIIFv3Resource(String id) {
        this.id = id;
    }

    protected IIIFv3Resource() {}

    public String getID() {
        return this.id;
    }

    @JsonProperty(JsonConstants.type)
    public abstract String getType();
}
