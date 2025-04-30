package eu.europeana.api.iiif.connection;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.http.HttpHeaders;

import eu.europeana.api.commons.auth.AuthenticationHandler;

import java.io.IOException;

/**
 * HttpConnection class with HTTP Client 5
 * for now only create a normal http client
 * Caching http client will be considered in future
 * @author srishti singh
 */
public class HttpConnection {

    private final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    public HttpConnection() {
    }


    public CloseableHttpResponse get(String url, String acceptHeaderValue 
                                   , HttpHeaders headers
                                   , AuthenticationHandler auth) throws IOException {
        HttpGet get = new HttpGet(url);
        addHeaders(get, headers);
        this.addHeaders(get, "Accept", acceptHeaderValue);
        auth.setAuthorization(get);
        return this.executeHttpClient(get);
    }

    public CloseableHttpResponse post(String url, String requestBody, String contentType
                                    , AuthenticationHandler auth) throws IOException {
        HttpPost post = new HttpPost(url);
        this.addHeaders(post, "Content-Type", contentType);
        auth.setAuthorization(post);
        post.setEntity(new StringEntity(requestBody));
        return this.executeHttpClient(post);
    }

    public CloseableHttpResponse put(String url, String jsonParamValue
                                   , AuthenticationHandler auth) throws IOException {
        HttpPut put = new HttpPut(url);
        auth.setAuthorization(put);
        put.setEntity(new StringEntity(jsonParamValue));
        return this.executeHttpClient(put);
    }

    public CloseableHttpResponse deleteURL(String url, AuthenticationHandler auth) throws IOException {
        HttpDelete delete = new HttpDelete(url);
        auth.setAuthorization(delete);
        return this.executeHttpClient(delete);
    }

    private <T extends HttpUriRequestBase> CloseableHttpResponse executeHttpClient(T url) throws IOException {
        return this.httpClient.execute(url);
    }

    private <T extends HttpUriRequestBase> void addHeaders(T url, HttpHeaders headers) {
        if ( headers == null ) { return; }
        for ( String key : headers.keySet() ) {
            addHeaders(url, key, headers.getFirst(key));
        }
    }

    private <T extends HttpUriRequestBase> void addHeaders(T url, String headerName, String headerValue) {
        if (StringUtils.isNotBlank(headerValue)) {
            url.setHeader(headerName, headerValue);
        }
    }
}
