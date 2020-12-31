/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.metrics.wos;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.Logger;
import org.dspace.metrics.scopus.CrisMetricDTO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Mykhaylo Boychuk (mykhaylo.boychuk at 4science.it)
 */
public class WOSPersonRestConnector {

    private static Logger log = org.apache.logging.log4j.LogManager.getLogger(WOSPersonRestConnector.class);

    private String apiKey;
    private String wosUrl;
    private HttpClient httpClient;

    @PostConstruct
    private void setup() {
        this.httpClient = HttpClientBuilder.create()
            .setConnectionManager(new PoolingHttpClientConnectionManager())
            .build();
    }

    public CrisMetricDTO sendRequestToWOS(String id)
            throws UnsupportedEncodingException, IOException, ClientProtocolException {
        double total = 0;
        int record = 0;
        final int count = 100;
        int recordsFound = -1;
        JSONObject json = null;
        JSONArray records = null;
        boolean error = false;
        CrisMetricDTO metricDTO = new CrisMetricDTO();
        log.info("Starting wos call for person: " + id);
        while (!error && (recordsFound == -1 || record < recordsFound)) {
            recordsFound = 0;
            HttpGet httpPost = new HttpGet(wosUrl.concat("AI=(").concat(URLEncoder.encode(id, StandardCharsets.UTF_8))
                .concat(")&count=")
                    .concat(String.valueOf(count)).concat("&firstRecord=").concat(String.valueOf(record + 1)));
            httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
            httpPost.setHeader("Connection", "keep-alive");
            httpPost.setHeader("X-ApiKey", apiKey);
            httpPost.setHeader("Accept", "application/json");

            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            log.info("wos person status code: " + statusCode);
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            try {
                json = new JSONObject(IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset()));
                if (StringUtils
                        .isBlank(json.getJSONObject("Data").getJSONObject("Records").get("records").toString())) {
                    log.info("wos person data records blank");
                    return null;
                }
                recordsFound = json.getJSONObject("QueryResult").getInt("RecordsFound");
                records = json.getJSONObject("Data")
                              .getJSONObject("Records")
                              .getJSONObject("records")
                              .getJSONArray("REC");
                log.info("recordsFound:" + recordsFound);
                log.info("records:" + records);
            } catch (JSONException | IOException e) {
                log.error("Error while parsing wos person json");
                log.error(e.getMessage(), e);
                error = true;
            }
            record += records.length();
            total += sumMetricCounts(records);
            if (records.length() < count) {
                // to be safe in the case the wos api would return less records than what initially reported
                break;
            }
        }
        log.info("updating metric: " + total);
        metricDTO.setMetricCount(total);
        metricDTO.setMetricType(UpdateWOSPersonMetrics.WOS_PERSON_METRIC_TYPE);
        return metricDTO;
    }

    private int sumMetricCounts(JSONArray records) {
        int total = 0;
        if (Objects.nonNull(records)) {
            for (int i = 0; i < records.length(); i++) {
                Integer count = records.getJSONObject(i)
                                       .getJSONObject("dynamic_data")
                                       .getJSONObject("citation_related")
                                       .getJSONObject("tc_list")
                                       .getJSONObject("silo_tc")
                                       .getInt("local_count");
                if (Objects.nonNull(count)) {
                    total += count.intValue();
                }
            }
        }
        return total;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getWosUrl() {
        return wosUrl;
    }

    public void setWosUrl(String wosUrl) {
        this.wosUrl = wosUrl;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

}