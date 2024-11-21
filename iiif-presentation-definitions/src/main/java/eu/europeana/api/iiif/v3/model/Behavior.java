/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hugo
 * @since 19 Nov 2024
 */
public enum Behavior {

    @JsonProperty("auto-advance")    autoAdvance
  , @JsonProperty("no-auto-advance") noAutoAdvance
  , @JsonProperty("repeat")          repeat
  , @JsonProperty("no-repeat")       noRepeat
  , @JsonProperty("unordered")       unordered
  , @JsonProperty("individuals")     individuals
  , @JsonProperty("continuous")      continuous
  , @JsonProperty("paged")           paged
  , @JsonProperty("facing-pages")    facingPages
  , @JsonProperty("non-paged")       nonPaged
  , @JsonProperty("multi-part")      multiPart
  , @JsonProperty("together")        together
  , @JsonProperty("sequence")        sequence
  , @JsonProperty("thumbnail-nav")   thumbnailNav
  , @JsonProperty("no-nav")          noNav
  , @JsonProperty("hidden")          hidden
}
