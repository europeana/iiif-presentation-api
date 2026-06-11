/**
 * 
 */
package eu.europeana.api.iiif.v3.model.content;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.ContentResource;

/**
 * @author Hugo
 * @since 7 Nov 2024
 */
@JsonPropertyOrder({ID, TYPE, LABEL, LANGUAGE, FORMAT, SERVICE})
public class Model extends ContentResource {

    public Model(String id) {
        super(id);
    }

    protected Model() {}

    @JsonProperty(JsonConstants.TYPE)
    public String getType() {
        return JsonConstants.MODEL;
    }
}
