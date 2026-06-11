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
@JsonPropertyOrder({ID, TYPE, LABEL, LANGUAGE
                   , FORMAT, HEIGHT, WIDTH, DURATION, SERVICE})
public class Video extends ContentResource {

    @JsonProperty(JsonConstants.HEIGHT)
    private Integer height;

    @JsonProperty(JsonConstants.WIDTH)
    private Integer width;

    @JsonProperty(JsonConstants.DURATION)
    private Double  duration;


    public Video(String id) {
        super(id);
    }

    protected Video() {}


    @JsonProperty(JsonConstants.TYPE)
    public String getType() {
        return JsonConstants.VIDEO;
    }


    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }
}
