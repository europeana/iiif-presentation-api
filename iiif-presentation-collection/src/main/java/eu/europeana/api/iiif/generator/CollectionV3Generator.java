package eu.europeana.api.iiif.generator;

import eu.europeana.api.iiif.v3.model.*;
import eu.europeana.api.iiif.v3.model.content.Dataset;
import eu.europeana.api.iiif.v3.model.content.Image;
import eu.europeana.api.iiif.v3.model.content.Text;
import eu.europeana.set.client.model.result.RecordPreview;
import eu.europeana.set.definitions.model.UserSet;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import static eu.europeana.api.iiif.generator.GeneratorUtils.*;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public class CollectionV3Generator implements CollectionGenerator<Collection>
                                            , GeneratorConstants {

    private static LanguageMap ROOT_LABEL 
        = new LanguageMap(LANG_META, GeneratorConstants.ROOT_LABEL);
    private static LanguageMap ROOT_SUMMARY 
        = new LanguageMap(LANG_META, ROOT_DESCRIPTION);
    private static LanguageMap ROOT_GALLERY_LABEL 
        = new LanguageMap(LANG_META, GeneratorConstants.ROOT_GALLERY_LABEL);
    private static LanguageMap ROOT_GALLERY_SUMMARY 
        = new LanguageMap(LANG_META, ROOT_GALLERY_DESCRIPTION);
    private static LanguageMap WEBSITE_TITLE_GALLERY 
        = new LanguageMap(LANG_META, GeneratorConstants.WEBSITE_TITLE_GALLERY);

    private static Agent EUROPEANA = newEuropeanaProvider();

    @Resource
    private CollectionSettings settings;

    public CollectionV3Generator(CollectionSettings settings) {
        this.settings = settings;
    }

    @Override
    public Collection generateRoot() {
        Collection col = new Collection(settings.getCollectionRootURI());
        col.setLabel(ROOT_LABEL);
        col.setSummary(ROOT_SUMMARY);
        col.setViewingDirection(ViewingDirection.ltr);
        col.getProvider().add(EUROPEANA);
        col.getBehavior().add(Behavior.unordered);
        col.getItems().add(new Collection(settings.getGalleryRootURI()));
        return col;
    }

    @Override
    public Collection generateGalleryRoot(java.util.Collection<? extends UserSet> sets) {
        Collection col = new Collection(settings.getGalleryRootURI());
        col.setLabel(ROOT_GALLERY_LABEL);
        col.setSummary(ROOT_GALLERY_SUMMARY);
        col.setViewingDirection(ViewingDirection.ltr);
        col.getProvider().add(EUROPEANA);
        col.getBehavior().add(Behavior.unordered);
        for (UserSet set : sets) {
            Collection child = new Collection(
                    buildUrlWithSetId(settings.getGalleryRootURI()
                                    , set.getIdentifier()));
            child.setLabel(getLanguageMap(set.getTitle()));
            col.getItems().add(child);
        }
        return col;
    }

    @Override
    public Collection generateGallery(UserSet set, List<RecordPreview> items) {
        Collection col = new Collection(buildUrlWithSetId(
                settings.getGalleryRootURI(), set.getIdentifier()));
        if (set.getTitle() != null) {
            col.setLabel(getLanguageMap(set.getTitle()));
        }
        if (set.getDescription() != null) {
            col.setSummary(getLanguageMap(set.getDescription()));
        }
        col.setViewingDirection(ViewingDirection.ltr);
        col.getProvider().add(EUROPEANA);
        col.getHomepage().add(newReference(set));
        col.getSeeAlso().add(newDataset(set));
        col.getBehavior().add(Behavior.unordered);
        if (items != null && items.size() > 0) {
            for (RecordPreview item : items) {
                col.getItems().add(getManifest(item));
            }
        }
        return col;
    }

    protected Manifest getManifest(RecordPreview item) {
        Manifest manifest = new Manifest(StringUtils.replace(
                settings.getIIIfManifestUrl(), settings.getIIIFApiIdPlaceholder()
              , item.getId()));
        manifest.getThumbnail().add(newThumbnail(item));

        if (!item.hasTitle()) {
            if (!item.hasDescription()) { return manifest; }

            manifest.setLabel(new LanguageMap(item.getDescription()));
            return manifest;
        }

        manifest.setLabel(new LanguageMap(item.getTitle()));

        if (!item.hasDescription()) { return manifest; }
        manifest.setSummary(new LanguageMap(item.getDescription()));

        return manifest;
    }

    protected Dataset newDataset(UserSet set) {
        Dataset ds = new Dataset(buildUrlWithSetId(settings.getSetApiBaseUrl()
                               , set.getIdentifier()) + "." + EXTENSION_JSONLD);
        ds.setFormat(MIMETYPE_JSONLD);
        ds.setProfile(SET_JSONLD_CONTEXT);
        return ds;
    }

    protected Image newThumbnail(RecordPreview item) {
        if (item.hasPreview()) {
            return new Image(item.getEdmPreview().get(0));
        }
        return null;
    }

    protected Text newReference(UserSet set) {
        Text ref = new Text(buildUrlWithSetId(settings.getGalleryLandingPage()
                                            , set.getIdentifier()));
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
