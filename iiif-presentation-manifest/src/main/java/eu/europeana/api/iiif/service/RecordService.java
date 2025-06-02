package eu.europeana.api.iiif.service;

import eu.europeana.api.commons.http.HttpResponseHandler;
import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.error.EuropeanaApiErrorResponse;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.exceptions.InvalidArgumentException;
import eu.europeana.api.iiif.exceptions.ResourceNotChangedException;
import eu.europeana.api.iiif.exceptions.RecordNotFoundException;
import eu.europeana.api.iiif.exceptions.RecordRetrievalException;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.spi.json.JsonProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static com.jayway.jsonpath.Configuration.defaultConfiguration;

@Service
public class RecordService extends BaseService {

    private static final Logger LOG = LogManager.getLogger(RecordService.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Return record information in Json format using the Record API base URL defined in the iiif.properties
     *
     * @param recordId Europeana record id in the form of "/datasetid/recordid" (so with leading slash and without trailing slash)
     * @param auth     the authentication method to be used
     * @return record information in json format
     * @throws EuropeanaApiException (IllegalArgumentException if a parameter has an illegal format,
     *                               InvalidApiKeyException if the provide key is not valid,
     *                               RecordNotFoundException if there was a 404,
     *                               RecordRetrieveException on all other problems)
     */
    public Object getRecordJson(String recordApiUrl, String recordId
                              , AuthenticationHandler auth, HttpHeaders reqHeaders
                              , ResourceCaching caching) throws EuropeanaApiException {
        try {
            HttpResponseHandler rsp = conn.get(buildRecordApiUrl(recordApiUrl, recordId), null, getHeaderMap(reqHeaders), auth);
            int responseCode = rsp.getStatus();
            String responseBody = rsp.getResponse();
            if (responseCode == HttpStatus.SC_OK) {
                caching.getHeaders(getHeaders(rsp.getCachingHeaders()));
                return parseResponse(responseBody);
            }
            else if (responseCode == HttpStatus.SC_NOT_MODIFIED) {
                throw new ResourceNotChangedException(recordId);
            }
            else {
                EuropeanaApiErrorResponse errorResponse = mapper.readValue(responseBody, EuropeanaApiErrorResponse.class);
                if (responseCode == HttpStatus.SC_UNAUTHORIZED || responseCode == HttpStatus.SC_FORBIDDEN) {
                    throw new RecordRetrievalException(errorResponse, rsp.getStatus());
                }
                if (responseCode == HttpStatus.SC_NOT_FOUND) {
                    throw new RecordNotFoundException("Record with id '" + recordId + "' not found");
                }

                LOG.error("Error retrieving record {}, reason {}", recordId, errorResponse.getMessage());
                throw new RecordRetrievalException("Error retrieving record: " + errorResponse.getMessage(),
                        errorResponse.getError(), errorResponse.getCode(), errorResponse.getStatus());
            }
        }
        catch (InvalidArgumentException | IOException e) {
            throw new RecordRetrievalException(" Error retrieving the record : " + e.getMessage());
        }
    }

    /**
     * Build the record api retrieval url with record id
     * @param recordApiUrl record api base url
     * @param recordId id
     * @return URL
     * @throws InvalidArgumentException
     */
    private String buildRecordApiUrl(String recordApiUrl, String recordId) 
            throws InvalidArgumentException {
        if (StringUtils.isEmpty(recordApiUrl)) {
            throw new InvalidArgumentException("Record api url must NOT be empty!!");
        }
        if (StringUtils.isEmpty(recordId)) {
            throw new InvalidArgumentException("Record Id must be present!!");
        }
        try {
            return new URIBuilder(recordApiUrl)
                    .appendPath(recordId + ".json")
                    .build().toString();
        } catch (URISyntaxException e) {
            throw new InvalidArgumentException("Error building the record api url - " + e.getMessage(), e);
        }
    }

    private Object parseResponse(String responseBody) throws RecordRetrievalException {
        try (InputStream in = new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8))) {
            JsonProvider jsonProvider = defaultConfiguration().jsonProvider();
            return jsonProvider.parse(in, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RecordRetrievalException(" Error parsing the record response: " + e.getMessage());
        }
    }
}
