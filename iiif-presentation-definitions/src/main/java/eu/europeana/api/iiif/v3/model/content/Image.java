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
@JsonPropertyOrder({ id, type, label, language, format, height, width, service })
public class Image extends ContentResource {

    @JsonProperty(JsonConstants.height)
    private Integer height;

    @JsonProperty(JsonConstants.width)
    private Integer width;


    public Image(String id) {
        super(id);
    }

    protected Image() {}


    @JsonProperty(JsonConstants.type)
    public String getType() {
        return JsonConstants.Image;
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
}
