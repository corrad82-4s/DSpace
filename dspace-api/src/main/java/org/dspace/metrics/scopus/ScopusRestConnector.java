/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.metrics.scopus;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.Logger;

/**
 * This class deals with logic management to connect to the SCOPUS external service
 * 
 * @author Mykhaylo Boychuk (mykhaylo.boychuk at 4science.it)
 */
public class ScopusRestConnector {

    private static Logger log = org.apache.logging.log4j.LogManager.getLogger(ScopusRestConnector.class);

    private String apiKey;
    private String insttoken;
    private String scopusUrl;

    private HttpClient httpClient;

    @PostConstruct
    private void setup() {
        this.httpClient = HttpClientBuilder.create()
            .setConnectionManager(new PoolingHttpClientConnectionManager())
            .build();
    }

    public InputStream get(String query) {
        try {
            return sendRequestToScopus(query);
        } catch (Exception e) {
            log.warn("Error while calling scopus with query " + query + ": " +
                e.getMessage(), e);
            return null;
        }
    }

    private InputStream sendRequestToScopus(String query)
            throws IOException {
        HttpPost httpPost = new HttpPost(scopusUrl + URLEncoder.encode(query, StandardCharsets.UTF_8));
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("Connection", "keep-alive");
        httpPost.setHeader("X-ELS-APIKey", apiKey);
        httpPost.setHeader("X-ELS-Insttoken", insttoken);
        httpPost.setHeader("Accept", "application/xml");

        HttpResponse response = httpClient.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            log.error("Error connecting to server! The Server responce with: " + statusCode);
            throw new RuntimeException();
        }
        return response.getEntity().getContent();
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getScopusUrl() {
        return scopusUrl;
    }

    public void setScopusUrl(String scopusUrl) {
        this.scopusUrl = scopusUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getInsttoken() {
        return insttoken;
    }

    public void setInsttoken(String insttoken) {
        this.insttoken = insttoken;
    }
}
