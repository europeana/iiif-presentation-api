package eu.europeana.api.iiif.connection;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;

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


    public CloseableHttpResponse get(String url, String acceptHeaderValue, String authorizationHeaderValue) throws IOException {
        HttpGet get = new HttpGet(url);
        this.addHeaders(get, "Accept", acceptHeaderValue);
        this.addHeaders(get, "Authorization", authorizationHeaderValue);
        return this.executeHttpClient(get);
    }

    public CloseableHttpResponse post(String url, String requestBody, String contentType, String authorizationHeaderValue) throws IOException {
        HttpPost post = new HttpPost(url);
        this.addHeaders(post, "Content-Type", contentType);
        this.addHeaders(post, "Authorization", authorizationHeaderValue);
        post.setEntity(new StringEntity(requestBody));
        return this.executeHttpClient(post);
    }

    public CloseableHttpResponse put(String url, String jsonParamValue, String authorizationHeaderValue) throws IOException {
        HttpPut put = new HttpPut(url);
        this.addHeaders(put, "Authorization", authorizationHeaderValue);
        put.setEntity(new StringEntity(jsonParamValue));
        return this.executeHttpClient(put);
    }

    public CloseableHttpResponse deleteURL(String url, String authorizationtHeaderValue) throws IOException {
        HttpDelete delete = new HttpDelete(url);
        this.addHeaders(delete, "Authorization", authorizationtHeaderValue);
        return this.executeHttpClient(delete);
    }

    private <T extends HttpUriRequestBase> CloseableHttpResponse executeHttpClient(T url) throws IOException {
        return this.httpClient.execute(url);
    }

    private <T extends HttpUriRequestBase> void addHeaders(T url, String headerName, String headerValue) {
        if (StringUtils.isNotBlank(headerValue)) {
            url.setHeader(headerName, headerValue);
        }

    }
}
