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
@JsonPropertyOrder({ id, type, label, description, metadata
                   , attribution, license, logo, seeAlso, service
                   , within, related, navDate, viewingHint, viewingDirection
                   , thumbnail, rendering, sequences })
// TODO needs to have europeana ID field for internal use
public class Manifest extends PresentationResource {

    @JsonProperty(JsonConstants.navDate)
    private String navDate;

    @JsonProperty(JsonConstants.viewingDirection)
    private ViewingDirection viewingDirection;

    @JsonProperty(JsonConstants.sequences)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Sequence> sequences;

    public Manifest(String id) {
        super(id);
    }

    protected Manifest() {}

    @JsonProperty(JsonConstants.type)
    public String getType() {
        return JsonConstants.Manifest;
    }


    public String getNavDate() {
        return this.navDate;
    }

    public void setNavDate(String navDate) {
        this.navDate = navDate;
    }


    public ViewingDirection getViewingDirection() {
        return this.viewingDirection;
    }

    public void setViewingDirection(ViewingDirection direction) {
        this.viewingDirection = direction;
    }


    public boolean hasSequences() {
        return ( this.sequences != null && !this.sequences.isEmpty() );
    }

    public List<Sequence> getSequences() {
        return ( this.sequences != null ? this.sequences
                                        : (this.sequences = new ArrayList<>()));
    }

    public void setSequences(List<Sequence> sequences) {
        this.sequences = sequences;
    }


    // TODO review these changes, added as they are present in manifest api
    @JsonIgnore
    private Integer startCanvasPageNr; // for internal use only, similar to 'start' field in v3
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
     * Create a new empty manifest (only id is filled)
     * @param europeanaId
     * @param manifestId
     */
    public Manifest(String europeanaId, String manifestId, String isShownBy) {
        super(manifestId);
        this.europeanaId = europeanaId;
        this.isShownBy = isShownBy;
    }
}
