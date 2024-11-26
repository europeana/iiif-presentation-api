/**
 * 
 */
package eu.europeana.api.iiif.generator;

import static eu.europeana.api.iiif.generator.GeneratorUtils.*;

import eu.europeana.api.iiif.v2.model.Collection;
import eu.europeana.api.iiif.v2.model.Dataset;
import eu.europeana.api.iiif.v2.model.Image;
import eu.europeana.api.iiif.v2.model.LanguageValue;
import eu.europeana.api.iiif.v2.model.Manifest;
import eu.europeana.api.iiif.v2.model.ResourceReference;
import eu.europeana.api.iiif.v2.model.ViewingHint;
import eu.europeana.api.iiif.v3.model.LanguageMap;
import eu.europeana.api.item.Item;
import eu.europeana.api.set.Set;
import eu.europeana.set.definitions.model.UserSet;

import java.util.Map;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public class CollectionV2Generator implements GeneratorConstants {

    private static LanguageValue ROOT_LABEL               = new LanguageValue(GeneratorConstants.ROOT_LABEL, LANG_META);
    private static LanguageValue ROOT_DESCRIPTION         = new LanguageValue(GeneratorConstants.ROOT_DESCRIPTION, LANG_META);
    private static LanguageValue ROOT_GALLERY_LABEL       = new LanguageValue(GeneratorConstants.ROOT_GALLERY_LABEL, LANG_META);
    private static LanguageValue ROOT_GALLERY_DESCRIPTION = new LanguageValue(GeneratorConstants.ROOT_GALLERY_DESCRIPTION, LANG_META);
  //private static LanguageValue WEBSITE_TITLE_GALLERY    = new LanguageValue(GeneratorConstants.WEBSITE_TITLE_GALLERY, LANG_META);

    private static Image         EUROPEANA_LOGO        = new Image(GeneratorConstants.EUROPEANA_LOGO);

    public Collection generateRoot() {
        Collection col = new Collection(getRootURI());
        col.setLabel(ROOT_LABEL);
        col.setDescription(ROOT_DESCRIPTION);
        col.setViewingHint(ViewingHint.individuals);
        col.setLogo(EUROPEANA_LOGO);
        col.getCollections().add(new Collection(getGalleryRootURI()));
        return col;
    }

    public Collection generateGalleryRoot(java.util.Collection<? extends UserSet> sets) {
        Collection col = new Collection(getGalleryRootURI());
        col.setLabel(ROOT_GALLERY_LABEL);
        col.setDescription(ROOT_GALLERY_DESCRIPTION);
        col.setViewingHint(ViewingHint.individuals);
        col.setLogo(EUROPEANA_LOGO);
        for (UserSet set : sets) {
            Collection child = new Collection(getGalleryURI(set.getIdentifier()));
            // get the first title for v2
            for (Map.Entry<String, String> entry : set.getTitle().entrySet()) {
                child.setLabel(new LanguageValue(entry.getKey(), entry.getValue()));
                break;
            }
            col.getCollections().add(child);
        }
        return col;
    }

    public Collection generateGallery(Set set) {
        Collection col = new Collection(getGalleryURI(set.getLocalID()));
        col.setLabel(newValue(set.getAnyTitle()));
        col.setViewingHint(ViewingHint.individuals);
        col.setLogo(EUROPEANA_LOGO);
        col.getRelated().add(newReference(set));
        col.getSeeAlso().add(newDataset(set));
        if ( set.hasItems() ) {
            for ( Item item : set.getItems() ) {
                col.getManifests().add(getManifest(item));
            }
        }
        return col;
    }

    protected Manifest getManifest(Item item) {
        Manifest manifest = new Manifest(getManifestURI(item.getLocalID()));

        manifest.setThumbnail(newThumbnail(item));
        
        String title = item.getAnyTitle();
        if ( title == null ) {
            String description = item.getAnyDescription();
            manifest.setLabel(newValue(description));
            return manifest;
        }

        manifest.setLabel(newValue(title));
        manifest.setDescription(newValue(item.getAnyDescription()));
        return manifest;
    }

    protected LanguageValue newValue(String value) {
        return ( value == null ? null : new LanguageValue(value) );
    }

    protected Dataset newDataset(Set set) {
        Dataset ds = new Dataset(getSetURL(set.getLocalID(), EXTENSION_JSONLD));
        ds.setFormat(MIMETYPE_JSONLD);
        ds.setProfile(SET_JSONLD_CONTEXT);
        return ds;
    }

    protected Image newThumbnail(Item item) {
        return new Image(item.getPreview());
    }

    protected ResourceReference newReference(Set set) {
        ResourceReference ref = new ResourceReference(set.getLandingPage());
        ref.setLabel(WEBSITE_TITLE_GALLERY);
        ref.setFormat(MIMETYPE_HTML);
        return ref;
    }
}
