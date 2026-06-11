/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import com.fasterxml.jackson.annotation.*;

import eu.europeana.api.iiif.v3.io.JsonConstants;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ID, TYPE, LABEL, SUMMARY, METADATA, REQUIRED_STATEMENT
                   , RIGHTS, PROVIDER, HOMEPAGE, SEE_ALSO, SERVICE, PART_OF
                   , NAV_DATE, BEHAVIOR, VIEWING_DIRECTION, THUMBNAIL, ITEMS
                   , RENDERING, PLACEHOLDER_CANVAS})
@JsonIgnoreProperties(ignoreUnknown=true)
public class Collection extends PresentationResource {

    @JsonProperty(JsonConstants.PART_OF)
    private List<Collection> partOf;

    @JsonProperty(JsonConstants.VIEWING_DIRECTION)
    private ViewingDirection viewingDirection;

    @JsonProperty(JsonConstants.ITEMS)
    private List<PresentationResource> items;

    public Collection(String id) {
        super(id);
    }

    protected Collection() {}

    @JsonProperty(JsonConstants.TYPE)
    public String getType() {
        return JsonConstants.COLLECTION;
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


    public boolean hasItems() {
        return ( this.items != null && !this.items.isEmpty() );
    }

    public List<PresentationResource> getItems() {
        return ( this.items != null ? this.items
                                    : (this.items = new ArrayList<>()));
    }
}
