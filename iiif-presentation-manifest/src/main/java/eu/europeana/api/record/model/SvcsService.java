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
public class SvcsService {

	@JsonProperty("about")
	private String id;

	@JsonProperty("label")
	@JsonDeserialize(converter = ConverterUtils.ToLanguageMap.class)
	private LanguageMap label = new LanguageMap();

	@JsonProperty("dctermsConformsTo")
	@JsonDeserialize(converter = ConverterUtils.ToString.class)
	private String conformsTo;

	@JsonProperty("doapImplements")
	@JsonDeserialize(converter = ConverterUtils.ToString.class)
	private String impls;


    public String getId() {
        return id;
    }

	public String getImplements() { 
		return impls;
	}

	public String getConformsTo() {
		return conformsTo;
	}

	public LanguageMap getLabel() {
		return label;
	}
}
