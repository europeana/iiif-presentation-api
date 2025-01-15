/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.*;

import eu.europeana.api.iiif.v3.io.JsonConstants;

/**
 * @author Hugo
 * @since 29 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ id, type, label, height, width, duration, requiredStatement
                   , rights, thumbnail, items })
@JsonIgnoreProperties(ignoreUnknown=true)
public class Canvas extends PresentationResource {

    @JsonProperty(JsonConstants.label)
    private LanguageMap label;

    @JsonProperty(JsonConstants.height)
    private Integer height; 

    @JsonProperty(JsonConstants.width)
    private Integer width;

    @JsonProperty(JsonConstants.duration)
    private Double duration;

    @JsonProperty(JsonConstants.items)
    private List<AnnotationPage> items;

    @JsonProperty(JsonConstants.annotations)
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
    @JsonProperty(JsonConstants.type)
    public String getType() {
        return JsonConstants.Canvas;
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
    public Annotation getStartCanvasAnnotation() {
        if (items == null || items.size() == 0) {
            return null;
        }
        return items.get(0).getItems().get(0);
    }


}
