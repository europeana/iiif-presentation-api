/**
 * 
 */
package eu.europeana.api.item;

import java.util.Map;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public class Item {

    private String id;
    private Map<String,String> title;
    private Map<String,String> description;
    private String preview;

    public Item(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public String getLocalID() {
        return id.replace("http://data.europeana.eu/item", "");
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

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }
}
