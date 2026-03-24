package eu.europeana.api.iiif.media;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import eu.europeana.api.iiif.generator.media.MediaGeneratorType;
import java.io.Serializable;

/**
 * @author Hugo Manguinhas
 * @since 20 Feb 2026
 */
/*

Example: 
<format mediaType="image/gif" label="GIF" category="Image" methodV2="supported" methodV3="supported"/>

 */
@JacksonXmlRootElement(localName = "format")
public class MediaType implements Serializable {

    @JacksonXmlProperty(localName =  "mediaType", isAttribute = true)
    private String mimeType;

    @JacksonXmlProperty(isAttribute = true)
    private String label;

    @JacksonXmlProperty(isAttribute = true)
    private MediaCategory category;

    @JacksonXmlProperty(isAttribute = true)
    private String methodV2;
    @JacksonXmlProperty(isAttribute = true)
    private String methodV3;


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

    public MediaGeneratorType getMethodV2() {
        return MediaGeneratorType.valueOf(methodV2.toUpperCase());
    }

    public MediaGeneratorType getMethodV3() {
        return MediaGeneratorType.valueOf(methodV3.toUpperCase());
    }

}