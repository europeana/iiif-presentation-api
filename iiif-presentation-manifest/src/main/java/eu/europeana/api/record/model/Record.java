package eu.europeana.api.record.model;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import eu.europeana.api.record.serialization.SerializationLifecycle;
import org.apache.commons.lang3.StringUtils;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE
              , getterVisibility = Visibility.NONE)
public class Record implements SerializationLifecycle {

    @JsonProperty("about")
    private String id;

    @JsonProperty("europeanaAggregation")
    private EuropeanaAggregation europeanaAggr;

    @JsonProperty("aggregations")
    private List<Aggregation> aggregations;

    @JsonProperty("proxies")
    private List<Proxy> proxies;

    @JsonProperty("services")
    private List<SvcsService> services;

    private boolean isArchived;

    public String getId() {
        return id;
    }

    public Aggregation getProviderAggregation() {
        return aggregations.get(0);
    }

    public Proxy getProxy() {
        return proxies.get(0);
    }

    public SvcsService getService(String id) {
        if (services == null || id == null) {
            return null;
        }

        for ( SvcsService service : services ) {
            if (id.equals(service.getId())) {
                return service;
            }
        }
        return null;
    }

    public String getPreview() {
        return (europeanaAggr != null) ? europeanaAggr.getPreview() : null;
    }

    public String getLandingPage() {
        return (europeanaAggr != null) ? europeanaAggr.getLandingPage() : null;
    }

    public ChangeLog getChangeLogByType(String type) {
        if (StringUtils.isNotEmpty(type) && europeanaAggr != null ) {
            for (ChangeLog c : europeanaAggr.getChangeLogs()) {
                if (type.equals(c.getType())) {
                    return c;
                }
            }
        }
        return null;
    }


    @Override
    public void postDeserialize() {
        removeAggregations();
        mergeProxies();
    }

    private void removeAggregations() {
        Iterator<Aggregation> iter = aggregations.iterator();
        while ( iter.hasNext() ) {
            Aggregation aggr = iter.next();
            if ( aggr.getId().contains("/provider/") ) { 
                aggr.setRecord(this);
                continue; 
            }
            iter.remove();
        }
    }

    private void mergeProxies() {
        Iterator<Proxy> iter = proxies.iterator();
        Proxy master = iter.next();
        while ( iter.hasNext() ) {
            Proxy proxy = iter.next();
            master.merge(proxy);
            iter.remove();
        }
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }
}
