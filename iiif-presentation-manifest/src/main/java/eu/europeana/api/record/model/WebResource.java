package eu.europeana.api.record.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.record.serialization.ConverterUtils;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE
              , getterVisibility = Visibility.NONE)
public class WebResource {

    @JsonProperty("about")
    private String id;

    private Record record;

    @JsonProperty("isNextInSequence")
    private String isNextInSequence;

    @JsonProperty("ebucoreHasMimeType")
    private String hasMimeType;

    private MediaType mediaType;

    @JsonProperty("ebucoreWidth")
    private Integer width;

    @JsonProperty("ebucoreHeight")
    private Integer height;

    @JsonProperty("ebucoreDuration")
    private Long duration;

    @JsonProperty("webResourceEdmRights")
    @JsonDeserialize(converter = ConverterUtils.ToString.class)
    private String rights;

    @JsonProperty("dctermsIsFormatOf")
    @JsonDeserialize(converter = ConverterUtils.ToListString.class)
    private List<String> isFormatOf;

    @JsonProperty("textAttributionSnippet")
    private String attributionSnippet;

    @JsonProperty("svcsHasService")
    @JsonDeserialize(converter = ConverterUtils.ToListString.class)
    private List<String> hasService;

    public String getId() {
        return id;
    }

    public Record getRecord() {
        return record;
    }

    public String getIsNextInSequence() {
        return isNextInSequence;
    }

    public boolean hasIsNextInSequence() {
        return (isNextInSequence != null);
    }

    public String getTextAttributionSnippet() {
        return attributionSnippet;
    }

    public String getMimeType() {
        return hasMimeType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getLicense() {
        return rights;
    }

    public Integer getHeight() {
        return height;
    }

    public Integer getWidth() {
        return width;
    }

    public Long getDuration() {
        return duration;
    }

    public Double getDurationInSeconds() {
        Long durationVal = getDuration();
        return ( durationVal == null ? null : durationVal / 1000D);
    }

    public Resolution getResolution() {
        Integer h = getHeight();
        Integer w  = getWidth();

        // if the WebResource does not have width or height
        // Set width and height to 400 (this is the size of the default icon which is what will likely be displayed)
        return (h != null && w != null ? new Resolution(w, h)
            : new Resolution(400, 400));
    }

    public boolean hasIsFormatOf() {
        return ( isFormatOf != null && !isFormatOf.isEmpty() );
    }

    public List<String> getIsFormatOf() {
        return isFormatOf;
    }

    public boolean hasServices() {
        return ( hasService != null && !hasService.isEmpty() );
    }

    public Collection<SvcsService> getServicesAsResources() {
        if ( !hasServices() ) { return Collections.emptyList(); }

        List<SvcsService> ret = new ArrayList<>(this.hasService.size());
        for ( String serviceVal : this.hasService ) {
            SvcsService service = record.getService(serviceVal);
            if (service != null) {
                ret.add(service);
            }
        }
        return ret;
    }

    public boolean hasServiceByConformsTo(String conformsTo) {
        return ( getServiceByConformsTo(conformsTo) != null );
    }

    public SvcsService getServiceByConformsTo(String conformsTo) {
        if ( !hasServices() ) { return null; }

        for (String hasServiceVal : this.hasService) {
            SvcsService service = record.getService(hasServiceVal);
            if (service != null
                && conformsTo.equals(service.getConformsTo())) {
                return service;
            }
        }
        return null;
    }

    protected void setRecord(Record rec) {
        this.record = rec;
    }
}
