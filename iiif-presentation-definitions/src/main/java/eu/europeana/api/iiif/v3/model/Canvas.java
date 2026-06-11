/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.*;

import eu.europeana.api.iiif.v3.io.JsonConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Hugo
 * @since 29 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ID, TYPE, LABEL, HEIGHT, WIDTH, DURATION, REQUIRED_STATEMENT
                   , RIGHTS, THUMBNAIL, ITEMS})
@JsonIgnoreProperties(ignoreUnknown=true)
public class Canvas extends PresentationResource {

    @JsonProperty(JsonConstants.LABEL)
    private LanguageMap label;

    @JsonProperty(JsonConstants.HEIGHT)
    private Integer height; 

    @JsonProperty(JsonConstants.WIDTH)
    private Integer width;

    @JsonProperty(JsonConstants.DURATION)
    private Double duration;

    @JsonProperty(JsonConstants.ITEMS)
    private List<AnnotationPage> items;

    @JsonProperty(JsonConstants.ANNOTATIONS)
    private List<AnnotationPage> annotations;

    /**
     * Create a new canvas object
     * @param id
     */
    public Canvas(String id) {
        super(id);
    }

    protected Canvas() {}

    @Override
    public String getType() {
        return JsonConstants.CANVAS;
    }


    public LanguageMap getLabel() {
        return label;
    }

    public void setLabel(LanguageMap label) {
        this.label = label;
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

 
    public boolean hasItems() {
        return ( this.items != null && !this.items.isEmpty() );
    }

    public List<AnnotationPage> getItems() {
        return ( this.items != null ? this.items
                                    : (this.items = new ArrayList<>()));
    }


    public boolean hasAnnotations() {
        return ( this.annotations != null && !this.annotations.isEmpty() );
    }

    public List<AnnotationPage> getAnnotations() {
        return ( this.annotations != null ? this.annotations
                                  : (this.annotations = new ArrayList<>()));
    }

    public Annotation getStartCanvasAnnotation() {
        if (items == null || items.size() == 0) {
            return null;
        }
        return items.get(0).getItems().get(0);
    }

    public int getPageNr() {
        return Integer.parseInt(StringUtils.substringAfter(getID(), "/canvas/p"));
    }
}
