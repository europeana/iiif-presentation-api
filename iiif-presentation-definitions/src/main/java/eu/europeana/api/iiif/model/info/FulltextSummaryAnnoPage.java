package eu.europeana.api.iiif.model.info;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.api.iiif.model.ManifestDefinitions;
import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.IIIFv3Resource;

/**
 * Created by luthien on 15/04/2021.
 * Srishti - TODO create definitions module in Fulltext and use the same class
 */
public class FulltextSummaryAnnoPage extends IIIFv3Resource {

    private static final long serialVersionUID = -670619785903826924L;

    @JsonProperty("language")
    private String language;

    private String[] textGranularity;

    private String type;

    @JsonIgnore
    private boolean orig;

    @JsonProperty("source")
    private String source;

    public FulltextSummaryAnnoPage(){}

    /**
     * This object serves as a placeholder for either an original or translated AnnoPage
     * It is used in the summary info endpoint only
     *
     * @param id    String containing identifying URL of the FulltextSummaryAnnoPage
     * @param language  String containing language of the FulltextSummaryAnnoPage
     * @param orig  boolean is this the original language true / false
     */
    public FulltextSummaryAnnoPage(String id, String language, boolean orig, String source){
        super(id);
        this.language = language;
        this.orig = orig;
        this.source = source;
    }

    @JsonProperty(JsonConstants.type)
    public String getType() {
        return this.type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String[] getTextGranularity() {
        return textGranularity;
    }

    public void setTextGranularity(String[] textGranularity) {
        this.textGranularity = textGranularity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
