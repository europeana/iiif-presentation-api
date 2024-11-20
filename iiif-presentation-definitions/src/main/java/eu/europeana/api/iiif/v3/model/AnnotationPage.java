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
@JsonPropertyOrder({ id, type, items })
public class AnnotationPage extends IIIFv3Resource {

    @JsonProperty(JsonConstants.items)
    private List<Annotation> items;


    public AnnotationPage(String id) {
        super(id);
    }

    protected AnnotationPage() {}

    @Override
    @JsonProperty(JsonConstants.type)
    public String getType() {
        return JsonConstants.AnnotationPage;
    }

    public boolean hasItems() {
        return ( this.items != null && !this.items.isEmpty() );
    }

    public List<Annotation> getItems() {
        return ( this.items != null ? this.items
                                    : (this.items = new ArrayList<>()));
    }
}
