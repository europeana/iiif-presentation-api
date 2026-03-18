package eu.europeana.api.record.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE
              , getterVisibility = Visibility.NONE)
public class EuropeanaAggregation {

    @JsonProperty("edmLandingPage")
    private String landingPage;

    @JsonProperty("edmPreview")
    private String preview;

    public String getLandingPage() {
        return landingPage;
    }

    public String getPreview() {
        return preview;
    }
}
