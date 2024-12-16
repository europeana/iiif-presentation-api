/**
 * 
 */
package eu.europeana.api.iiif.v3.model;


import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.content.Dataset;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.iiif.v3.model.content.Text;

/**
 * @author Hugo
 * @since 24 Oct 2024
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = type )
@JsonSubTypes({
  @JsonSubTypes.Type(value = Manifest.class  , name = Manifest)
, @JsonSubTypes.Type(value = Collection.class, name = Collection) 
, @JsonSubTypes.Type(value = Canvas.class    , name = Canvas) 
})
public abstract class PresentationResource extends IIIFv3Resource {

    @JsonProperty(JsonConstants.label)
    private LanguageMap label;

    @JsonProperty(JsonConstants.summary)
    private LanguageMap summary;

    @JsonProperty(JsonConstants.metadata)
    private List<LabelledValue> metadata;

    @JsonProperty(JsonConstants.requiredStatement)
    private LabelledValue requiredStatement;

    @JsonProperty(JsonConstants.rights)
    private Text rights;

    @JsonProperty(JsonConstants.provider)
    private List<Agent> provider;

    @JsonProperty(JsonConstants.homepage)
    private List<Text> homepage;

    @JsonProperty(JsonConstants.seeAlso)
    private List<Dataset> seeAlso;

    @JsonProperty(JsonConstants.service)
    private List<Service> service;

    @JsonProperty(JsonConstants.navDate)
    private String navDate;

    @JsonProperty(JsonConstants.behavior)
    private List<Behavior> behavior;

    @JsonProperty(JsonConstants.thumbnail)
    private List<Image> thumbnail;

    @JsonProperty(JsonConstants.rendering)
    private List<String> rendering;

    @JsonProperty(JsonConstants.placeholderCanvas)
    private Canvas placeholderCanvas;

    public PresentationResource(String id) {
        super(id);
    }

    protected PresentationResource() {
        super();
    }

    public LanguageMap getLabel() {
        if ( label == null ) { label = new LanguageMap(); }
        return this.label;
    }

    public void setLabel(LanguageMap label) {
        this.label = label;
    }

    public LanguageMap getSummary() {
        if ( summary == null ) { summary = new LanguageMap(); }
        return this.summary;
    }

    public void setSummary(LanguageMap summary) {
        this.summary = summary;
    }

    public boolean hasMetadata() {
        return ( this.metadata != null && !this.metadata.isEmpty() );
    }

    public List<LabelledValue> getMetadata() {
        return ( this.metadata != null ? this.metadata
                                       : (this.metadata = new ArrayList<>()));
    }

    public void addMetadata(LabelledValue metadata) {
        getMetadata().add(metadata);
    }

    public LabelledValue getRequiredStatement() {
        return this.requiredStatement;
    }

    public void setRequiredStatement(LabelledValue statement) {
        this.requiredStatement = statement;
    }


    public Text getRights() {
        return this.rights;
    }

    public void setRights(Text rights) {
        this.rights = rights;
    }


    public boolean hasProvider() {
        return ( this.provider != null && !this.provider.isEmpty() );
    }

    public List<Agent> getProvider() {
        return ( this.provider != null ? this.provider
                                       : (this.provider = new ArrayList<>(1)));
    }

    public boolean hasHomepage() {
        return ( this.homepage != null && !this.homepage.isEmpty() );
    }

    public List<Text> getHomepage() {
        return ( this.homepage != null ? this.homepage
                                       : (this.homepage = new ArrayList<>(1)));
    }

    public boolean hasSeeAlso() {
        return ( this.seeAlso != null && !this.seeAlso.isEmpty() );
    }

    public List<Dataset> getSeeAlso() {
        return ( this.seeAlso != null ? this.seeAlso
                                      : (this.seeAlso = new ArrayList<>()));
    }

    public void setSeeAlso(List<Dataset> seeAlso) {
        this.seeAlso = seeAlso;
    }

    public boolean hasServices() {
        return ( this.service != null && !this.service.isEmpty() );
    }

    public List<Service> getServices() {
        return ( this.service != null ? this.service
                                      : (this.service = new ArrayList<>()));
    }

    public void addService(Service service) {
        getServices().add(service);
    }


    public boolean hasThumbnail() {
        return ( this.thumbnail != null && !this.thumbnail.isEmpty() );
    }

    public List<Image> getThumbnail() {
        return ( this.thumbnail != null ? this.thumbnail
                                        : (this.thumbnail = new ArrayList<>()));
    }


    public String getNavDate() {
        return this.navDate;
    }

    public void setNavDate(String navDate) {
        this.navDate = navDate;
    }

    public boolean hasBehavior() {
        return ( this.behavior != null && !this.behavior.isEmpty() );
    }

    public List<Behavior> getBehavior() {
        return ( this.behavior != null ? this.behavior
                                       : (this.behavior = new ArrayList<>(1)));
    }


    public Canvas getPlaceholderCanvas() {
        return this.placeholderCanvas;
    }

    public void setPlaceholderCanvas(Canvas canvas) {
        this.placeholderCanvas = canvas;
    }
}
