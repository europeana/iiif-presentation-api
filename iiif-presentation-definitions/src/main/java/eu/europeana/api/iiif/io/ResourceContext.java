package eu.europeana.api.iiif.io;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Hugo
 * @since 15 Sep 2023
 */

@JsonSerialize(using = ContextSerializer.class)
public class ResourceContext {

    private String[] importURIs;
    private String   baseURI;

    public ResourceContext(String baseURI, String... importURIs) {
        this.importURIs = importURIs;
        this.baseURI    = baseURI; 
    }

    public String[] getImportURIs()  { return importURIs; }

    public boolean hasImportURIs() { 
        return ( importURIs != null && importURIs.length > 0 ); 
    }

    public String getBase() { return baseURI; }

    public boolean hasBase() { return baseURI != null; }
}