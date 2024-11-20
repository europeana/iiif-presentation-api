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
@JsonPropertyOrder({ id, type, motivation, resource, on })
public class Annotation extends IIIFv2Resource {

    @JsonProperty(JsonConstants.motivation)
    private String motivation;

    @JsonProperty(JsonConstants.resource)
    private AnnotationBody resource;
    
    @JsonProperty(JsonConstants.on)
    private String on;


    public Annotation(String id) {
        super(id);
    }

    protected Annotation() {}

    @Override
    @JsonProperty(JsonConstants.type)
    public String getType() {
        return JsonConstants.Annotation;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }


    public AnnotationBody getBody() {
        return resource;
    }

    public void setBody(AnnotationBody resource) {
        this.resource = resource;
    }

    public String getOn() {
        return on;
    }

    public void setOn(String on) {
        this.on = on;
    }
}
