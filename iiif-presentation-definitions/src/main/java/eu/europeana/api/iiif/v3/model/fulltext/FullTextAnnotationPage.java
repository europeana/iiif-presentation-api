/**
 * 
 */
package eu.europeana.api.iiif.v3.model.fulltext;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.AnnotationPage;

/**
 * @author Hugo
 * @since 20 Nov 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ID, TYPE, LANGUAGE, TEXT_GRANULARITY, SOURCE, ITEMS})
@JsonIgnoreProperties(ignoreUnknown=true)
public class FullTextAnnotationPage extends AnnotationPage {

    @JsonProperty(JsonConstants.LANGUAGE)
    private String language;

    @JsonProperty(JsonConstants.TEXT_GRANULARITY)
    private List<TextGranularity> textGranularity;

    @JsonProperty(JsonConstants.SOURCE)
    private String source;

    public FullTextAnnotationPage(String id) {
        super(id);
    }

    public FullTextAnnotationPage(String id, String language, List<TextGranularity> textGranularity, String source) {
        super(id);
        this.language = language;
        this.textGranularity = textGranularity;
        this.source = source;
    }

    protected FullTextAnnotationPage() {}

    
    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean hasTextGranularity() {
        return ( this.textGranularity != null && !this.textGranularity.isEmpty() );
    }

    public List<TextGranularity> getTextGranularity() {
        return ( this.textGranularity != null ? this.textGranularity
                                      : (this.textGranularity = new ArrayList<>()));
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
