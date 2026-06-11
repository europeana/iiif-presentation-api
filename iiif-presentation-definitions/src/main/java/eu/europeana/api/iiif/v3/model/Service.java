/**
 *
 */
package eu.europeana.api.iiif.v3.model;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v3.io.JsonConstants;

/**
 * @author Hugo
 * @since 29 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({CONTEXT, ID, TYPE, PROFILE, LABEL})
public class Service extends IIIFv3Resource {

    @JsonProperty(JsonConstants.CONTEXT)
    private String context;

    @JsonProperty(JsonConstants.TYPE)
    private String type;

    @JsonProperty(JsonConstants.PROFILE)
    private String profile;

    @JsonProperty(JsonConstants.LABEL)
    private LanguageMap label;

    public Service(String id, String type) {
        super(id);
        this.type = type;
    }

    protected Service() {
    }

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

    public LanguageMap getLabel() {
        if (label == null) {
            label = new LanguageMap();
        }
        return this.label;
    }

    public void setLabel(LanguageMap label) {
        this.label = label;
    }

}
