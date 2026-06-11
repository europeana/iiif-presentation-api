package eu.europeana.api.iiif.v3.model.content;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.ContentResource;
import eu.europeana.api.iiif.v3.model.LanguageMap;

/**
 * @author Hugo
 * @since 7 Nov 2024
 */
@JsonPropertyOrder({ID, TYPE, LABEL, LANGUAGE, FORMAT, SERVICE})
public class Text extends ContentResource {

    public Text(String id) {
        super(id);
    }

    public Text(String id, LanguageMap label, String format) {
        super(id, label, format);
    }

    protected Text() {}

    // Added JsonIgnore - fetches type value from the parent class @JsonTypeInfo
    // EA-4232 creates duplicate type value after parsing.
    @JsonProperty(JsonConstants.TYPE)
    public String getType() {
        return JsonConstants.TEXT;
    }
}
