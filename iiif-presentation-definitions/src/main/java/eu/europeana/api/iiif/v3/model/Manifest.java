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
 * @since 14 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ id, type, label, summary, metadata, requiredStatement
                   , rights, provider, homepage, seeAlso, service, partOf
                   , navDate, behavior, viewingDirection, thumbnail, start, items
                   , rendering, placeholderCanvas })
@JsonIgnoreProperties(ignoreUnknown=true)
public class Manifest extends PresentationResource {

    @JsonProperty(JsonConstants.partOf)
    private List<Collection> partOf;

    @JsonProperty(JsonConstants.viewingDirection)
    private ViewingDirection viewingDirection;

    @JsonProperty(JsonConstants.start)
    private Canvas start;

    @JsonProperty(JsonConstants.items)
    private List<Canvas> items;

    @JsonProperty(JsonConstants.rights)
    private String rightsUrl;

    public Manifest(String id) {
        super(id);
    }

    protected Manifest() {}

    public String getType() {
        return JsonConstants.Manifest;
    }


    public boolean hasPartOf() {
        return ( this.partOf != null && !this.partOf.isEmpty() );
    }

    public List<Collection> getPartOf() {
        return ( this.partOf != null ? this.partOf
                                     : (this.partOf = new ArrayList<>()));
    }

    public ViewingDirection getViewingDirection() {
        return this.viewingDirection;
    }

    public void setViewingDirection(ViewingDirection direction) {
        this.viewingDirection = direction;
    }

    
    public Canvas getStart() {
        return this.start;
    }

    public void setStart(Canvas start) {
        //Generate new canvas object with the of the intended canvas for indicating the start
        this.start = new Canvas(start.getID());
    }


    public boolean hasItems() {
        return ( this.items != null && !this.items.isEmpty() );
    }

    public List<Canvas> getItems() {
        return ( this.items != null ? this.items
                                    : (this.items = new ArrayList<>()));
    }

    public String getRightsUrl() {
        return this.rightsUrl;
    }

    public void setRightsUrl(String rights) {
        this.rightsUrl = rights;
    }

}
