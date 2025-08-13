/**
 * 
 */
package eu.europeana.api.iiif.v3.model.content;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.ContentResource;

/**
 * @author Hugo
 * @since 7 Nov 2024
 */
@JsonPropertyOrder({ id, type, label, language, format, service })
public class Model extends ContentResource {

    public Model(String id) {
        super(id);
    }

    protected Model() {}

    @JsonIgnore
    public String getType() {
        return JsonConstants.Model;
    }
}
