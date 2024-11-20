/**
 * 
 */
package eu.europeana.api.iiif.v2.model;

import static eu.europeana.api.iiif.v2.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v2.io.JsonConstants;

/**
 * @author Hugo
 * @since 23 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonPropertyOrder({ id, label, format })
public class ResourceReference extends IIIFv2Resource {

    @JsonProperty(JsonConstants.label)
    private String label;

    @JsonProperty(JsonConstants.format)
    private String format;


    public ResourceReference(String id) { super(id); }


    @Override
    public String getType() {
        return null;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}