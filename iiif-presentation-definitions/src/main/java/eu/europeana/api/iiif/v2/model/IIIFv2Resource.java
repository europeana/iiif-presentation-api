/**
 * 
 */
package eu.europeana.api.iiif.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.v2.io.JsonConstants;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public abstract class IIIFv2Resource implements IIIFResource {

    @JsonProperty(JsonConstants.id)
    private String id;

    public IIIFv2Resource(String id) {
        this.id = id;
    }

    protected IIIFv2Resource() {}

    public String getID() {
        return this.id;
    }

    @JsonProperty(JsonConstants.type)
    public abstract String getType();
}
