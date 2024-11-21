/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hugo
 * @since 19 Nov 2024
 */
public enum ViewingDirection {

    @JsonProperty("left-to-right") ltr
  , @JsonProperty("right-to-left") rtl
  , @JsonProperty("top-to-bottom") ttb
  , @JsonProperty("bottom-to-top") btt
}
