/**
 *
 */
package eu.europeana.api.iiif.generator;

import eu.europeana.api.iiif.v3.model.*;
import eu.europeana.api.iiif.v3.model.content.Dataset;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.iiif.v3.model.content.Text;
import eu.europeana.api.item.Item;
import eu.europeana.api.set.Set;
import eu.europeana.set.definitions.model.UserSet;

import static eu.europeana.api.iiif.generator.GeneratorUtils.*;
import static eu.europeana.api.iiif.generator.GeneratorUtils.buildGalleryUrl;


/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public class CollectionV3Generator implements GeneratorConstants {

    private static LanguageMap ROOT_LABEL = new LanguageMap(LANG_META, GeneratorConstants.ROOT_LABEL);
    private static LanguageMap ROOT_SUMMARY = new LanguageMap(LANG_META, ROOT_DESCRIPTION);
    private static LanguageMap ROOT_GALLERY_LABEL = new LanguageMap(LANG_META, GeneratorConstants.ROOT_GALLERY_LABEL);
    private static LanguageMap ROOT_GALLERY_SUMMARY = new LanguageMap(LANG_META, ROOT_GALLERY_DESCRIPTION);
    private static LanguageMap WEBSITE_TITLE_GALLERY = new LanguageMap(LANG_META, GeneratorConstants.WEBSITE_TITLE_GALLERY);

    private static Agent EUROPEANA = newEuropeanaProvider();

    public Collection generateRoot(String collectionRootUri, String galleryRootUri) {
        Collection col = new Collection(collectionRootUri);
        col.setLabel(ROOT_LABEL);
        col.setSummary(ROOT_SUMMARY);
        col.setViewingDirection(ViewingDirection.ltr);
        col.getProvider().add(EUROPEANA);
        col.getBehavior().add(Behavior.unordered);
        col.getItems().add(new Collection(galleryRootUri));
        return col;
    }

    public Collection generateGalleryRoot(String galleryRootUri, java.util.Collection<? extends UserSet> sets) {
        Collection col = new Collection(getGalleryRootURI(galleryRootUri));
        col.setLabel(ROOT_GALLERY_LABEL);
        col.setSummary(ROOT_GALLERY_SUMMARY);
        col.setViewingDirection(ViewingDirection.ltr);
        col.getProvider().add(EUROPEANA);
        col.getBehavior().add(Behavior.unordered);
        for (UserSet set : sets) {
            Collection child = new Collection(buildGalleryUrl(galleryRootUri, set.getIdentifier()));
            child.setLabel(new LanguageMap(set.getTitle()));
            col.getItems().add(child);
        }
        return col;
    }

    public Collection generateGallery(String iiifBaseUrl, Set set) {
        Collection col = new Collection(getGalleryURI(iiifBaseUrl, set.getLocalID()));
        if (set.hasTitle()) {
            col.setLabel(new LanguageMap(set.getTitle()));
        }
        if (set.hasDescription()) {
            col.setSummary(new LanguageMap(set.getDescription()));
        }
        col.setViewingDirection(ViewingDirection.ltr);
        col.getProvider().add(EUROPEANA);
        col.getHomepage().add(newReference(set));
        col.getSeeAlso().add(newDataset(set));
        col.getBehavior().add(Behavior.unordered);
        if (set.hasItems()) {
            for (Item item : set.getItems()) {
                col.getItems().add(getManifest(iiifBaseUrl, item));
            }
        }
        return col;
    }

    protected Manifest getManifest(String iiifBaseUrl, Item item) {
        Manifest manifest = new Manifest(getManifestURI(iiifBaseUrl, item.getLocalID()));

        manifest.getThumbnail().add(newThumbnail(item));

        if (!item.hasTitle()) {
            if (!item.hasDescription()) {
                return manifest;
            }
            manifest.setLabel(new LanguageMap(item.getDescription()));
            return manifest;
        }

        manifest.setLabel(new LanguageMap(item.getTitle()));

        if (!item.hasDescription()) {
            return manifest;
        }
        manifest.setSummary(new LanguageMap(item.getDescription()));

        return manifest;
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

    protected Text newReference(Set set) {
        Text ref = new Text(set.getLandingPage());
        ref.setLabel(WEBSITE_TITLE_GALLERY);
        ref.setFormat(MIMETYPE_HTML);
        return ref;
    }

    protected static Agent newEuropeanaProvider() {
        return new Agent(WEBSITE_PAGE_ABOUT,
                new Image(EUROPEANA_LOGO),
                new Text(WEBSITE_PAGE_LANDING,
                        new LanguageMap(LANG_META, EUROPEANA_NAME),
                        MIMETYPE_HTML));
    }
}
