package eu.europeana.api.iiif.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

/**
 * DTO for mapping the DePublication reasons fetched from remote.
 */
@JacksonXmlRootElement(localName="RDF" , namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DePubReasonResponse {

  @JacksonXmlProperty(localName = "Concept" , isAttribute = true, namespace = "http://www.w3.org/2004/02/skos/core#")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<Concept> conceptList;

  public List<Concept> getConceptList() {
    return conceptList;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Concept {
    @JacksonXmlProperty(isAttribute = true , namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private String about;
    @JacksonXmlProperty(isAttribute = true , namespace = "http://www.w3.org/2004/02/skos/core#")
    private String note;

    public String getAbout() {
      return about;
    }
    public String getNote() {
      return note;
    }
  }

}