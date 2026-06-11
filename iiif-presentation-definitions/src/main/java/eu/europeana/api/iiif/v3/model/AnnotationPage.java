/**
 * 
 */
package eu.europeana.api.iiif.v3.model;

import static eu.europeana.api.iiif.v3.io.JsonConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.fulltext.FullTextAnnotationPage;

/**
 * @author Hugo
 * @since 29 Oct 2024
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(defaultImpl = AnnotationPage.class, use = Id.DEDUCTION)
@JsonSubTypes({ 
   @JsonSubTypes.Type(FullTextAnnotationPage.class)
})
@JsonPropertyOrder({ID, TYPE, ITEMS})
@SuppressWarnings("javaarchitecture:S7027")
public class AnnotationPage extends IIIFv3Resource {

    @JsonProperty(JsonConstants.ITEMS)
    private List<Annotation> items;


    public AnnotationPage(String id) {
        super(id);
    }

    protected AnnotationPage() {}

    @Override
    @JsonProperty(JsonConstants.TYPE)
    public String getType() {
        return JsonConstants.ANNOTATION_PAGE;
    }

    public boolean hasItems() {
        return ( this.items != null && !this.items.isEmpty() );
    }

    public List<Annotation> getItems() {
        return ( this.items != null ? this.items
                                    : (this.items = new ArrayList<>()));
    }
}
