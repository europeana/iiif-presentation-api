/**
 * 
 */
package eu.europeana.api.caching;

/**
 * @author Hugo
 * @since 22 Nov 2024
 */
public class WeakETag implements ETag {

    public static String formatAsWeakEtag(String value) {
        StringBuilder builder = new StringBuilder();
        builder.append("W/").append('"').append(value).append('"');
        return builder.toString();
    }

    private String value;

    public WeakETag() {}

    public WeakETag(String value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public WeakETag parse(String etag) {
        return new WeakETag(etag);
    }

    @Override
    public String format() {
        return formatAsWeakEtag(this.value);
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if ( o instanceof WeakETag ) {
            return this.value.equals(((WeakETag)o).getValue());
        }
        return false;
    }
    
    @Override
    public String toString() {
        return this.format();
    }
}
