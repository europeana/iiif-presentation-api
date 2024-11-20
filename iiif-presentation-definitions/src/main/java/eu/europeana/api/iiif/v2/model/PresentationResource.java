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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.europeana.api.iiif.v2.io.JsonConstants;

/**
 * @author Hugo
 * @since 24 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = type )
@JsonSubTypes({
    @JsonSubTypes.Type(value = Collection.class, name = Collection) 
  , @JsonSubTypes.Type(value = Manifest.class  , name = Manifest)
  , @JsonSubTypes.Type(value = Sequence.class  , name = Sequence) 
  , @JsonSubTypes.Type(value = Canvas.class    , name = Canvas) 
})

public abstract class PresentationResource extends IIIFv2Resource {

    @JsonProperty(JsonConstants.label)
    private LanguageValue label;
    
    @JsonProperty(JsonConstants.description)
    private LanguageValue description;

    @JsonProperty(JsonConstants.metadata)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<LabelledValue> metadata;

    @JsonProperty(JsonConstants.attribution)
    private String attribution;
    
    @JsonProperty(JsonConstants.license)
    private String license;

    @JsonProperty(JsonConstants.logo)
    private Image logo;

    @JsonProperty(JsonConstants.seeAlso)
    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY
                       , JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED } )
    private List<Dataset> seeAlso;

    @JsonProperty(JsonConstants.service)
    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY
                       , JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED } )
    private List<Service> service;

    @JsonProperty(JsonConstants.within)
    private String within;
    
    @JsonProperty(JsonConstants.related)
    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY
                       , JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED } )
    private List<ResourceReference> related;

    @JsonProperty(JsonConstants.viewingHint)
    private ViewingHint viewingHint;

    @JsonProperty(JsonConstants.thumbnail)
    private Image thumbnail;

    @JsonProperty(JsonConstants.rendering)
    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY
                       , JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED } )
    private List<Image> rendering;


    public PresentationResource(String id) {
        super(id);
    }

    protected PresentationResource() {
        super();
    }


    public LanguageValue getLabel() {
        return this.label;
    }

    public void setLabel(LanguageValue value) {
        this.label = value;
    }


    public LanguageValue getDescription() {
        return this.description;
    }

    public void setDescription(LanguageValue value) {
        this.description = value;
    }


    public boolean hasMetadata() {
        return ( this.metadata != null && !this.metadata.isEmpty() );
    }

    public List<LabelledValue> getMetadata() {
        return ( this.metadata != null ? this.metadata
                                       : (this.metadata = new ArrayList<>()));
    }


    public String getAttribution() {
        return this.attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }


    public String getLicense() {
        return this.license;
    }

    public void setLicense(String license) {
        this.license = license;
    }


    public Image getLogo() {
        return this.logo;
    }

    public void setLogo(Image logo) {
        this.logo = logo;
    }


    public boolean hasSeeAlso() {
        return ( this.seeAlso != null && !this.seeAlso.isEmpty() );
    }

    public List<Dataset> getSeeAlso() {
        return ( this.seeAlso != null ? this.seeAlso
                                       : (this.seeAlso = new ArrayList<>()));
    }


    public boolean hasServices() {
        return ( this.service != null && !this.service.isEmpty() );
    }

    public List<Service> getServices() {
        return ( this.service != null ? this.service
                                      : (this.service = new ArrayList<>()));
    }


    public String getWithin() {
        return this.within;
    }

    public void setWithin(String within) {
        this.within = within;
    }


    public boolean hasRelated() {
        return ( this.related != null && !this.related.isEmpty() );
    }

    public List<ResourceReference> getRelated() {
        return ( this.related != null ? this.related
                                      : (this.related = new ArrayList<>()));
    }


    public ViewingHint getViewingHint() {
        return this.viewingHint;
    }

    public void setViewingHint(ViewingHint hint) {
        this.viewingHint = hint;
    }


    public Image getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
    }


    public boolean hasRendering() {
        return ( this.rendering != null && !this.rendering.isEmpty() );
    }

    public List<Image> getRendering() {
        return ( this.rendering != null ? this.rendering
                                        : (this.rendering = new ArrayList<>()));
    }
}
