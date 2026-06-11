/**
 * 
 */
package eu.europeana.api.iiif.v2.model;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v2.io.JsonConstants;

/**
 * @author Hugo
 * @since 29 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonPropertyOrder({CONTEXT, ID, TYPE, PROFILE})
public class Service extends IIIFv2Resource {

    @JsonProperty(JsonConstants.context)
    private String context;

    @JsonProperty(JsonConstants.type)
    private String type;

    @JsonProperty(JsonConstants.profile)
    private String profile;

    public Service(String id) {
        super(id);
    }

    /**
     * Initialize Service with id , context
     * @param id -
     * @param context -
     */
    public Service(String id, String context) {
        super(id);
        this.context = context;
    }

    protected Service() {}

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
