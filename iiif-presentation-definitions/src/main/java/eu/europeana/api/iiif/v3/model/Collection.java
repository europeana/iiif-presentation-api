/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v3.io.JsonConstants;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ id, type, label, summary, metadata, requiredStatement
                   , rights, provider, homepage, seeAlso, service, partOf
                   , navDate, behavior, viewingDirection, thumbnail, items
                   , rendering, placeholderCanvas })
@JsonIgnoreProperties(ignoreUnknown=true)
public class Collection extends PresentationResource {

    @JsonProperty(JsonConstants.partOf)
    private List<Collection> partOf;

    @JsonProperty(JsonConstants.viewingDirection)
    private ViewingDirection viewingDirection;

    @JsonProperty(JsonConstants.items)
    private List<PresentationResource> items;

    public Collection(String id) {
        super(id);
    }

    protected Collection() {}

    @JsonProperty(JsonConstants.type)
    public String getType() {
        return JsonConstants.Collection;
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
