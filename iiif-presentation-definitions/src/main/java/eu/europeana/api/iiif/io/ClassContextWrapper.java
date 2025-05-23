/**
 * 
 */
package eu.europeana.api.iiif.io;

import static eu.europeana.api.iiif.v2.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import eu.europeana.api.iiif.v2.io.JsonConstants;

/**
 * @author Hugo
 * @since 19 Nov 2024
 */
@JsonPropertyOrder(context)
public class ClassContextWrapper<T> {

    @JsonProperty(JsonConstants.context)
    private ResourceContext context;

    @JsonUnwrapped
    private T resource;

    public ClassContextWrapper(ResourceContext context, T resource) {
        this.context  = context;
        this.resource = resource;
    }
}
