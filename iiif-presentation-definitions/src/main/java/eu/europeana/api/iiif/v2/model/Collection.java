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
                   , within, related, navDate, viewingHint
                   , thumbnail, rendering, manifests, collections })
public class Collection extends PresentationResource {

    @JsonProperty(JsonConstants.navDate)
    private String navDate;

    @JsonProperty(JsonConstants.manifests)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Manifest> manifests;

    @JsonProperty(JsonConstants.collections)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Collection> collections;

    public Collection(String id) {
        super(id);
    }

    protected Collection() {}

    @JsonProperty(JsonConstants.type)
    public String getType() {
        return JsonConstants.Collection;
    }


    public String getNavDate() {
        return this.navDate;
    }

    public void setNavDate(String navDate) {
        this.navDate = navDate;
    }


    public boolean hasManifests() {
        return ( this.manifests != null && !this.manifests.isEmpty() );
    }

    public List<Manifest> getManifests() {
        return ( this.manifests != null ? this.manifests
                                        : (this.manifests = new ArrayList<>()));
    }

    public boolean hasCollections() {
        return ( this.collections != null && !this.collections.isEmpty() );
    }

    public List<Collection> getCollections() {
        return ( this.collections != null ? this.collections
                                          : (this.collections = new ArrayList<>()));
    }
}
