package eu.europeana.api.iiif.service;

import eu.europeana.api.iiif.generator.CollectionGenerator;
import eu.europeana.api.iiif.generator.ManifestGenerator;
import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.api.iiif.utils.IIIFConstants;

/**
 * @author Hugo
 * @since 7 Apr 2025
 */
public class IIIFVersionSupport {

    private String versionId;
    private String versionNr;

    private ManifestGenerator<?>   manifestGenerator;
    private CollectionGenerator<?> collectionGenerator;

    public IIIFVersionSupport(String versionId, String versionNr
                            , ManifestGenerator<?> manifestGenerator
                            , CollectionGenerator<?> collectionGenerator) {
        this.versionId = versionId;
        this.versionNr = versionNr;
        this.manifestGenerator   = manifestGenerator;
        this.collectionGenerator = collectionGenerator;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getVersionNr() {
        return versionNr;
    }

    public String getContentType() {
        return String.format(IIIFConstants.CONTENT_TYPE, getVersionId());
    }

    public ManifestGenerator<IIIFResource> getManifestGenerator() {
        return (ManifestGenerator<IIIFResource>)manifestGenerator;
    }

    public CollectionGenerator<?> getCollectionGenerator() {
        return collectionGenerator;
    }
}
