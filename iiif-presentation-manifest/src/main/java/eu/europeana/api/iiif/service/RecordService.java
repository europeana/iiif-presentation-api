package eu.europeana.api.iiif.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
import eu.europeana.api.commons_sb3.error.EuropeanaApiErrorResponse;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.http.HttpResponseHandler;
import eu.europeana.api.iiif.exceptions.InvalidArgumentException;
import eu.europeana.api.iiif.exceptions.ResourceNotChangedException;
import eu.europeana.api.record.model.Record;
import eu.europeana.api.record.model.RecordResponse;
import eu.europeana.api.record.serialization.BeanLifecycleModifier;
import eu.europeana.api.iiif.exceptions.RecordNotFoundException;
import eu.europeana.api.iiif.exceptions.RecordRetrievalException;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jayway.jsonpath.spi.json.JsonProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static com.jayway.jsonpath.Configuration.defaultConfiguration;
import static eu.europeana.api.commons_sb3.error.EuropeanaErrorConstants.*;
import static eu.europeana.api.commons_sb3.error.EuropeanaErrorConstants.code;

@Service
public class RecordService extends BaseService {

    private static final Logger LOG = LogManager.getLogger(RecordService.class);

    private ObjectMapper recordMapper;

    public RecordService() {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanLifecycleModifier());
        recordMapper = new ObjectMapper();
        recordMapper.registerModule(module);
    }

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
    public RecordResponse getRecordJson(String recordApiUrl, String recordId
                              , AuthenticationHandler auth, HttpHeaders reqHeaders
                              , ResourceCaching caching) throws EuropeanaApiException {
        try {
            HttpResponseHandler rsp = recordClient.get(buildRecordApiUrl(recordApiUrl, recordId)
                                                     , getHeaderMap(reqHeaders)
                                                     , auth);
            int responseCode = rsp.getStatus();
            if (responseCode == HttpStatus.SC_OK) {
                caching.getHeaders(getHeaders(rsp.getCachingHeaders()));
                return parseResponse(rsp);
            }

            if (responseCode == HttpStatus.SC_NOT_MODIFIED) {
                throw new ResourceNotChangedException(recordId);
            }

            EuropeanaApiErrorResponse errorResponse = constructErrorResponse(responseCode, rsp.getResponse());
            //TODO replace it once we start using record api v3
            // recordMapper.readValue(responseBody, EuropeanaApiErrorResponse.class);

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

    private RecordResponse parseResponse(HttpResponseHandler rsp) throws RecordRetrievalException {
        try {
            return recordMapper.readValue(rsp.getResponse()
                                        , RecordResponse.class);
        } catch (IOException e) {
            throw new RecordRetrievalException(" Error parsing the record response: " + e.getMessage());
        }
    }

    /**
     * TODO Should be removed once we start using record api v3
     * as Current SR API uses different model of error response
     * @param json
     * @return
     */
    private EuropeanaApiErrorResponse constructErrorResponse(int responseCode, String json) throws RecordRetrievalException {
        try {
            JsonNode node = recordMapper.readTree(json);
            return new EuropeanaApiErrorResponse(
                    responseCode,
                    node.has(error) ? node.get(error).asText() : "",
                    node.has(message) ? node.get(message).asText() : "Error retrieving record",
                    null,
                    null,
                    node.has(code) ? node.get(code).asText() : "");
        } catch (JsonProcessingException e) {
            throw new RecordRetrievalException(" Error parsing the record response: " + e.getMessage());
        }
    }

}
