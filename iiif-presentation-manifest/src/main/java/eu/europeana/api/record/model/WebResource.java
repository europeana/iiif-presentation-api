package eu.europeana.api.record.model;

import java.util.Collection;
import java.util.HashMap;


import org.apache.commons.lang3.StringUtils;

import eu.europeana.api.iiif.media.MediaType;


/**
 * Class to help retrieve and sort web resources received from record JSON data
 * @author Patrick Ehlert
 * Created on 07-03-2018
 */
public class WebResource extends HashMap<String, Object> implements RecordConstants {

    private static final long serialVersionUID = -1726986203390766226L;

    private Collection<SvcsService> services;

    private MediaType mediaType;

    public WebResource() {
        super();
        // default constructor
    }

    /**
     * Create new webresource (for testing)
     * @param id String containing this webresource's id
     * @param isNextInSequence String containing the id of the webresource that's next in sequence
     */
    public WebResource(String id, String isNextInSequence) {
        super();
        super.put(ABOUT, id);
        super.put(EDM_NEXT_IN_SEQUENCE, isNextInSequence);
    }

    /**
     * @return the id of the webresource (in edm 'about' value)
     */
    public String getId() {
        return JsonUtils.asString(get(ABOUT));
    }

    public void setServices(Collection<SvcsService> services) {
        this.services = services;
    }

    public Collection<SvcsService> getServices() {
    	return services;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public MediaType getMediaType() {
    	return mediaType;
    }

    /**
     * @return true if the webresource has a isNextInSequence key with a non-empty value
     */
    public boolean hasNextInSequence() {
        return !StringUtils.isEmpty(this.getNextInSequence());
    }

    /**
     * @return the value of the isNextInSequence key, or null if there is no key
     */
    public String getNextInSequence() {
        return JsonUtils.asString(get(EDM_NEXT_IN_SEQUENCE));
    }

    public String getMimeType() {
        return JsonUtils.asString(get(EBUCORE_HAS_MIMETYPE));
    }

    public String getLicense() {
    	return JsonUtils.asString(get(WEB_RESOURCE_EDM_RIGHTS));
    }

    public Integer getHeight() {
	    return JsonUtils.asInteger(get(EBUCORE_HEIGHT));
    }

    public Integer getWidth() {
	    return JsonUtils.asInteger(get(EBUCORE_WIDTH));
    }

    public Long getDuration() {
	    return JsonUtils.asLong(get(EBUCORE_DURATION));
    }

    public Double getDurationInSeconds() {
	    Long duration = getDuration();
	    return ( duration == null ? null : duration / 1000D);
    }

    public Resolution getResolution() {
        Integer height = getHeight();
        Integer width  = getWidth();

        // if the WebResource does not have width or height
        // Set width and height to 400 (this is the size of the default icon which is what will likely be displayed)
        return (height != null && width != null ? new Resolution(width, height)
        									    : new Resolution(400,400) );
    }

    public String getAttributionText() {
        return JsonUtils.asString(this.get(TEXT_ATTRIB_SNIPPET));
    }


    public boolean hasService(String conformsTo) {
    	return ( getService(conformsTo) != null );
    }

    public SvcsService getService(String conformsTo) {
    	for ( SvcsService s : services ) {
    		if ( conformsTo.equals(s.getConformsTo()) ) { return s; }
    	}
    	return null;
    }
}
