package eu.europeana.api.iiif.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.commons.http.HttpConnection;
import eu.europeana.api.commons_sb3.definitions.caching.CachingHeaders;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.Header;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, String> getHeaderMap(HttpHeaders headers) {
        Map<String, String> headerMap = new HashMap<>();
        for (String key : headers.keySet() ) {
            headerMap.put(key, headers.getFirst(key));
        }
        return headerMap;
    }

    public HttpHeaders getHeaders(List<Header> cachingHeaders) {
        HttpHeaders headers = new HttpHeaders();
        for (Header h: cachingHeaders) {
            if (StringUtils.equals(h.getName(), CachingHeaders.ETAG)) {
                headers.setETag(h.getValue());
            }
            if (StringUtils.equals(h.getName(), CachingHeaders.LAST_MODIFIED)) {
                headers.set(CachingHeaders.LAST_MODIFIED, h.getValue());

            } if (StringUtils.equals(h.getName(), CachingHeaders.CACHE_CONTROL)) {
                headers.setCacheControl(h.getValue());

            }
        }
        return headers;
    }

}
