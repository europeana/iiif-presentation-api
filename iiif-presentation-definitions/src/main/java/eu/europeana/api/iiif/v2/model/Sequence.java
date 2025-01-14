/**
 * 
 */
package eu.europeana.api.iiif.v2.model;

import static eu.europeana.api.iiif.v2.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v2.io.JsonConstants;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonPropertyOrder({ id, type, label, description, metadata
                   , attribution, license, logo, seeAlso, service
                   , within, related, viewingHint, viewingDirection
                   , thumbnail, rendering, startCanvas, canvases })
public class Sequence extends PresentationResource {

    @JsonProperty(JsonConstants.viewingDirection)
    private ViewingDirection viewingDirection;

    @JsonProperty(JsonConstants.startCanvas)
    private String startCanvas;

    @JsonProperty(JsonConstants.canvases)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Canvas> canvases;

    public Sequence(String id) {
        super(id);
    }

    public Sequence() {}

    @JsonProperty(JsonConstants.type)
    public String getType() {
        return JsonConstants.Sequence;
    }


    public ViewingDirection getViewingDirection() {
        return this.viewingDirection;
    }

    public void setViewingDirection(ViewingDirection direction) {
        this.viewingDirection = direction;
    }


    public String getStartCanvas() {
        return this.startCanvas;
    }

    public void setStartCanvas(String startCanvas) {
        this.startCanvas = startCanvas;
    }


    public boolean hasCanvases() {
        return ( this.canvases != null && !this.canvases.isEmpty() );
    }

    public List<Canvas> getCanvases() {
        return ( this.canvases != null ? this.canvases
                                       : (this.canvases = new ArrayList<>()));
    }

    public void setCanvases(List<Canvas> canvases) {
        this.canvases = canvases;
    }
}
