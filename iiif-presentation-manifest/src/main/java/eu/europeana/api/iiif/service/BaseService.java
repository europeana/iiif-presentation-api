package eu.europeana.api.iiif.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.commons.http.HttpConnection;
import eu.europeana.api.commons_sb3.definitions.caching.CachingHeaders;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseService {

    protected final HttpConnection recordClient;
    protected final HttpConnection fulltextClient;

    protected final ObjectMapper   mapper;

    private static final int MAX_TOTAL_CONNECTIONS    = 200;
    private static final int DEFAULT_MAX_PER_ROUTE    = 100;
    private static final int MAX_CACHED_ENTRIES       = 1000;
    private static final int MAX_CACHED_OBJECT_SIZE   = 65536;

    private static final int RECORD_CONNECT_TIMEOUT = 10_000;
    protected static final int RECORD_SOCKET_TIMEOUT  = 30_000;
    private static final int FULLTEXT_CONNECT_TIMEOUT = 8_000;
    protected static final int FULLTEXT_SOCKET_TIMEOUT  = 20_000;

    public BaseService() {
        recordClient = new HttpConnection(createConnectionPool(true));
        fulltextClient = new HttpConnection(createConnectionPool(false));

        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }

    /**
     * Create a connection pool manager for the HttpConnections
     *
     * Optionally choose a connection pool re-use policy:
     *     LIFO to re-use as few connections as possible making it possible for connections to become idle and expire;
     *     FIFO to re-use all connections equally preventing them from becoming idle and expiring.
     *
     * @param recordApi true if the pool is created for record api
     * @return PoolingHttpClientConnectionManager
     */
    public PoolingHttpClientConnectionManager createConnectionPool(boolean recordApi) {
       return PoolingHttpClientConnectionManagerBuilder.create()
               .setMaxConnPerRoute(DEFAULT_MAX_PER_ROUTE)
               .setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
               .setDefaultConnectionConfig(ConnectionConfig.custom()
                       .setConnectTimeout(recordApi ? Timeout.ofMilliseconds(RECORD_CONNECT_TIMEOUT) : Timeout.ofMilliseconds(FULLTEXT_CONNECT_TIMEOUT))
                       .setSocketTimeout(recordApi ? Timeout.ofMilliseconds(RECORD_SOCKET_TIMEOUT): Timeout.ofMilliseconds(FULLTEXT_SOCKET_TIMEOUT))
                       .build())
               .setConnPoolPolicy(PoolReusePolicy.LIFO)
               .build();
    }


    public Map<String, String> getHeaderMap(HttpHeaders headers) {
        if (headers == null)  return null ;
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
