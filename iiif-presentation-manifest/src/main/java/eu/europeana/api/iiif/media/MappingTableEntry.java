package eu.europeana.api.iiif.media;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.api.iiif.generator.media.MediaGeneratorType;

/**
 * Represents the 'mapping' element from mediatypemapping.xml e.g.  *
<config>
	<mapping mediaType="image/jpg" methodV2="supported" methodV3="absent"/>
</config>
*/
@JacksonXmlRootElement(localName = "mapping")
public class MappingTableEntry {
  @JacksonXmlProperty(isAttribute = true)
  private String mediaType;
  @JacksonXmlProperty(isAttribute = true)
  private String methodV2;
  @JacksonXmlProperty(isAttribute = true)
  private String methodV3;

  public String getMediaType() {
    return mediaType;
  }

  public MediaGeneratorType getMethodV2() {
    return MediaGeneratorType.valueOf(methodV2.toUpperCase());
  }

  public MediaGeneratorType getMethodV3() {
    return MediaGeneratorType.valueOf(methodV3.toUpperCase());
  }
}