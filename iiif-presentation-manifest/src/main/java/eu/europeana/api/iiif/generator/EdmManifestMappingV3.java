package eu.europeana.api.iiif.generator;

import eu.europeana.api.commons_sb3.definitions.iiif.AcceptUtils;
import eu.europeana.api.iiif.generator.media.MediaGeneratorRegistry;
import eu.europeana.api.iiif.generator.media.MediaGeneratorType;
import eu.europeana.api.iiif.generator.media.MediaGeneratorVersion;
import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.media.MediaTypeCatalog;
import eu.europeana.api.iiif.model.info.FulltextSummaryAnnoPage;
import eu.europeana.api.iiif.model.info.FulltextSummaryCanvas;
import eu.europeana.api.iiif.v3.model.*;
import eu.europeana.api.iiif.v3.model.content.*;
import eu.europeana.api.iiif.v3.model.fulltext.FullTextAnnotationPage;
import eu.europeana.api.record.model.Aggregation;
import eu.europeana.api.record.model.Proxy;
import eu.europeana.api.record.model.Record;
import eu.europeana.api.record.model.WebResource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static eu.europeana.api.iiif.generator.ManifestGeneratorUtils.*;

import static eu.europeana.api.iiif.generator.ManifestGeneratorConstants.*;

/**
 * This class contains all the methods for mapping EDM record data to IIIF Manifest data for IIIF v3
 *
 * @author Srishti Singh
 * Created on 25-03-2020
 *
 * Updated By Lúthien
 * modified on 15-02-2023
 */
// ignore sonarqube rule: we return null on purpose in this class
// ignore pmd rule:  we want to make a clear which objects are v2 and which v3
public final class EdmManifestMappingV3 implements ManifestGenerator<Manifest> {

    private static final Logger LOG = LogManager.getLogger(EdmManifestMappingV3.class);
    private static final MediaGeneratorVersion VERSION = MediaGeneratorVersion.V3;

    private ManifestSettings       settings;
    private MediaTypeCatalog       mediaTypes;
    private MediaGeneratorRegistry registry;

    public EdmManifestMappingV3(ManifestSettings settings
                              , MediaTypeCatalog mediaTypes
                              , MediaGeneratorRegistry registry) {
        this.settings = settings;
        this.mediaTypes = mediaTypes;
        this.registry = registry;
    }

    /**
     * Generates a IIIF v3 manifest based on the provided (parsed) json document
     * @param jsonDoc parsed json document
     * @return IIIF Manifest v3 object
     */
    public Manifest generateManifest(Record record) {
        String europeanaId = record.getId();

        Manifest manifest = new Manifest(settings.getManifestId(europeanaId));
        manifest.getServices().add(getServiceDescriptionV3(settings, europeanaId));

        Aggregation aggr = record.getProviderAggregation();
        Proxy proxy = record.getProxy();
        manifest.setLabel(proxy.getTitleOrDescription());
        manifest.setSummary(proxy.getDescription());
        addMetaDataV3(proxy, manifest.getMetadata());
        manifest.getThumbnail().add(getThumbnailImage(record));
        manifest.setNavDate(getNavDate(proxy));
        addHomePage(record, manifest);
        manifest.setRequiredStatement(getAttribution(aggr));
        manifest.setRights(getRights(aggr));
        manifest.setSeeAlso(getDataSets(record));

        // get the canvas items and if present add to manifest
        addItems(record, manifest);

        addProvider(manifest);

        return manifest;
    }

    /**
     * We generate all full text links in one place, so we can raise a timeout if retrieving the necessary
     * data for all full texts is too slow.
     * From EA-2604 on, originalLanguage is available on the FulltextSummaryCanvas and copied to the AnnotationBody if
     * motivation = 'painting'
     */
    public void fillWithFullText(Manifest manifest, Map<String, FulltextSummaryCanvas> summary) {
        if ( summary == null ) { return; }

        if ( !manifest.hasItems() ) { return; }

        for (Canvas canvas : manifest.getItems()) {
            // we need to generate the same annopageId hash based on imageId
            String apHash = derivePageId(canvas.getStartCanvasAnnotation().getBody().getID());
            FulltextSummaryCanvas ftCanvas = summary.get(apHash);
            if (ftCanvas == null) {
                // This warning can be logged for empty pages that do not have a fulltext, but if we get a lot
                // then Record API and Fulltext API are not in sync (or the hashing algorithm changed).
                LOG.warn("Inconsistent data! No fulltext annopage found for record {} page {}. Generated hash = {}",
                        manifest.getID(), canvas.getID(), apHash);
            } else {
                addFulltextLinkToCanvas(canvas, ftCanvas);
            }
        }
    }

    private void addFulltextLinkToCanvas(eu.europeana.api.iiif.v3.model.Canvas canvas, FulltextSummaryCanvas summaryCanvas) {
        List<AnnotationPage> summaryAnnoPages = new ArrayList<>();
        createFTSummaryAnnoPages(summaryAnnoPages, summaryCanvas);
        canvas.getAnnotations().addAll(summaryAnnoPages);
        for (eu.europeana.api.iiif.v3.model.AnnotationPage ap : canvas.getItems()) {
            for (eu.europeana.api.iiif.v3.model.Annotation ann : ap.getItems()) {
                // for translations originalLanguage will be null
                if (StringUtils.equalsAnyIgnoreCase(ann.getMotivation(), "painting") && summaryCanvas.getOriginalLanguage() != null) {
                    ann.getBody().setLanguage(summaryCanvas.getOriginalLanguage());
                }
            }
        }
    }

    private void createFTSummaryAnnoPages(List<AnnotationPage> summaryAnnoPages, FulltextSummaryCanvas summaryCanvas) {
        for (FulltextSummaryAnnoPage sap : summaryCanvas.getFTSummaryAnnoPages()) {
            summaryAnnoPages.add(new FullTextAnnotationPage(sap.getID(), sap.getLanguage(), sap.getTextGranularity(), sap.getSource()));
        }
    }





    /**
     * Generates Service descriptions for the manifest
     */
    private Service getServiceDescriptionV3(ManifestSettings ms, String europeanaId) {
        Service service = new Service(ms.getContentSearchURL(europeanaId), null);
        service.setContext(ManifestGeneratorConstants.SEARCH_CONTEXT_VALUE);
        service.setProfile(ManifestGeneratorConstants.SEARCH_PROFILE_VALUE);
        return service;
    }

    /**
     * Adds date, format, relation, type, language and source values 
     * with the appropriate label
     */
    private void addMetaDataV3(Proxy proxy, List<LabelledValue> ret) {
        addMetaDataV3("date",  proxy.getDate(), ret);
        addMetaDataV3("format", proxy.getFormat(), ret);
        addMetaDataV3("relation", proxy.getRelation(), ret );
        addMetaDataV3("type", proxy.getType(), ret);
        addMetaDataV3("language", proxy.getLanguage(), ret);
        addMetaDataV3("source", proxy.getSource(), ret);
    }


    private void addMetaDataV3(String fieldName, LanguageMap map
                             , List<LabelledValue> dest) {
        //TODO
        // We go over all meta data values and check if it's an url or not.
        // Non-url values are always included as is. If it's an url then we wrap that with an html anchor tag.
        // Additionally we check if the url is also present in object.timespans, agents, concepts or places. If so we
        // add the corresponding preflabels (in all available languages) as well.
        if ( map.isEmpty() ) { return; }

        dest.add(new LabelledValue(
                     new LanguageMap(LanguageMap.DEFAULT_METADATA_KEY, fieldName)
                   , map) );
    }

    /*
     * TODO: improve the representation of URLs 
     * and the fetching of extra labels from contextual classes
     */
    
    private String asHtmlReference(String url) {
        // 1. add html anchor tag to current value
        return "<a href='" + url + "'>" + url + "</a>";
    }

    /**
     * Return the first license description we find in any 'aggregation.edmRights' field. Note that we first try the europeanaAggregation and if
     * that doesn't contain an edmRights, we check the other aggregations
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @param jsonDoc parsed json document
     * @return Rights object containing rights information
     */
    private Text getRights(Aggregation aggr) {
    	String rights = aggr.getRights();
        if ( StringUtils.isEmpty(rights) ) { return null; }

        return new Text(rights, null, "text/html");
    }

    /**
     * Return array with the id of the thumbnail as defined in 'europeanaAggregation.edmPreview'
     * @param jsonDoc parsed json document
     * @return Image object, or null if no edmPreview was found
     */
    private Image getThumbnailImage(Record record) {
    	String preview = record.getPreview();
        return ( StringUtils.isEmpty(preview) ? null : new Image(preview) );
    }

    /**
     * @param europeanaId consisting of dataset ID and record ID separated by a slash
     * @param jsonDoc parsed json document
     * @return {@link Text} containing reference to the landing page of the item on Europeana website
     */
    private void addHomePage(Record record, Manifest manifest) {
        String landingPage = record.getLandingPage();
        if ( landingPage == null ) { return; }

        manifest.getHomepage().add(new Text(landingPage
                                          , new LanguageMap(LanguageMap.DEFAULT_METADATA_KEY, "Europeana")
                                          , "text/html") );
    }

    /**
     * Return attribution text as a String
     * We look for the webResource that corresponds to our edmIsShownBy and return the attribution snippet for that.
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @param isShownBy edmIsShownBy value
     * @param jsonDoc parsed json document
     * @return
     */
    private LabelledValue getAttribution(Aggregation aggr) {
    	WebResource wr = aggr.getIsShownByResource();
        if ( wr != null ) { wr = aggr.getIsShownAtResource(); }

        if ( wr == null ) { return null; }

        String attribution = wr.getTextAttributionSnippet();
        if (StringUtils.isEmpty(attribution)) { return null; }

        return new LabelledValue(new LanguageMap(LanguageMap.DEFAULT_METADATA_KEY, ATTRIBUTION_STRING),
                                 new LanguageMap(LanguageMap.DEFAULT_METADATA_KEY, attribution));
    }

    /**
     * Generates 3 datasets with the appropriate ID and format (one for rdf/xml, one for json and one for json-ld)
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @return array of 3 datasets
     */
    private List<Dataset> getDataSets(Record record) {
        String id = record.getId();
        List<Dataset> result = new ArrayList<>(3);
        result.add(new Dataset(settings.getDatasetId(id, ".json-ld")
                             , AcceptUtils.MEDIA_TYPE_JSONLD
                             , EDM_SCHEMA_URL));
        result.add(new Dataset(settings.getDatasetId(id, ".json")
                             , org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                             , EDM_SCHEMA_URL));
        result.add(new Dataset(settings.getDatasetId(id, ".rdf")
                             , ManifestGeneratorConstants.MEDIA_TYPE_RDF
                             , EDM_SCHEMA_URL));
        return result;
    }

    /**
     * Generates an ordered array of {@link Canvas}es referring to edmIsShownBy and hasView {@link WebResource}s.
     * For more information about the ordering @see {@link WebResourceSorter}
     * @param europeanaId
     * @param isShownBy
     * @param jsonDoc
     * @return array of Canvases
     */
    private void addItems(Record record, Manifest manifest) {
        
    	// generate canvases in a same order as the web resources
        List<WebResource> views = record.getProviderAggregation().getOrderedViews();

        if (views.isEmpty()) {
            LOG.debug("No Canvas generated for europeanaId {}", record.getId());
            return;
        }

        int order = 1;
        for (WebResource view : views) {
            Canvas canvas = getCanvasV3(view, order);
            // for non supported media types we do not create any canvas. Case-4 of media type handling : See-EA-3413
            if (canvas == null) { continue; }

            manifest.getItems().add(canvas);
            order++;
        }

        if ( order > 1 ) {
        	manifest.setStart(manifest.getItems().get(0));
        }
    }


    /**
     * Generates a new canvas, but note that we do not fill the otherContent (Full-Text) here. That's done later.
     */
    private Canvas getCanvasV3(WebResource wr, int order) {

        Canvas c = new Canvas(settings.getCanvasId(wr, order));
        c.setLabel(new LanguageMap(null, "p. " + order));

        //special exception for euscreen which is not mimetype specific
        if ( isEuScreen(wr.getId()) ) {
            return (Canvas)registry.getGenerator(MediaGeneratorType.EUSCREEN, VERSION)
                                   .generate(c, wr);
        }

        // get the configured media type of the mimetype
        String mimeType = wr.getMimeType();
        Optional<MediaType> media = mediaTypes.getMediaType(mimeType);
        if (media.isEmpty()) { return null; }

        wr.setMediaType(media.get());

        return (Canvas) registry.getGenerator(mediaTypes.getGeneratorMethodV3(mimeType), VERSION)
            .generate(c, wr);
    }

    private void addProvider(Manifest manifest) {
        manifest.getProvider().add(new Agent("https://www.europeana.eu/en/about-us",
                new Image(ManifestGeneratorConstants.EUROPEANA_LOGO_URL),
                new Text("https://www.europeana.eu",
                        new LanguageMap(LanguageMap.DEFAULT_METADATA_KEY, "Europeana"), "text/html")));
    }
}