/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v3.io.JsonConstants;

/**
 * @author Hugo
 * @since 24 Oct 2024
 */

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ label, value })
@JsonIgnoreProperties(ignoreUnknown=true)
public class LabelledValue {

    @JsonProperty(JsonConstants.label)
    private LanguageMap label;

    @JsonProperty(JsonConstants.value)
    private LanguageMap value;

    /**
     * Create a new MetaData object
     * @param label languagemap containing metadata field label information in various languages
     * @param value languagemap containing metadata field value information in various lanuages
     */
    public LabelledValue(LanguageMap label, LanguageMap value) {
        this.label = label;
        this.value = value;
    }

    protected LabelledValue() {}

    public LanguageMap getLabel() {
        return label;
    }

    public LanguageMap getValue() {
        return value;
    }

    /**
     * @return textual representation of the contents of the metadata object (for debugging purposes)
     */
    @Override
    public String toString() {
        return "label: " + label + " value: " + value;
    }

}