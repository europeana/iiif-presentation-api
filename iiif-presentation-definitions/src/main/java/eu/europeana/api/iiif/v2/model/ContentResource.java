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
 * @since 29 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonPropertyOrder({ id, type, label, format, service })
public abstract class ContentResource extends IIIFv2Resource {

    @JsonProperty(JsonConstants.label)
    private LanguageValue label;

    @JsonProperty(JsonConstants.format)
    private String format;

    @JsonProperty(JsonConstants.service)
    private Service service;


    public ContentResource(String id) {
        super(id);
    }

    protected ContentResource() {}


    public LanguageValue getLabel() {
        return this.label;
    }

    public void setLabel(LanguageValue value) {
        this.label = value;
    }


    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }


    public Service getService() {
        return this.service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
