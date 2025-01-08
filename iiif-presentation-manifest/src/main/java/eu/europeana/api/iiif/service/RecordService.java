package eu.europeana.api.iiif.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.connection.HttpConnection;
import eu.europeana.api.iiif.exceptions.InvalidApiKeyException;
import eu.europeana.api.iiif.exceptions.InvalidArgumentException;
import eu.europeana.api.iiif.exceptions.RecordNotFoundException;
import eu.europeana.api.iiif.exceptions.RecordRetrievalException;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class RecordService extends BaseService {

    private static final Logger LOG = LogManager.getLogger(RecordService.class);

    /**
     * Return record information in Json format using the Record API base URL defined in the iiif.properties
     *
     * @param recordId Europeana record id in the form of "/datasetid/recordid" (so with leading slash and without trailing slash)
     * @param wskey    api key to send to record API
     * @return record information in json format
     * @throws EuropeanaApiException (IllegalArgumentException if a parameter has an illegal format,
     *                               InvalidApiKeyException if the provide key is not valid,
     *                               RecordNotFoundException if there was a 404,
     *                               RecordRetrieveException on all other problems)
     */
    public String getRecordJson(String recordApiUrl, String recordId, String wskey) throws EuropeanaApiException {
        try {
            URI uri = buildRecordApiUrl(recordApiUrl, recordId, wskey);
            CloseableHttpResponse response = httpConnection.get(uri.toString(), null, null);
            int responseCode = response.getCode();
            if (responseCode == HttpStatus.SC_OK) {
                return mapper.readValue(EntityUtils.toString(response.getEntity()), String.class);
            }
            if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new InvalidApiKeyException("Apikey is not valid !!");
            } else if (responseCode == HttpStatus.SC_NOT_FOUND) {
                throw new RecordNotFoundException("Record with id '" + recordId + "' not found");
            }
            LOG.error("Error retrieving record {}, reason {}", recordId, response.getReasonPhrase());
            throw new RecordRetrievalException("Error retrieving record: " + response.getReasonPhrase());

        } catch (InvalidArgumentException | IOException | ParseException e) {
            throw new RecordRetrievalException(" Error retrieving the record : " + e.getMessage());
        }
    }

    /**
     * Build the record api retrieval url with record id
     * @param recordApiUrl record api base url
     * @param recordId id
     * @param wskey key for authorisation
     * @return URL
     * @throws InvalidArgumentException
     */
    private URI buildRecordApiUrl(String recordApiUrl, String recordId, String wskey) throws InvalidArgumentException {
        if (StringUtils.isEmpty(recordApiUrl)) {
            throw new InvalidArgumentException("Record api url must NOT be empty!!");
        }
        if (StringUtils.isEmpty(recordId)) {
            throw new InvalidArgumentException("Record Id must be present!!");
        }
        try {
            return new URIBuilder(recordApiUrl).
                    appendPath(recordId)
                    .appendPath(".json")
                    .addParameter("wskey", wskey)
                    .build();
        } catch (URISyntaxException e) {
            throw new InvalidArgumentException("Error building the record api url - " + e.getMessage(), e);
        }
    }


}
