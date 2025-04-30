/**
 * 
 */
package eu.europeana.api.caching;

/**
 * @author Hugo
 * @since 22 Nov 2024
 */
public interface ETag {


    public <E extends ETag> E parse(String etag);

    public String getValue();

    public String format();
}
