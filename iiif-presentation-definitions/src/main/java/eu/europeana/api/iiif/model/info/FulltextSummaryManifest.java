package eu.europeana.api.iiif.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.europeana.api.commons_sb3.definitions.iiif.IIIFDefinitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by luthien on 07/04/2021.
 */
@JsonPropertyOrder({"@context", "textGranularity", "items"})
public class FulltextSummaryManifest implements Serializable {
    private static final long serialVersionUID = -8052995235828716772L;

    @JsonProperty("@context")
    private final String[] context = new String[]{IIIFDefinitions.MEDIA_TYPE_W3ORG_JSONLD, IIIFDefinitions.MEDIA_TYPE_IIIF_V3};
    // switch when available in this version
//    private final String[] context = new String[]{MEDIA_TYPE_W3ORG_JSONLD, IIIFDefinitions.TEXT_GRANULARITY_CONTEXT, MEDIA_TYPE_IIIF_V3};

    @JsonProperty("items")
    private List<FulltextSummaryCanvas> canvases;

    public FulltextSummaryManifest() {}

    /**
     * This is a container object to group "fake" FulltextSummaryCanvas objects containing original and translated AnnoPages
     * for a given Fulltext record / object
     *
     * @param dataSetId String containing the dataset of this Fulltext FulltextSummaryManifest
     * @param localId   String containing the localId of this Fulltext FulltextSummaryManifest
     */
    public FulltextSummaryManifest(String dataSetId, String localId){
        canvases = new ArrayList<>();
    }

    /**
     * Adds a *fake* FulltextSummaryCanvas containing original and translated versions of an AnnoPage (AnnotationLangPages)
     * @param fulltextSummaryCanvas FulltextSummaryCanvas object to be added to the canvases List
     */
    public void addCanvas(FulltextSummaryCanvas fulltextSummaryCanvas){
        canvases.add(fulltextSummaryCanvas);
    }

    @JsonValue
    public List<FulltextSummaryCanvas> getCanvases() {
        return new ArrayList<>(canvases);
    }

}
