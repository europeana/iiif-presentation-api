/**
 * 
 */
package eu.europeana.api.iiif.generator;

import java.util.List;

import eu.europeana.api.iiif.model.IIIFResource;
import eu.europeana.set.client.model.result.RecordPreview;
import eu.europeana.set.definitions.model.UserSet;

/**
 * @author Hugo
 * @since 7 Apr 2025
 */
public interface CollectionGenerator<C extends IIIFResource> {

    C generateRoot();

    C generateGalleryRoot(java.util.Collection<? extends UserSet> sets);

    C generateGallery(UserSet set, List<RecordPreview> items);
}
