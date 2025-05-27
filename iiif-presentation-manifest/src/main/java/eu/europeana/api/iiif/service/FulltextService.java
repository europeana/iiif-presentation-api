package eu.europeana.api.iiif.service;

import eu.europeana.api.commons_sb3.definitions.caching.CachingHeaders;
import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.exceptions.FullTextCheckException;
import eu.europeana.api.iiif.exceptions.ResourceNotChangedException;
import eu.europeana.api.iiif.generator.ManifestSettings;
import eu.europeana.api.iiif.model.ManifestDefinitions;
import eu.europeana.api.iiif.model.info.FulltextSummaryCanvas;
import eu.europeana.api.iiif.model.info.FulltextSummaryManifest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class FulltextService extends BaseService {

    private static final Logger LOG = LogManager.getLogger(FulltextService.class);

    private final ManifestSettings settings;

    public FulltextService(ManifestSettings settings) {
        this.settings = settings;
    }

    /**
     * Fetch the Fulltext Summary response
     * ex - {fulltext-baseurl}/presentation/{dsid}/{lcID}/annopage
     * @param fulltextUrl
     * @return
     * @throws EuropeanaApiException
     */
    public FulltextSummaryManifest getFullTextSummary(
            String url, AuthenticationHandler auth
          , HttpHeaders reqHeaders, ResourceCaching caching) throws EuropeanaApiException{

        
        try ( CloseableHttpResponse rsp = conn.get(url, null, reqHeaders, auth) ) {
            int code = rsp.getCode();
            if (code == HttpStatus.SC_OK) {
                caching.getHeaders(getHeaders(rsp));
                HttpEntity entity = rsp.getEntity();
                String jsonV = EntityUtils.toString(entity);
                return mapper.readValue(jsonV, FulltextSummaryManifest.class);
            }
            if (code == HttpStatus.SC_NOT_MODIFIED) {
                throw new ResourceNotChangedException(url);
            }
            if (code == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            LOG.error("Error retrieving fulltext summary {}, reason {}", url, rsp.getReasonPhrase());
            throw new FullTextCheckException("Error retrieving fulltext summary - " + rsp.getReasonPhrase());

        } catch (IOException | ParseException e) {
            throw new FullTextCheckException("Error retrieving fulltext summary - " + e.getMessage(), e);
        }
    }


    public Map<String, FulltextSummaryCanvas> getFulltextSummary(
            String fulltextUrl, AuthenticationHandler auth
          , HttpHeaders reqHeaders, ResourceCaching caching) throws EuropeanaApiException {
        return createCanvasMap(getFullTextSummary(fulltextUrl, auth, reqHeaders, caching));
    }

    public Map<String, FulltextSummaryCanvas> getFulltextSummary(
            String recordId, String fullTextApi, AuthenticationHandler auth
          , HttpHeaders reqHeaders, ResourceCaching caching) 
                    throws EuropeanaApiException {
        String fullTextSummaryUrl = generateFullTextSummaryUrl(recordId, fullTextApi);
        return getFulltextSummary(fullTextSummaryUrl, auth, reqHeaders, caching);
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
    private Map<String, FulltextSummaryCanvas> createCanvasMap(
            FulltextSummaryManifest summary) {

        if ( summary == null ) { return null; }
        LinkedHashMap<String, FulltextSummaryCanvas> map = new LinkedHashMap<>();

        for (FulltextSummaryCanvas ftSummaryCanvasToAdd : summary.getCanvases()) {
            FulltextSummaryCanvas canvas = map.get(ftSummaryCanvasToAdd.getPageNumber());
            if (canvas != null) {
                canvas.addFTSummaryAnnoPage(ftSummaryCanvasToAdd.getFTSummaryAnnoPages().get(0));
                // in this case there will be no original language (translations)
                if (canvas.getOriginalLanguage() != null) {
                    canvas.setOriginalLanguage(null);
                }
            } else {
                map.put(ftSummaryCanvasToAdd.getPageNumber(), ftSummaryCanvasToAdd);
            }
        }
        return map;

    }

    private HttpHeaders getHeaders(CloseableHttpResponse rsp) {
        HttpHeaders headers = new HttpHeaders();
        Header header = null;

        header = rsp.getFirstHeader(CachingHeaders.ETAG);
        if ( header != null ) { headers.setETag(header.getValue()); }

        header = rsp.getFirstHeader(CachingHeaders.CACHE_CONTROL);
        if ( header != null ) { headers.setCacheControl(header.getValue()); }

        header = rsp.getFirstHeader(CachingHeaders.LAST_MODIFIED);
        if ( header != null ) { headers.set(CachingHeaders.LAST_MODIFIED, header.getValue()); }
        return headers;
    }

    /**
     * Generates a url to a full text resource
     *
     * @param fullTextApiUrl optional, if not specified then the default Full-Text API specified in .properties is used
     * @param europeanaId    identifier to include in the path
     */
    private String generateFullTextSummaryUrl(String europeanaId
                                            , String fullTextApiUrl) {
        if ( fullTextApiUrl == null ) {
            fullTextApiUrl = settings.getFullTextApiBaseUrl();
        }
        return fullTextApiUrl + ManifestDefinitions.getFulltextSummaryPath(europeanaId);
    }
}
