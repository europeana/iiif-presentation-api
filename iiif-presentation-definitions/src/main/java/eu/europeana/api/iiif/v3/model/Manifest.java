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

    public Manifest(String id) {
        super(id);
    }

    protected Manifest() {}

    @JsonProperty(JsonConstants.type)
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
        this.start = start;
    }


    public boolean hasItems() {
        return ( this.items != null && !this.items.isEmpty() );
    }

    public List<Canvas> getItems() {
        return ( this.items != null ? this.items
                                    : (this.items = new ArrayList<>()));
    }


    // TODO review these changes, added as they are present in manifest api
    @JsonIgnore
    private String europeanaId; // for internal use only
    @JsonIgnore
    private String isShownBy; // for internal use only

    public String getEuropeanaId() {
        return europeanaId;
    }

    public String getIsShownBy() {
        return isShownBy;
    }

    /**
     * Create a new empty manifest (only id, context and logo ar filled in)
     * @param europeanaId
     * @param manifestId
     */
    public Manifest(String europeanaId, String manifestId, String isShownBy) {
        super(manifestId);
        this.europeanaId = europeanaId;
        this.isShownBy = isShownBy;
    }
}
