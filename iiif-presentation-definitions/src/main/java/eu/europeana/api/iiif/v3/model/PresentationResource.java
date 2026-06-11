package eu.europeana.api.iiif.v3.model;


import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.*;

import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.content.Dataset;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.iiif.v3.model.content.Text;

/**
 * @author Hugo
 * @since 24 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = TYPE)
@JsonSubTypes({
  @JsonSubTypes.Type(value = Manifest.class  , name = MANIFEST)
, @JsonSubTypes.Type(value = Collection.class, name = COLLECTION)
, @JsonSubTypes.Type(value = Canvas.class    , name = CANVAS)
})
@SuppressWarnings("javaarchitecture:S7027")
public abstract class PresentationResource extends IIIFv3Resource {

    @JsonProperty(JsonConstants.LABEL)
    private LanguageMap label;

    @JsonProperty(JsonConstants.SUMMARY)
    private LanguageMap summary;

    @JsonProperty(JsonConstants.METADATA)
    private List<LabelledValue> metadata;

    @JsonProperty(JsonConstants.REQUIRED_STATEMENT)
    private LabelledValue requiredStatement;

    @JsonProperty(JsonConstants.RIGHTS)
    private Text rights;

    @JsonProperty(JsonConstants.PROVIDER)
    private List<Agent> provider;

    @JsonProperty(JsonConstants.HOMEPAGE)
    private List<Text> homepage;

    @JsonProperty(JsonConstants.SEE_ALSO)
    private List<Dataset> seeAlso;

    @JsonProperty(JsonConstants.SERVICE)
    private List<Service> service;

    @JsonProperty(JsonConstants.NAV_DATE)
    private String navDate;

    @JsonProperty(JsonConstants.BEHAVIOR)
    private List<Behavior> behavior;

    @JsonProperty(JsonConstants.THUMBNAIL)
    private List<Image> thumbnail;

    @JsonProperty(JsonConstants.RENDERING)
    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY
            , JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED } )
    private List<Image> rendering;

    @JsonProperty(JsonConstants.PLACEHOLDER_CANVAS)
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

    public boolean hasRendering() {
        return ( this.rendering != null && !this.rendering.isEmpty() );
    }

    public List<Image> getRendering() {
        return ( this.rendering != null ? this.rendering
                : (this.rendering = new ArrayList<>()));
    }
}
