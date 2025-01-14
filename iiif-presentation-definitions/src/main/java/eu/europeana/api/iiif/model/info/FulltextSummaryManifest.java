package eu.europeana.api.iiif.model.info;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import static eu.europeana.api.commons_sb3.definitions.iiif.IIIFDefinitions.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by luthien on 07/04/2021.
 * Srishti - TODO create definitions module in Fulltext and use the same class
 */
@JsonPropertyOrder({"@context", "textGranularity", "items"})
public class FulltextSummaryManifest implements Serializable {
    private static final long serialVersionUID = -8052995235828716772L;

    @JsonProperty("@context")
    private final String[] context = new String[]{MEDIA_TYPE_W3ORG_JSONLD, TEXT_GRANULARITY_CONTEXT, MEDIA_TYPE_IIIF_V3};

    @JsonIgnore
    private String              dataSetId;

    @JsonIgnore
    private String              localId;

    @JsonProperty("items")
    private List<FulltextSummaryCanvas> canvases;

    FulltextSummaryManifest() {
        // for jackson deserialisation
    }

    /**
     * This is a container object to group "fake" SummaryCanvas objects AnnoPages for a given Fulltext record / object
     *
     * @param dataSetId String containing the dataset of this Fulltext SummaryManifest
     * @param localId   String containing the localId of this Fulltext SummaryManifest
     */
    public FulltextSummaryManifest(String dataSetId, String localId){
        this.dataSetId = dataSetId;
        this.localId = localId;
        canvases = new ArrayList<>();
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    /**
     * Adds a *fake* SummaryCanvas containing an AnnoPage (AnnotationLangPages)
     * @param summaryCanvas SummaryCanvas object to be added to the canvases List
     */
    public void addCanvas(FulltextSummaryCanvas summaryCanvas){
        canvases.add(summaryCanvas);
    }

    public List<FulltextSummaryCanvas> getCanvases() {
        return new ArrayList<>(canvases);
    }

    public void setCanvases(List<FulltextSummaryCanvas> canvases) {
        this.canvases = new ArrayList<>(canvases);
    }

    public String[] getContext() {
        return context;
    }
}