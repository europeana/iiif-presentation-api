package eu.europeana.api.iiif.media;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * @author Hugo Manguinhas
 * @since 20 Feb 2026
 */
/*

Example: 
<format mediaType="image/jpg" label="JPG" category="Image"/>

 */
@JacksonXmlRootElement(localName = "format")
public class MediaType {

    @JacksonXmlProperty(localName =  "mediaType", isAttribute = true)
    private String mimeType;

    @JacksonXmlProperty(isAttribute = true)
    private String label;

    @JacksonXmlProperty(isAttribute = true)
    private MediaCategory category;

    public String getMimeType() {
        return mimeType;
    }

    public String getLabel() {
        return label;
    }

    public MediaCategory getCategory() {
        return category;
    }

    public boolean isAudioVisual() {
        return category.isAudioVisual() ;
    }
}
