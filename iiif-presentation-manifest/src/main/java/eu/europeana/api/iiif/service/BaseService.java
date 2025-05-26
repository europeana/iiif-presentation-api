package eu.europeana.api.iiif.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.iiif.connection.HttpConnection;

public class BaseService {

    protected final HttpConnection conn;
    protected final ObjectMapper   mapper;

    public BaseService() {
        conn = new HttpConnection();
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }
}
