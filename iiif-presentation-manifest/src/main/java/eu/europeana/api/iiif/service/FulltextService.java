package eu.europeana.api.iiif.service;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;

import eu.europeana.api.iiif.exceptions.FullTextCheckException;
import eu.europeana.api.iiif.model.info.FulltextSummaryCanvas;
import eu.europeana.api.iiif.model.info.FulltextSummaryManifest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class FulltextService extends BaseService {

    private static final Logger LOG = LogManager.getLogger(FulltextService.class);

    /**
     * Fetch the Fulltext Summary response
     * ex - {fulltext-baseurl}/presentation/{dsid}/{lcID}/annopage
     * @param fulltextUrl
     * @return
     * @throws EuropeanaApiException
     */
    public FulltextSummaryManifest getFullTextSummary(String fulltextUrl) throws EuropeanaApiException{
        try {
            CloseableHttpResponse response = httpConnection.get(fulltextUrl, null, null);
            if (response.getCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                String jsonV = EntityUtils.toString(entity);
                return mapper.readValue(jsonV, FulltextSummaryManifest.class);
            }
            LOG.error("Error retrieving fulltext summary {}, reason {}", fulltextUrl, response.getReasonPhrase());
            throw new FullTextCheckException("Error retrieving fulltext summary - " + response.getReasonPhrase());

        } catch (IOException | ParseException e) {
            throw new FullTextCheckException("Error retrieving fulltext summary - " + e.getMessage(), e);
        }
    }


    public Map<String, FulltextSummaryCanvas> getFulltextSummary(String fulltextUrl) throws EuropeanaApiException {
        return createCanvasMap(getFullTextSummary(fulltextUrl));
    }


    /**
     * Creates FulltextSummaryCanvas from FulltextSummaryManifest
     * @param summary Fulltext AnnoPage info response object
     *
     * For translations with same dsId, LcID and pgID, multiple annoations Page will exists
     * Hence add the Annotations only.
     * Also for now, fulltextSummaryCanvas.getFTSummaryAnnoPages() will have only one annotation
     * @see <a href="https://github.com/europeana/fulltext-api/blob/7bfbb90981a760ff4d5231a0106307e6405eec51/api/src/main/java/eu/europeana/fulltext/api/service/FTService.java#L227">
     * #collectionAnnoPageInfo</a>
     *
     * @return
     */
    // TODO we are executing ftSummaryCanvasToAdd.getPageNumber() twice
    private Map<String, FulltextSummaryCanvas> createCanvasMap(FulltextSummaryManifest summary) {
        LinkedHashMap<String, FulltextSummaryCanvas> summaryCanvasMap = new LinkedHashMap<>();

        for (FulltextSummaryCanvas ftSummaryCanvasToAdd : summary.getCanvases()) {
            FulltextSummaryCanvas canvas = summaryCanvasMap.get(ftSummaryCanvasToAdd.getPageNumber());
            if (canvas != null) {
                canvas.addFTSummaryAnnoPage(ftSummaryCanvasToAdd.getFTSummaryAnnoPages().get(0));
                // in this case there will be no original language (translations)
                if (canvas.getOriginalLanguage() != null) {
                    canvas.setOriginalLanguage(null);
                }
            } else {
                summaryCanvasMap.put(ftSummaryCanvasToAdd.getPageNumber(), ftSummaryCanvasToAdd);
            }
        }
        return summaryCanvasMap;

    }
}
