package eu.europeana.api.iiif.io;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Class to generate context with uris and base Uri
 * @author Hugo
 * @since 15 Sep 2023
 */

@JsonSerialize(using = ContextSerializer.class)
public class ResourceContext {

    private String[] importURIs;
    private String   baseURI;

    /**
     * Constructor for creating ResourceContext with base and multiple import uri's
     * @param baseURI base uri for the context
     * @param importURIs import uri's
     */
    public ResourceContext(String baseURI, String... importURIs) {
        //Private mutable members should not be stored or returned directly java:S2384
        this.importURIs = importURIs.clone();
        this.baseURI    = baseURI; 
    }

    public String[] getImportURIs()  { return this.importURIs.clone(); }

    /**
     * returns true if imprort uri's has values
     * @return true or false
     */
    public boolean hasImportURIs() { 
        return ( importURIs != null && importURIs.length > 0 ); 
    }

    public String getBase() { return baseURI; }

    /**
     * Returns true if ResourceContext has base value
     * @return true or false
     */
    public boolean hasBase() { return baseURI != null; }
}
