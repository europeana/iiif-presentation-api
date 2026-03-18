package eu.europeana.api.record.model;

/**
 * Helper class to convert an EDM date string to a Java LocalDate object
 * @author Patrick Ehlert
 * Created on 13-02-2018
 */
public class RecordUtils {

    public static boolean isEuScreen(String url) {
        return ( url != null && url.contains("://www.euscreen.eu/item.html") );
    }
}