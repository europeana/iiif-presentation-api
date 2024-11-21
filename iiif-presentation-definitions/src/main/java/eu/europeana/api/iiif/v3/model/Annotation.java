/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v3.io.JsonConstants;

/**
 * @author Hugo
 * @since 29 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonPropertyOrder({ id, type, motivation, timeMode, body, target })
public class Annotation extends IIIFv3Resource {

    @JsonProperty(JsonConstants.motivation)
    private String motivation;

    @JsonProperty(JsonConstants.timeMode)
    private TimeMode timeMode;

    @JsonProperty(JsonConstants.body)
    private ContentResource body;
    
    @JsonProperty(JsonConstants.target)
    private String target;


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

    public TimeMode getTimeMode() {
        return timeMode;
    }

    public void setTimeMode(TimeMode timeMode) {
        this.timeMode = timeMode;
    }

    public ContentResource getBody() {
        return body;
    }

    public void setBody(ContentResource body) {
        this.body = body;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
