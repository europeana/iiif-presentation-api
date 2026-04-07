package eu.europeana.api.iiif.media;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.api.iiif.generator.media.MediaGeneratorType;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author srishti singh
 * @since 18 April 2023
 */

/*

Example: 
<config>
	<format mediaType="image/gif" label="GIF" category="Image" methodV2="supported" methodV3="supported"/>
	...
</config>

*/

@JacksonXmlRootElement(localName = "config")
public class MediaTypeCatalog {

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "format")
        public List<MediaType> mediaTypeCategories;

        private Map<String, MediaType> map = new HashMap<>();

         /**
         * Map contains all the suppoerted media types except EU Screen entries
         * @return
         */
         public Map<String, MediaType> getMap() {
             return this.map;
         }


        /**
         * Checks if a media Type is configured for the given mime Type
         *
         * @param mimeType mime type to match
         * @return true if a Media Type match is configured, false otherwise.
         */
        public boolean hasMediaType(String mimeType) {
            return map.containsKey(mimeType);
        }

        /**
         * Gets the configured media Type for the given entity mime type
         *
         * @param mimetype entity ID
         * @return Matching media Type, or empty Optional if none found
         */
        public Optional<MediaType> getMediaType(String mimetype) {
            if (StringUtils.isNotEmpty(mimetype)) {
                return Optional.ofNullable(map.get(mimetype));
            }
            return Optional.empty();
        }


  public MediaGeneratorType getGeneratorMethodV2(String mimetype) {
    Optional<MediaType> mediaType = Optional.ofNullable(map.get(mimetype));
    return mediaType.isPresent() ? mediaType.get().getMethodV2() : null;
  }

  public MediaGeneratorType getGeneratorMethodV3(String mimetype) {
    Optional<MediaType> mediaType = Optional.ofNullable(map.get(mimetype));
    return mediaType.isPresent() ? mediaType.get().getMethodV3() : null;
  }

}
