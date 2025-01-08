package eu.europeana.api.iiif.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.api.iiif.model.ManifestDefinitions;
import eu.europeana.api.iiif.v3.io.JsonConstants;
import eu.europeana.api.iiif.v3.model.IIIFv3Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luthien on 15/04/2021.
 * Srishti - TODO create definitions module in Fulltext and use the same class
 */
public class FulltextSummaryCanvas extends IIIFv3Resource {

    private static final long serialVersionUID = 7066577659030844718L;

    private static final String PAGE_ID_START = "/canvas/";

    private String originalLanguage;

    @JsonProperty("annotations")
    private List<FulltextSummaryAnnoPage> ftSummaryAnnoPages;

    public FulltextSummaryCanvas(){}

    @JsonProperty(JsonConstants.type)
    public String getType() {
        return ManifestDefinitions.INFO_CANVAS_TYPE;
    }

    /**
     * This is not a true IIIF FulltextSummaryCanvas object but merely a container object to group original and
     * translated Annopages
     *
     * @param id String containing identifying URL of the FulltextSummaryCanvas
     */
    public FulltextSummaryCanvas(String id) {
        super(id);
        ftSummaryAnnoPages = new ArrayList<>();
    }

    /**
     * Adds an annotation - actually: an FulltextSummaryAnnoPage (AnnoPage for a specific language) to the FulltextSummaryCanvas
     * @param ftSummaryAnnoPage FulltextSummaryAnnoPage object to be added to the annotations List
     */
    public void addFTSummaryAnnoPage(FulltextSummaryAnnoPage ftSummaryAnnoPage){
        ftSummaryAnnoPages.add(ftSummaryAnnoPage);
    }

    public List<FulltextSummaryAnnoPage> getFTSummaryAnnoPages() {
        return new ArrayList<>(ftSummaryAnnoPages);
    }

    /**
     * Return the page id of this fulltext annopage
     * @return fulltext id or null if it cannot be found
     */
    public String getPageNumber(){
        int start = getID().indexOf(PAGE_ID_START);
        if (start == -1) {
            return null;
        }
        String result = getID().substring(start + PAGE_ID_START.length());
        int end = result.indexOf('?');
        if (end != -1) {
            result = result.substring(0, end);
        }
        return result;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    /*
     * Used by Jackson deserializing data from Fulltext API
     */
    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public List<String> getAnnoPageIDs(){
        List<String> annoPageIDs  = new ArrayList<>();
        for (FulltextSummaryAnnoPage sap : ftSummaryAnnoPages) {
            annoPageIDs.add(sap.getID());
        }
        return annoPageIDs;
    }

    public Map<String, String> getAnnoPageIDLang(){
        Map<String, String> annoPageLangs  = new HashMap<>();
        for (FulltextSummaryAnnoPage sap : ftSummaryAnnoPages) {
            annoPageLangs.put(sap.getID(), sap.getLanguage());
        }
        return annoPageLangs;
    }
}
