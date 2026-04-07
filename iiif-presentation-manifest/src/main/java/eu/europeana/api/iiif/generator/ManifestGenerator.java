/**
 * 
 */
package eu.europeana.api.iiif.generator;

import java.util.Map;

import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.model.info.FulltextSummaryCanvas;
import eu.europeana.api.record.model.Record;

/**
 * @author Hugo
 * @since 7 Apr 2025
 */
public interface ManifestGenerator<M extends IIIFResource> {

    /**
     * Generates a IIIF manifest based on the provided (parsed) json document
     * @param record  Record object
     * @return IIIF Manifest
     */
    M generateManifest(Record record);

    //void fillWithFullText(M manifest, URL fullTextApi, AuthenticationHandler auth) throws EuropeanaApiException;

    void fillWithFullText(M manifest, Map<String, FulltextSummaryCanvas> summary);
}
