/**
 * 
 */
package eu.europeana.api.iiif.v2.model;

import static eu.europeana.api.iiif.v2.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.*;

import eu.europeana.api.iiif.v2.io.JsonConstants;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonPropertyOrder({ id, type, label, height, width, description, metadata
                   , attribution, license, logo, seeAlso, service
                   , within, related, viewingHint
                   , thumbnail, rendering, images, otherContent })
public class Canvas extends PresentationResource {

    @JsonProperty(JsonConstants.height)
    private Integer height;

    @JsonProperty(JsonConstants.width)
    private Integer width;

    @JsonProperty(JsonConstants.images)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Annotation> images;

    @JsonProperty(JsonConstants.otherContent)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
   private List<String> otherContent;

    public Canvas(String id) {
        super(id);
    }

    protected Canvas() {}

    @JsonProperty(JsonConstants.type)
    public String getType() {
        return JsonConstants.Canvas;
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


    public boolean hasImages() {
        return ( this.images != null && !this.images.isEmpty() );
    }

    public List<Annotation> getImages() {
        return ( this.images != null ? this.images
                                     : (this.images = new ArrayList<>()));
    }

    public void setImages(List<Annotation> images) {
        this.images = images;
    }

    public boolean hasOtherContent() {
        return ( this.otherContent != null && !this.otherContent.isEmpty() );
    }

    public List<String> getOtherContent() {
        return ( this.otherContent != null ? this.otherContent
                                           : (this.otherContent = new ArrayList<>()));
    }

    // TODO review this added as it is present in manifest api

    @JsonIgnore
    private int pageNr; // for internal use

    public int getPageNr() {
        return pageNr;
    }

    /**
     * Create a new canvas object
     * @param id
     * @param pageNr
     */
    public Canvas(String id, int pageNr) {
        super(id);
        this.pageNr = pageNr;
    }

    @JsonIgnore
    public Annotation getStartImageAnnotation() {
        if (images == null || images.size() == 0) {
            return null;
        }
        return images.get(0);
    }

}
