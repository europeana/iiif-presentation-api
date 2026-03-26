package eu.europeana.api.record.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE
    , getterVisibility = Visibility.NONE)
public class ChangeLog {

    @JsonProperty
    private String type;
    @JsonProperty
    private String context;

    public String getType() {
        return type;
    }

    public String getContext() {
        return context;
    }
}
