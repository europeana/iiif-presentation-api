/**
 * 
 */
package eu.europeana.api.iiif.v2.model;

import static eu.europeana.api.iiif.v2.io.JsonConstants.*;

import com.fasterxml.jackson.annotation.JsonCreator;
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
@JsonPropertyOrder({ lang, value })
@JsonIgnoreProperties(ignoreUnknown=true)
public class LanguageValue {

    @JsonProperty(JsonConstants.value)
    private String value;

    @JsonProperty(JsonConstants.lang)
    private String lang;

    /**
     */
    @JsonCreator
    public LanguageValue(String value) {
        this(value, null);
    }

    public LanguageValue(String value, String lang) {
        this.value = value;
        this.lang  = lang;
    }

    protected LanguageValue() {}

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLang() {
        return this.lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean hasLang() {
        return (this.lang != null);
    }

    /**
     * @return textual representation of the contents of the metadata object (for debugging purposes)
     */
    @Override
    public String toString() {
        return "value: " + this.value + " lang: " + this.lang;
    }

}