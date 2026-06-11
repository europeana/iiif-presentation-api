/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.content.Dataset;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.iiif.v3.model.content.Model;
import eu.europeana.api.iiif.v3.model.content.Sound;
import eu.europeana.api.iiif.v3.model.content.Text;
import eu.europeana.api.iiif.v3.model.content.Video;

/**
 * @author Hugo
 * @since 29 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY
            , property = TYPE)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Dataset.class, name = DATASET)
  , @JsonSubTypes.Type(value = Image.class  , name = IMAGE)
  , @JsonSubTypes.Type(value = Model.class  , name = MODEL)
  , @JsonSubTypes.Type(value = Sound.class  , name = SOUND)
  , @JsonSubTypes.Type(value = Text.class   , name = TEXT)
  , @JsonSubTypes.Type(value = Video.class  , name = VIDEO)
})
@JsonPropertyOrder({ID, TYPE, LABEL, LANGUAGE, FORMAT, SERVICE})
public abstract class ContentResource extends IIIFv3Resource {

    @JsonProperty(JsonConstants.LABEL)
    private LanguageMap label;

    @JsonProperty(JsonConstants.LANGUAGE)
    private String  originalLanguage;

    @JsonProperty(JsonConstants.FORMAT)
    private String format;

    @JsonProperty(JsonConstants.SERVICE)
    //temporary until we fix our data
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Service> service;


    public ContentResource(String id) {
        super(id);
    }

    public ContentResource(String id, LanguageMap label, String format) {
        super(id);
        this.label  = label;
        this.format = format;
    }

    protected ContentResource() {}


    public LanguageMap getLabel() {
        if ( label == null ) { label = new LanguageMap(); }
        return this.label;
    }

    public void setLabel(LanguageMap label) {
        this.label = label;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }


    public String getLanguage() {
        return originalLanguage;
    }

    public void setLanguage(String language) {
        this.originalLanguage = language;
    }

    public boolean hasServices() {
        return ( this.service != null && !this.service.isEmpty() );
    }

    public List<Service> getServices() {
        return ( this.service != null ? this.service
                                      : (this.service = new ArrayList<>()));
    }
}
