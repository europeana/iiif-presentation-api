/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.iiif.v3.model.content.Text;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonPropertyOrder({ id, type, logo, homepage })
public class Agent extends IIIFv3Resource {

    @JsonProperty(JsonConstants.logo)
    private List<Image> logo;

    @JsonProperty(JsonConstants.homepage)
    private List<Text> homepage;

    public Agent(String id, Image logo, Text homepage) {
        super(id);
        getHomepage().add(homepage);
        getLogo().add(logo);
    }

    public Agent(String id) {
        super(id);
    }

    protected Agent() {}

    @JsonProperty(JsonConstants.type)
    public String getType() {
        return JsonConstants.Agent;
    }

    public boolean hasHomepage() {
        return ( this.homepage != null && !this.homepage.isEmpty() );
    }

    public List<Text> getHomepage() {
        return ( this.homepage != null ? this.homepage
                                       : (this.homepage = new ArrayList<>(1)));
    }

    public boolean hasLogo() {
        return ( this.logo != null && !this.logo.isEmpty() );
    }

    public List<Image> getLogo() {
        return ( this.logo != null ? this.logo
                                   : (this.logo = new ArrayList<>(1)));
    }

}
