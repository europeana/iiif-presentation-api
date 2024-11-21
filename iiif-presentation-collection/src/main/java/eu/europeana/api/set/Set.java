/**
 * 
 */
package eu.europeana.api.set;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.europeana.api.iiif.v2.model.Manifest;
import eu.europeana.api.item.Item;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public class Set
{
    private String             id;
    private Map<String,String> title;
    private Map<String,String> description;
    private List<Item>         items;

    public Set(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public String getLocalID() {
        return id.substring(id.lastIndexOf('/')+1);
    }

    //titles
    public boolean hasTitle() {
        return (title != null && !title.isEmpty());
    }

    public Map<String,String> getTitle() {
        return title;
    }

    public String getAnyTitle() {
        return (hasTitle() ? title.values().iterator().next() : null);
    }

    public void setTitle(Map<String,String> title) {
        this.title = title;
    }

    //descriptions
    public boolean hasDescription() {
        return (description != null && !description.isEmpty());
    }

    public Map<String,String> getDescription() {
        return description;
    }

    public String getAnyDescription() {
        return (hasDescription() ? description.values().iterator().next() : null);
    }

    public void setDescription(Map<String,String> description) {
        this.description = description;
    }

    //items
    public boolean hasItems() {
        return ( this.items != null && !this.items.isEmpty() );
    }

    public List<Item> getItems() {
        return ( this.items != null ? this.items
                                    : (this.items = new ArrayList<>()));
    }

    public String getLandingPage() {
        return "https://www.europeana.eu/galleries/" + getLocalID();
    }
}
