package eu.europeana.api.iiif.generator;

import java.util.List;

import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.set.client.model.result.RecordPreview;
import eu.europeana.set.definitions.model.UserSet;

/**
 * Interface to generate Collection
 * @author Hugo
 * @since 7 Apr 2025
 */
public interface CollectionGenerator<C extends IIIFResource> {

    /**
     * Generates the root collection
     * @return IIIFResource
     */
    C generateRoot();

    /**
     * Generates gallery Root collection for the UserSets
     * @param sets galleries for the root collection
     * @return IIIFResource
     */
    C generateGalleryRoot(java.util.Collection<? extends UserSet> sets);

    /**
     * Generate Gallery collection for the user set and
     * the items associated with the user set
     * @param set user set
     * @param items items of the user set
     * @return IIIFResource
     */
    C generateGallery(UserSet set, List<RecordPreview> items);
}
