/**
 * 
 */
package eu.europeana.api.iiif.v2.model;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v2.io.JsonConstants;

/**
 * @author Hugo
 * @since 24 Oct 2024
 */

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ label, value })
@JsonIgnoreProperties(ignoreUnknown=true)
public class LabelledValue {

    @JsonProperty(JsonConstants.label)
    private String label;

    @JsonProperty(JsonConstants.value)
    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY
                       , JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED } )
    private List<LanguageValue> value;

    /**
     * Create a new MetaData object
     * @param label languagemap containing metadata field label information in various languages
     * @param value languagemap containing metadata field value information in various lanuages
     */
    public LabelledValue(String label, List<LanguageValue> value) {
        this.label = label;
        this.value = value;
    }

    protected LabelledValue() {}

    public String getLabel() {
        return label;
    }

    public List<LanguageValue> getValue() {
        return value;
    }

    /**
     * @return textual representation of the contents of the metadata object (for debugging purposes)
     */
    @Override
    public String toString() {
        return "label: " + this.label + " value: " + this.value;
    }

}