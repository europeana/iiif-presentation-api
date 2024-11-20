/**
 * 
 */
package eu.europeana.api.iiif.v2.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hugo
 * @since 19 Nov 2024
 */
public enum ViewingHint {

    individuals
  , paged
  , continuous
  , @JsonProperty("multi-part") multiPart
  , @JsonProperty("non-paged") nonPaged
  , top
  , @JsonProperty("facing-pages") facingPages  
}