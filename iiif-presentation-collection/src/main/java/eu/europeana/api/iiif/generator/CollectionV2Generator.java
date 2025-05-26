package eu.europeana.api.iiif.generator;

import static eu.europeana.api.iiif.generator.GeneratorUtils.*;

import eu.europeana.api.iiif.v2.model.Collection;
import eu.europeana.api.iiif.v2.model.Dataset;
import eu.europeana.api.iiif.v2.model.Image;
import eu.europeana.api.iiif.v2.model.LanguageValue;
import eu.europeana.api.iiif.v2.model.Manifest;
import eu.europeana.api.iiif.v2.model.ResourceReference;
import eu.europeana.api.iiif.v2.model.ViewingHint;
import eu.europeana.set.client.model.result.RecordPreview;
import eu.europeana.set.definitions.model.UserSet;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public class CollectionV2Generator implements CollectionGenerator<Collection>
                                            , GeneratorConstants {

    private static LanguageValue ROOT_LABEL               
        = new LanguageValue(GeneratorConstants.ROOT_LABEL, LANG_META);
    private static LanguageValue ROOT_DESCRIPTION         
        = new LanguageValue(GeneratorConstants.ROOT_DESCRIPTION, LANG_META);
    private static LanguageValue ROOT_GALLERY_LABEL       
        = new LanguageValue(GeneratorConstants.ROOT_GALLERY_LABEL, LANG_META);
    private static LanguageValue ROOT_GALLERY_DESCRIPTION 
        = new LanguageValue(GeneratorConstants.ROOT_GALLERY_DESCRIPTION, LANG_META);
    private static Image         EUROPEANA_LOGO        
        = new Image(GeneratorConstants.EUROPEANA_LOGO);

    @Resource
    private CollectionSettings settings;

    public CollectionV2Generator(CollectionSettings settings) {
        this.settings = settings;
    }

    @Override
    public Collection generateRoot() {
        Collection col = new Collection(settings.getCollectionRootURI());
        col.setLabel(ROOT_LABEL);
        col.setDescription(ROOT_DESCRIPTION);
        col.setViewingHint(ViewingHint.individuals);
        col.setLogo(EUROPEANA_LOGO);
        col.getCollections().add(new Collection(settings.getGalleryRootURI()));
        return col;
    }

    @Override
    public Collection generateGalleryRoot(java.util.Collection<? extends UserSet> sets) {
        Collection col = new Collection(settings.getGalleryRootURI());
        col.setLabel(ROOT_GALLERY_LABEL);
        col.setDescription(ROOT_GALLERY_DESCRIPTION);
        col.setViewingHint(ViewingHint.individuals);
        col.setLogo(EUROPEANA_LOGO);
        for (UserSet set : sets) {
            Collection child = new Collection(buildUrlWithSetId(
                    settings.getGalleryRootURI(), set.getIdentifier()));
            // get the first title for v2
            for (Map.Entry<String, String> entry : set.getTitle().entrySet()) {
                child.setLabel(new LanguageValue(entry.getKey(), entry.getValue()));
                break;
            }
            col.getCollections().add(child);
        }
        return col;
    }

    @Override
    public Collection generateGallery(UserSet set, List<RecordPreview> items) {
        Collection col = new Collection(
                buildUrlWithSetId(settings.getGalleryRootURI(), set.getIdentifier()));
        if (set.getTitle() != null) {
            col.setLabel(newValue(set.getTitle().values().iterator().next()));
        }
        col.setViewingHint(ViewingHint.individuals);
        col.setLogo(EUROPEANA_LOGO);
        col.getRelated().add(newReference(set));
        col.getSeeAlso().add(newDataset(set));
        if (items != null && items.size() > 0) {
            for (RecordPreview item : items) {
                col.getManifests().add(getManifest(item));
            }
        }
        return col;
    }


    protected Manifest getManifest(RecordPreview item) {
        Manifest manifest = new Manifest(
                StringUtils.replace(settings.getIIIfManifestUrl()
                                  , settings.getIIIFApiIdPlaceholder()
                                  , item.getId()));
        manifest.setThumbnail(newThumbnail(item));
        if (item.hasDescription()) {
            String description = item.getDescription().values().iterator().next().get(0);
            if (item.hasTitle()) {
                String title = item.getTitle().values().iterator().next().get(0);
                manifest.setLabel(newValue(title));
                manifest.setDescription(newValue(description));
            } else {
                manifest.setLabel(newValue(description));
            }
        }
        return  manifest;
    }

    protected LanguageValue newValue(String value) {
        return ( value == null ? null : new LanguageValue(value) );
    }

    protected Dataset newDataset(UserSet set) {
        Dataset ds = new Dataset(
                buildUrlWithSetId(settings.getSetApiBaseUrl()
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

    protected ResourceReference newReference(UserSet set) {
        // landing page https://www.europeana.eu/galleries/{setId}"
        ResourceReference ref = new ResourceReference(
                buildUrlWithSetId(settings.getGalleryLandingPage()
                                , set.getIdentifier()));
        ref.setLabel(WEBSITE_TITLE_GALLERY);
        ref.setFormat(MIMETYPE_HTML);
        return ref;
    }
}
