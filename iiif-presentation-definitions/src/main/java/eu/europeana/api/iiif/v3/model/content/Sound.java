/**
 * 
 */
package eu.europeana.api.iiif.v3.model.content;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.ContentResource;

/**
 * @author Hugo
 * @since 7 Nov 2024
 */
@JsonPropertyOrder({ID, TYPE, LABEL, LANGUAGE, FORMAT, DURATION, SERVICE})
public class Sound extends ContentResource {

    @JsonProperty(JsonConstants.DURATION)
    private Double  duration;


    public Sound(String id) {
        super(id);
    }

    protected Sound() {}

    @JsonProperty(JsonConstants.TYPE)
    public String getType() {
        return JsonConstants.SOUND;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }
}
