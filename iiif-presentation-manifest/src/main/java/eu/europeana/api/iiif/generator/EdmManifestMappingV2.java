package eu.europeana.api.iiif.generator;

import eu.europeana.api.commons_sb3.definitions.iiif.AcceptUtils;
import eu.europeana.api.iiif.generator.media.MediaGeneratorRegistry;
import eu.europeana.api.iiif.generator.media.MediaGeneratorType;
import eu.europeana.api.iiif.generator.media.MediaGeneratorVersion;
import eu.europeana.api.iiif.media.MediaType;
import eu.europeana.api.iiif.media.MediaTypeCatalog;
import eu.europeana.api.iiif.model.info.FulltextSummaryCanvas;
import eu.europeana.api.iiif.utils.LanguageMapUtils;
import eu.europeana.api.iiif.v2.model.*;
import eu.europeana.api.iiif.v3.model.LanguageMap;
import eu.europeana.api.record.model.Aggregation;
import eu.europeana.api.record.model.ChangeLog;
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
 * This class contains all the methods for mapping EDM record data to IIIF Manifest data for IIIF v2
 *
 * @author Patrick Ehlert
 * Created on 08-02-2018
 *
 * Updated By Srishti Singh to adjust with the new model class
 *
 */
// ignore sonarqube rule: we return null on purpose in this class
// ignore pmd rule:  we want to make a clear which objects are v2 and which v3
@SuppressWarnings({"squid:S1168", "pmd:UnnecessaryFullyQualifiedName"})
public final class EdmManifestMappingV2 implements ManifestGenerator<Manifest> {

    private static final Logger LOG = LogManager.getLogger(EdmManifestMappingV2.class);
    private static final MediaGeneratorVersion VERSION = MediaGeneratorVersion.V2;

    private ManifestSettings       settings;
    private MediaTypeCatalog       mediaTypes;
    private MediaGeneratorRegistry registry;

    public EdmManifestMappingV2(ManifestSettings settings
                              , MediaTypeCatalog mediaTypes, MediaGeneratorRegistry registry) {
        this.settings   = settings;
        this.mediaTypes = mediaTypes;
        this.registry   = registry;
    }

    /**
     * Generates a IIIF v2 manifest based on the provided (parsed) json document
     * @return IIIF Manifest v2 object
     */
    public Manifest generateManifest(Record record) {

        String europeanaId = record.getId();
        Manifest manifest = new Manifest(settings.getManifestId(europeanaId));
        manifest.getServices().add(getServiceDescription(europeanaId));

        Aggregation aggr = record.getProviderAggregation();
        Proxy proxy = record.getProxy();
        manifest.setLabel(getLabels(proxy));
        manifest.getDescription().addAll(getDescription(proxy));
        addMetaDataV2(proxy, manifest.getMetadata());
        manifest.setThumbnail(getThumbnailImageV2(record));
        manifest.setNavDate(getNavDate(proxy));
        manifest.setAttribution(getAttribution(record));
        manifest.setLicense(aggr.getRights());
        manifest.setLogo(new Image(ManifestGeneratorConstants.EUROPEANA_LOGO_URL));
        addRelated(record, manifest);
        addDataSets(record, manifest);

        addSequences(record, manifest);

        return manifest;
    }

    /**
     * We generate all full text links in one place, so we can raise a timeout if retrieving the necessary
     * data for all full texts is too slow.
     * From EA-2604 on, originalLanguage is available on the FulltextSummaryCanvas and copied to the AnnotationBody if
     * motivation = 'sc:painting'
     */
    public void fillWithFullText(Manifest manifest
                               , Map<String, FulltextSummaryCanvas> summary) {

        if (manifest.getSequences() == null || manifest.getSequences().isEmpty()) {
            LOG.debug("Not checking for fulltext because record doesn't have any sequences");
            return;
        }

        // there is always only 1 sequence
        Sequence sequence = manifest.getSequences().get(0);
        if ( summary == null ) { return; }
            
        // loop over canvases to add full-text link(s) to all
        for (Canvas canvas : sequence.getCanvases()) {
            // we need to generate the same annopageId hash based on imageId
            String apHash = derivePageId(canvas.getStartImageAnnotation().getBody().getID());
            FulltextSummaryCanvas ftCanvas = summary.get(apHash);
            if (ftCanvas == null) {
                // This warning can be logged for empty pages that do not have a fulltext, but if we get a lot
                // then Record API and Fulltext API are not in sync (or the hashing algorithm changed)
                LOG.warn("Possible inconsistent data. No fulltext annopage found for record {} page {}. Generated hash = {}",
                        manifest.getID(), canvas.getID(), apHash);
            } else {
                addFulltextLinkToCanvas(canvas, ftCanvas);
            }
        }

        /*
        Map<String, FulltextSummaryCanvas> summaryCanvasMap;
        if (manifest.getSequences() != null && manifest.getSequences().size() > 0) {
            // there is always only 1 sequence
            Sequence sequence = manifest.getSequences().get(0);
            // Get all the available AnnoPages incl translations from the summary endpoint of Fulltext
            String fullTextSummaryUrl = generateFullTextSummaryUrl(manifest.getEuropeanaId(), fullTextApi);
            summaryCanvasMap = fulltextService.getFulltextSummary(fullTextSummaryUrl);
            if (null != summaryCanvasMap) {
                // loop over canvases to add full-text link(s) to all
                for (Canvas canvas : sequence.getCanvases()) {
                    // we need to generate the same annopageId hash based on imageId
                    String apHash = GenerateUtils.derivePageId(canvas.getStartImageAnnotation().getBody().getID());
                    FulltextSummaryCanvas ftCanvas = summaryCanvasMap.get(apHash);
                    if (ftCanvas == null) {
                        // This warning can be logged for empty pages that do not have a fulltext, but if we get a lot
                        // then Record API and Fulltext API are not in sync (or the hashing algorithm changed)
                        LOG.warn("Possible inconsistent data. No fulltext annopage found for record {} page {}. Generated hash = {}",
                                manifest.getEuropeanaId(), canvas.getPageNr(), apHash);
                    } else {
                        addFulltextLinkToCanvasV2(canvas, ftCanvas);
                    }
                }
            }
        } else {
            LOG.debug("Not checking for fulltext because record doesn't have any sequences");
        }
        */
    }

    private void addFulltextLinkToCanvas(Canvas canvas, FulltextSummaryCanvas summaryCanvas) {
        canvas.getOtherContent().addAll(summaryCanvas.getAnnoPageIDs());
        for (eu.europeana.api.iiif.v2.model.Annotation ann : canvas.getImages()) {
            // original language will be null for translation
            if (StringUtils.equalsAnyIgnoreCase(ann.getMotivation(), "sc:painting") && summaryCanvas.getOriginalLanguage() != null) {
                ann.getBody().setLanguage(summaryCanvas.getOriginalLanguage());
            }
        }
    }

    /**
     * Generates a service description for the manifest
     */
    private Service getServiceDescription(String europeanaId) {
        Service service = new Service(settings.getContentSearchURL(europeanaId));
        service.setContext(ManifestGeneratorConstants.SEARCH_CONTEXT_VALUE);
        service.setProfile(ManifestGeneratorConstants.SEARCH_PROFILE_VALUE);
        return service;
    }

    /**
     * Return first proxy.dctermsIsPartOf that starts with "http://data.theeuropeanlibrary.org/ that we can find
     * @param jsonDoc parsed json document
     * @return
     */

    /**
     * We first check all proxies for a title. If there are no titles, then we check the description fields
     * @param jsonDoc parsed json document
     * @return array of LanguageObject
     */
    private LanguageValue getLabels(Proxy proxy) {
        return LanguageMapUtils.langMapToObject(proxy.getTitleOrDescription());
    }

    /**
     * Returns the values from the proxy.dcDescription fields, but only if they aren't used as a label yet.
     * @param jsonDoc parsed json document
     * @return
     */
    private List<LanguageValue> getDescription(Proxy proxy) {
        return LanguageMapUtils.langMapToObjects(proxy.getDescription());
    }

    /**
     * Reads the dcDate, dcFormat, dcRelation, dcType, dcLanguage and dcSource values from all proxies and puts them in a
     * map with the appropriate label
     * @param jsonDoc parsed json document
     * @return
     */
    private void addMetaDataV2(Proxy proxy, List<LabelledValue> ret) {
        addMetaDataV2("date", proxy.getDate(), ret);
        addMetaDataV2("format", proxy.getFormat(), ret);
        addMetaDataV2("relation", proxy.getRelation(), ret);
        addMetaDataV2("type", proxy.getType(), ret);
        addMetaDataV2("language", proxy.getLanguage(), ret);
        addMetaDataV2("source", proxy.getSource(), ret);
    }

    private void addMetaDataV2(String fieldName, LanguageMap map
                             , List<LabelledValue> dest) {
        if ( map.isEmpty() ) { return; }

        List<LanguageValue> list = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String lang = entry.getKey();
            List<String> values = entry.getValue();
            for ( String value : values ) {
                list.add(new LanguageValue(lang, value));
            }
        }
        dest.add(new LabelledValue(fieldName, list));
    }


    /**
     * Return an with the id of the thumbnail as defined in 'europeanaAggregation.edmPreview'
      * @return Image object, or null if no edmPreview was found
     */
    private Image getThumbnailImageV2(Record record) {
    	String preview = record.getPreview();
        return (StringUtils.isEmpty(preview) ? null : new Image(preview) );
    }

    /**
     * Return attribution text as a String
     * We look for the webResource that corresponds to our edmIsShownBy and return the 'textAttributionSnippet' for that.
     * @param record object having record details
     * @return attribution string
     */
    private String getAttribution(Record  record) {
        if(record.isArchived()){
            ChangeLog c = record.getChangeLogByType("Delete");
            if(c != null && c.getContext() != null) {
                return settings.getDePubMessages().get(c.getContext());
            }
        }
        else {
            Aggregation aggr = record.getProviderAggregation();
            WebResource wr = aggr.getIsShownByResource();
            if (wr != null) {
                wr = aggr.getIsShownAtResource();
            }
            return (wr == null ? null : wr.getTextAttributionSnippet());
        }
        return  null;
    }

    private void addRelated(Record record, Manifest manifest) {
        String landingPage = record.getLandingPage();
        if ( landingPage == null ) { return; }

        manifest.getRelated().add(
            new ResourceReference(landingPage, "Europeana Website"
                               , "text/html") );
    }


    /**
     * Generates 3 datasets with the appropriate ID and format (one for rdf/xml, one for json and one for json-ld)
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @return array of 3 datasets
     */
    private void addDataSets(Record record, Manifest manifest) {
        String id = record.getId();
        manifest.getSeeAlso().add(new Dataset(settings.getDatasetId(id, ".json-ld")
                                , AcceptUtils.MEDIA_TYPE_JSONLD
                                , EDM_SCHEMA_URL));
        manifest.getSeeAlso().add(new Dataset(settings.getDatasetId(id, ".json")
                                , org.springframework.http.MediaType.APPLICATION_JSON_VALUE
                                , EDM_SCHEMA_URL));
        manifest.getSeeAlso().add(new Dataset(settings.getDatasetId(id, ".rdf")
                                , ManifestGeneratorConstants.MEDIA_TYPE_RDF
                                , EDM_SCHEMA_URL));
    }

    /**
     * @param europeanaId consisting of dataset ID and record ID separated by a slash (string should have a leading slash and not trailing slash)
     * @param isShownBy
     * @param jsonDoc parsed json document
     * @return
     */
    private void addSequences(Record record, Manifest manifest) {

    	List<WebResource> views = record.getProviderAggregation().getOrderedViews();
    	if ( views.isEmpty() ) { 
            LOG.debug("No Canvas generated for europeanaId {}", record.getId());
    		return; 
    	}

        int order = 1;
        List<Canvas> canvases = new ArrayList<>(views.size());
        for (WebResource webResource : views) {
            Canvas canvas = getCanvas(webResource, order);
            // for non supported media types we do not create any canvas. Case-4 of media type handling : See-EA-3413
            if (canvas == null) { continue; }

            canvases.add(canvas);
            order++;
        }

        // if there are canvas generated add the sequence
        if (!canvases.isEmpty()) {
            // there should be only 1 sequence, so sequence number is always 1
            Sequence sequence = new Sequence();
            sequence.setStartCanvas(canvases.get(0).getID());
            sequence.setCanvases(canvases);
            sequence.setLabel(new LanguageValue("Current Page Order"));
        	manifest.getSequences().add(sequence);
        }
    }


    /**
     * Generates a new canvas, but note that we do not fill the otherContent (Full-Text) here. That is done later
     */
    private Canvas getCanvas(WebResource wr, int order) {

        Canvas c = new Canvas(settings.getCanvasId(wr, order));
        c.setLabel(new LanguageValue("p. " + order));

        if (isEuScreen(wr.getId())) {
            return (Canvas)registry.getGenerator(MediaGeneratorType.EUSCREEN,VERSION)
                                   .generate(c, wr);
        }

        // get the configured media type of the mimetype
        String mimeType = wr.getMimeType();
        Optional<MediaType> media = mediaTypes.getMediaType(mimeType);
        if (media.isEmpty()) { return null; }

        wr.setMediaType(media.get());

        return (Canvas)registry.getGenerator(mediaTypes.getGeneratorMethodV2(mimeType), VERSION)
                               .generate(c, wr);
    }
}