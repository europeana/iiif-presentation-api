package eu.europeana.api.record.model;

import static eu.europeana.api.record.model.RecordUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.europeana.api.iiif.exceptions.DataInconsistentException;
import eu.europeana.api.record.serialization.ConverterUtils;
import eu.europeana.api.record.serialization.SerializationLifecycle;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE
              , getterVisibility = Visibility.NONE)
public class Aggregation implements SerializationLifecycle {

    @JsonProperty("about")
    private String id;

    @JsonProperty("edmIsShownAt")
    private String isShownAt;

    @JsonProperty("edmIsShownBy")
    private String isShownBy;

    @JsonProperty("hasView")
    private List<String> hasViews;

    @JsonProperty("webResources")
    private List<WebResource> webResources;

    private List<WebResource> orderedViews;

    @JsonProperty("edmRights")
    @JsonDeserialize(converter = ConverterUtils.ToString.class)
    private String rights;

    public String getId() {
        return id;
    }

    public String getIsShownAt() {
        return isShownAt;
    }

    public WebResource getIsShownAtResource() {
        return getWebResource(isShownAt);
    }

    public String getIsShownBy() {
        return isShownBy;
    }

    public WebResource getIsShownByResource() {
        return getWebResource(isShownBy);
    }

    public WebResource getWebResource(String id) {
        return searchForWebResource(id, webResources);
    }

    public List<WebResource> getOrderedViews() {
        return orderedViews;
    }

    public WebResource getView(String id) {
        return searchForWebResource(id, orderedViews);
    }

    public String getRights() {
        return rights;
    }

    @Override
    public void postDeserialize() {
        this.orderedViews = buildOrderedViews();
    }

    protected void setRecord(Record record) {
        if(webResources != null) {
            for (WebResource wr : webResources) {
                wr.setRecord(record);
            }
        }
    }

    private WebResource searchForWebResource(String id
                                           , Collection<WebResource> col) {
        if ( id == null || col== null ) { return null; }

        for ( WebResource wr : col ) {
            if ( id.equals(wr.getId()) ) { return wr; }
        }
        return null;
    }

    private List<WebResource> buildOrderedViews() {

        LinkedHashSet<String> views = new LinkedHashSet<String>();
        if (isEuScreen(isShownAt)) {
            views.add(isShownAt);
        }
        if (isShownBy != null) {
            views.add(isShownBy);
        }
        if (hasViews != null) {
            views.addAll(hasViews);
        }
        List<WebResource> unsorted = new ArrayList<>();
        for (String view : views) {
            WebResource wr = getWebResource(view);
            if (wr != null) {
                unsorted.add(wr);
            }
        }
        List<WebResource> sorted;
        try {
            sorted = WebResourceSorter.sort(unsorted, views);
        } catch (DataInconsistentException e) {
            //LOG.error("Error trying to sort webresources for {}. Cause: {}", europeanaId, e);
            sorted = unsorted;
        }
        return sorted;
    }
}