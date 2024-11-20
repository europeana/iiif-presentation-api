/**
 * 
 */
package eu.europeana.api.iiif.v2.model;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v2.io.JsonConstants;

/**
 * @author Hugo
 * @since 29 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonPropertyOrder({ id, type, format, height, width, language, service })
public class AnnotationBody extends Image {

    @JsonProperty(JsonConstants.language)
    private String  originalLanguage;


    public AnnotationBody(String id) {
        super(id);
    }

    protected AnnotationBody() {}


    public String getLanguage() {
        return originalLanguage;
    }

    public void setLanguage(String language) {
        this.originalLanguage = language;
    }
}
