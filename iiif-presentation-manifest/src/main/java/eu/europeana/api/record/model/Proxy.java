package eu.europeana.api.record.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.europeana.api.iiif.v3.model.LanguageMap;
import eu.europeana.api.record.serialization.ConverterUtils;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE
              , getterVisibility = Visibility.NONE)
public class Proxy {

    @JsonProperty("dcTitle")
    @JsonDeserialize(converter = ConverterUtils.ToLanguageMap.class)
    private LanguageMap title = new LanguageMap();

    @JsonProperty("dcDescription")
    @JsonDeserialize(converter = ConverterUtils.ToLanguageMap.class)
    private LanguageMap description = new LanguageMap();

    @JsonProperty("dctermsIssued")
    @JsonDeserialize(converter = ConverterUtils.ToLanguageMap.class)
    private LanguageMap issued = new LanguageMap();

    @JsonProperty("dcDate")
    @JsonDeserialize(converter = ConverterUtils.ToLanguageMap.class)
    private LanguageMap date = new LanguageMap();

    @JsonProperty("dcFormat")
    @JsonDeserialize(converter = ConverterUtils.ToLanguageMap.class)
    private LanguageMap format = new LanguageMap();

    @JsonProperty("dcRelation")
    @JsonDeserialize(converter = ConverterUtils.ToLanguageMap.class)
    private LanguageMap relation = new LanguageMap();

    @JsonProperty("dcType")
    @JsonDeserialize(converter = ConverterUtils.ToLanguageMap.class)
    private LanguageMap type = new LanguageMap();

    @JsonProperty("dcLanguage")
    @JsonDeserialize(converter = ConverterUtils.ToLanguageMap.class)
    private LanguageMap language = new LanguageMap();

    @JsonProperty("dcSource")
    @JsonDeserialize(converter = ConverterUtils.ToLanguageMap.class)
    private LanguageMap source = new LanguageMap();

    public LanguageMap getTitleOrDescription() {
        return ( title.isEmpty() ? description : title );
    }

    public LanguageMap getDescription() {
        return description;
    }

    public LanguageMap getIssued() {
        return issued;
    }

    public LanguageMap getDate() {
        return date;
    }

    public LanguageMap getFormat() {
        return format;
    }

    public LanguageMap getRelation() {
        return relation;
    }

    public LanguageMap getType() {
        return type;
    }

    public LanguageMap getLanguage() {
        return language;
    }
    
    public LanguageMap getSource() {
        return source;
    }

    public void merge(Proxy proxy) {
        title.add(proxy.title);
        description.add(proxy.description);
        issued.add(proxy.issued);
        date.add(proxy.date);
        format.add(proxy.format);
        relation.add(proxy.relation);
        type.add(proxy.type);
        language.add(proxy.language);
        source.add(proxy.source);
    }
}
