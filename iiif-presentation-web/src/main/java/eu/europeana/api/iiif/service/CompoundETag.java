/**
 * 
 */
package eu.europeana.api.iiif.service;

import eu.europeana.api.caching.ETag;
import eu.europeana.api.caching.WeakETag;

/**
 * @author Hugo
 * @since 22 Apr 2025
 */
public class CompoundETag implements ETag {

    private int    size;
    private ETag[] etags;

    public CompoundETag(ETag... etags) {
        this(etags.length, etags);
    }

    public CompoundETag(int size, ETag... etags) {
        this.etags = etags;
        this.size  = size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompoundETag parse(String etag) {
        ETag[] parts = new ETag[this.size];
        int index = 0, lastIndex = 0;
        int last = this.size - 1;
        for ( int i = 0; i < last; i++ ) {
            index = etag.indexOf('|', lastIndex);
            if ( index < 0 ) { return null; }
            if ( lastIndex < index ) {
                parts[i] = new WeakETag(etag.substring(lastIndex, index));
            }
            lastIndex = index + 1;
        }
        if ( lastIndex < etag.length() ) {
            parts[last] = new WeakETag(etag.substring(lastIndex));
        }
        return new CompoundETag(parts);
    }

    @Override
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("W/\"");
        boolean first = true;
        for ( ETag etag : this.etags ) {
            if ( !first ) { sb.append("|"); }
            sb.append(etag == null ? "" : etag.getValue());
            first = false;
        }
        sb.append("\"");
        return sb.toString();
    }

    @Override
    public String getValue() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for ( ETag etag : this.etags ) {
            if ( !first ) { sb.append("|"); }
            sb.append(etag.getValue());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return format();
    }

    public ETag getFirstEtag() {
        return this.etags[0];
    }

    public ETag getEtag(int index) {
        return this.etags[index];
    }

    public boolean hasEtag(int index) {
        return ( this.etags[index] != null );
    }
}
