/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;
import static org.dspace.app.launcher.ScriptLauncher.handleScript;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.amazonaws.util.StringInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.dspace.app.launcher.ScriptLauncher;
import org.dspace.app.metrics.CrisMetrics;
import org.dspace.app.metrics.service.CrisMetricsService;
import org.dspace.app.rest.matcher.CrisMetricsMatcher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.app.scripts.handler.impl.TestDSpaceRunnableHandler;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.CrisMetricsBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.externalservices.wos.UpdateWOSMetrics;
import org.dspace.externalservices.wos.WOSPersonRestConnector;
import org.dspace.externalservices.wos.WOSRestConnector;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author mykhaylo boychuk (mykhaylo.boychuk at 4science.it)
 */
public class UpdateWOSMetricsIT extends AbstractControllerIntegrationTest {

    @Autowired
    private CrisMetricsService crisMetriscService;

    @Autowired
    private WOSRestConnector wosRestConnector;

    @Autowired
    private WOSPersonRestConnector wosPersonRestConnector;

    @Test
    public void updateCrisMetricsFromWosMockitoTest() throws Exception {
        context.turnOffAuthorisationSystem();

        HttpClient originalHttpClient = wosRestConnector.getHttpClient();
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Item itemA = null;
        try (FileInputStream file = new FileInputStream(testProps.get("test.wosResponceJSON").toString())) {

            String xmlMetricsExample = IOUtils.toString(file, Charset.defaultCharset());
            wosRestConnector.setHttpClient(httpClient);

            BasicHttpResponse basicHttpResponse = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "OK");
            basicHttpResponse.setEntity(new BasicHttpEntity());
            InputStream inputStream = new StringInputStream(xmlMetricsExample);
            BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
            basicHttpResponse.setEntity(basicHttpEntity);
            basicHttpEntity.setChunked(true);
            basicHttpEntity.setContent(inputStream);

            when(httpClient.execute(ArgumentMatchers.any())).thenReturn(basicHttpResponse);

            parentCommunity = CommunityBuilder.createCommunity(context)
                                              .withName("Parent Community").build();

            Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                                               .withRelationshipType("Publication")
                                               .withName("Collection 1").build();

            itemA = ItemBuilder.createItem(context, col1)
                               .withTitle("Title item A")
                               .withDoiIdentifier("10.1016/j.jinf.2020.06.024").build();

            CrisMetrics metric1 = CrisMetricsBuilder.createCrisMetrics(context, itemA)
                                                    .withMetricType(UpdateWOSMetrics.WOS_METRIC_TYPE)
                                                    .withMetricCount(23)
                                                    .isLast(true).build();

            context.restoreAuthSystemState();

            String[] args = new String[] { "update-metrics", "-s", "wos" };
            TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

            assertEquals(0, handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, admin));

            CrisMetrics metric = crisMetriscService.findLastMetricByResourceIdAndMetricsTypes(context,
                                     UpdateWOSMetrics.WOS_METRIC_TYPE, itemA.getID());

            assertNotEquals(metric1.getID(), metric.getID());
            assertFalse(metric1.getLast());
            assertTrue(metric.getLast());
            assertEquals(87, metric.getMetricCount(), 0);
        } finally {
            CrisMetricsBuilder.deleteCrisMetrics(itemA);
            wosRestConnector.setHttpClient(originalHttpClient);
        }
    }

    @Test
    public void wosResponceMetricCountAbsentMockitoTest() throws Exception {
        context.turnOffAuthorisationSystem();

        HttpClient originalHttpClient = wosRestConnector.getHttpClient();
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Item itemA = null;
        try (FileInputStream file = new FileInputStream(testProps.get(
                        "test.wosResponceMetricCountAbsent").toString())) {

            String xmlMetricsExample = IOUtils.toString(file, Charset.defaultCharset());
            wosRestConnector.setHttpClient(httpClient);

            BasicHttpResponse basicHttpResponse = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "OK");
            basicHttpResponse.setEntity(new BasicHttpEntity());
            InputStream inputStream = new StringInputStream(xmlMetricsExample);
            BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
            basicHttpResponse.setEntity(basicHttpEntity);
            basicHttpEntity.setChunked(true);
            basicHttpEntity.setContent(inputStream);

            when(httpClient.execute(ArgumentMatchers.any())).thenReturn(basicHttpResponse);

            parentCommunity = CommunityBuilder.createCommunity(context)
                                              .withName("Parent Community").build();

            Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                                               .withRelationshipType("Publication")
                                               .withName("Collection 1").build();

            itemA = ItemBuilder.createItem(context, col1)
                               .withTitle("Title item A")
                               .withDoiIdentifier("10.1016/j.ji").build();

            CrisMetrics metric1 = CrisMetricsBuilder.createCrisMetrics(context, itemA)
                                                    .withMetricType(UpdateWOSMetrics.WOS_METRIC_TYPE)
                                                    .withMetricCount(2312)
                                                    .isLast(true).build();

            context.restoreAuthSystemState();

            String[] args = new String[] { "update-metrics", "-s", "wos" };
            TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

            assertEquals(0, handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, admin));

            CrisMetrics metric = crisMetriscService.findLastMetricByResourceIdAndMetricsTypes(context,
                                     UpdateWOSMetrics.WOS_METRIC_TYPE, itemA.getID());

            assertEquals(metric1.getID(), metric.getID());

            String tokenAdmin = getAuthToken(admin.getEmail(), password);
            getClient(tokenAdmin).perform(get("/api/cris/metrics/" + metric.getID()))
                                 .andExpect(status().isOk())
                                 .andExpect(jsonPath("$", is(CrisMetricsMatcher.matchCrisMetrics(metric))))
                                 .andExpect(jsonPath("$._links.self.href", Matchers.containsString(
                                                     "/api/cris/metrics/" + metric.getID())));
        } finally {
            CrisMetricsBuilder.deleteCrisMetrics(itemA);
            wosRestConnector.setHttpClient(originalHttpClient);
        }
    }

    @Test
    public void badRequestMockitoTest() throws Exception {
        context.turnOffAuthorisationSystem();

        HttpClient originalHttpClient = wosRestConnector.getHttpClient();
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Item itemA = null;
        try (FileInputStream file = new FileInputStream(testProps.get("test.wosBadRequest").toString())) {

            String xmlMetricsExample = IOUtils.toString(file, Charset.defaultCharset());
            wosRestConnector.setHttpClient(httpClient);

            BasicHttpResponse basicHttpResponse = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "OK");
            basicHttpResponse.setEntity(new BasicHttpEntity());
            InputStream inputStream = new StringInputStream(xmlMetricsExample);
            BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
            basicHttpResponse.setEntity(basicHttpEntity);
            basicHttpEntity.setChunked(true);
            basicHttpEntity.setContent(inputStream);

            when(httpClient.execute(ArgumentMatchers.any())).thenReturn(basicHttpResponse);

            parentCommunity = CommunityBuilder.createCommunity(context)
                                              .withName("Parent Community").build();

            Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                                               .withRelationshipType("Publication")
                                               .withName("Collection 1").build();

            itemA = ItemBuilder.createItem(context, col1)
                               .withTitle("Title item A")
                               .withDoiIdentifier("10.1016/j.ji").build();

            CrisMetrics metric1 = CrisMetricsBuilder.createCrisMetrics(context, itemA)
                                                    .withMetricType(UpdateWOSMetrics.WOS_METRIC_TYPE)
                                                    .withMetricCount(2312)
                                                    .isLast(true).build();

            context.restoreAuthSystemState();

            String[] args = new String[] { "update-metrics", "-s", "wos" };
            TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

            assertEquals(0, handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, admin));

            CrisMetrics metric = crisMetriscService.findLastMetricByResourceIdAndMetricsTypes(context,
                                     UpdateWOSMetrics.WOS_METRIC_TYPE, itemA.getID());

            assertEquals(metric1.getID(), metric.getID());

        } finally {
            CrisMetricsBuilder.deleteCrisMetrics(itemA);
            wosRestConnector.setHttpClient(originalHttpClient);
        }
    }

    @Test
    public void updateCrisMetricsWithPersonEntityTypeMockitoTest() throws Exception {
        context.turnOffAuthorisationSystem();

        HttpClient originalHttpClient = wosPersonRestConnector.getHttpClient();
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Item itemA = null;
        try (FileInputStream file = new FileInputStream(testProps.get("test.wosResponceMultiRecords").toString())) {

            String wosMetricsExample = IOUtils.toString(file, Charset.defaultCharset());
            wosPersonRestConnector.setHttpClient(httpClient);

            BasicHttpResponse basicHttpResponse = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "OK");
            basicHttpResponse.setEntity(new BasicHttpEntity());
            InputStream inputStream = new StringInputStream(wosMetricsExample);
            BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
            basicHttpResponse.setEntity(basicHttpEntity);
            basicHttpEntity.setChunked(true);
            basicHttpEntity.setContent(inputStream);

            when(httpClient.execute(ArgumentMatchers.any())).thenReturn(basicHttpResponse);

            parentCommunity = CommunityBuilder.createCommunity(context)
                                              .withName("Parent Community").build();

            Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                                               .withRelationshipType("Person")
                                               .withName("Collection 1").build();

            itemA = ItemBuilder.createItem(context, col1)
                               .withTitle("Title item A")
                               .withOrcidIdentifier("0000-0001-8190-0000").build();

            CrisMetrics metric = CrisMetricsBuilder.createCrisMetrics(context, itemA)
                                                    .withMetricType(UpdateWOSMetrics.WOS_PERSON_METRIC_TYPE)
                                                    .withMetricCount(22)
                                                    .isLast(true).build();

            context.restoreAuthSystemState();

            String[] args = new String[] { "update-metrics", "-s", "wos", "-p", "person" };
            TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

            assertEquals(0, handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, admin));

            CrisMetrics wosMetric = crisMetriscService.findLastMetricByResourceIdAndMetricsTypes(context,
                                                       UpdateWOSMetrics.WOS_PERSON_METRIC_TYPE, itemA.getID());

            assertNotEquals(metric.getId(), wosMetric.getId());
            assertEquals(wosMetric.getMetricCount(), 280, 0);
            assertEquals(wosMetric.getMetricType(), UpdateWOSMetrics.WOS_PERSON_METRIC_TYPE);
        } finally {
            CrisMetricsBuilder.deleteCrisMetrics(itemA);
            wosPersonRestConnector.setHttpClient(originalHttpClient);
        }
    }

    @Test
    public void updateCrisMetricsWithPersonEntityTypeBadRequestMockitoTest() throws Exception {
        context.turnOffAuthorisationSystem();

        HttpClient originalHttpClient = wosPersonRestConnector.getHttpClient();
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Item itemA = null;
        try (FileInputStream file = new FileInputStream(testProps.get("test.wosBadRequest").toString())) {

            String wosMetricsExample = IOUtils.toString(file, Charset.defaultCharset());
            wosPersonRestConnector.setHttpClient(httpClient);

            BasicHttpResponse basicHttpResponse = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "OK");
            basicHttpResponse.setEntity(new BasicHttpEntity());
            InputStream inputStream = new StringInputStream(wosMetricsExample);
            BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
            basicHttpResponse.setEntity(basicHttpEntity);
            basicHttpEntity.setChunked(true);
            basicHttpEntity.setContent(inputStream);

            when(httpClient.execute(ArgumentMatchers.any())).thenReturn(basicHttpResponse);

            parentCommunity = CommunityBuilder.createCommunity(context)
                                              .withName("Parent Community").build();

            Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                                               .withRelationshipType("Person")
                                               .withName("Collection 1").build();

            itemA = ItemBuilder.createItem(context, col1)
                               .withTitle("Title item A")
                               .withOrcidIdentifier("0000").build();

            CrisMetrics metric = CrisMetricsBuilder.createCrisMetrics(context, itemA)
                                                    .withMetricType(UpdateWOSMetrics.WOS_PERSON_METRIC_TYPE)
                                                    .withMetricCount(22)
                                                    .isLast(true).build();

            context.restoreAuthSystemState();

            String[] args = new String[] { "update-metrics", "-s", "wos", "-p", "person" };
            TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

            assertEquals(0, handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, admin));

            CrisMetrics wosMetric = crisMetriscService.findLastMetricByResourceIdAndMetricsTypes(context,
                                                       UpdateWOSMetrics.WOS_PERSON_METRIC_TYPE, itemA.getID());

            assertEquals(metric.getId(), wosMetric.getId());
            assertEquals(wosMetric.getMetricCount(), 22, 0);
            assertEquals(wosMetric.getMetricType(), UpdateWOSMetrics.WOS_PERSON_METRIC_TYPE);
        } finally {
            CrisMetricsBuilder.deleteCrisMetrics(itemA);
            wosPersonRestConnector.setHttpClient(originalHttpClient);
        }
    }

    @Test
    public void updateCrisMetricsWithPersonEntityTypeUnauthorizedMockitoTest() throws Exception {
        context.turnOffAuthorisationSystem();

        HttpClient originalHttpClient = wosPersonRestConnector.getHttpClient();
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Item itemA = null;
        try (FileInputStream file = new FileInputStream(testProps.get("test.wosUnauthorized").toString())) {

            String wosMetricsExample = IOUtils.toString(file, Charset.defaultCharset());
            wosPersonRestConnector.setHttpClient(httpClient);

            BasicHttpResponse basicHttpResponse = new BasicHttpResponse(
                                                  new ProtocolVersion("http", 1, 1), 401, "Unauthorized");
            basicHttpResponse.setEntity(new BasicHttpEntity());
            InputStream inputStream = new StringInputStream(wosMetricsExample);
            BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
            basicHttpResponse.setEntity(basicHttpEntity);
            basicHttpEntity.setChunked(true);
            basicHttpEntity.setContent(inputStream);

            when(httpClient.execute(ArgumentMatchers.any())).thenReturn(basicHttpResponse);

            parentCommunity = CommunityBuilder.createCommunity(context)
                                              .withName("Parent Community").build();

            Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                                               .withRelationshipType("Person")
                                               .withName("Collection 1").build();

            itemA = ItemBuilder.createItem(context, col1)
                               .withTitle("Title item A")
                               .withOrcidIdentifier("0000-0001-8190-0000").build();

            CrisMetrics metric = CrisMetricsBuilder.createCrisMetrics(context, itemA)
                                                    .withMetricType(UpdateWOSMetrics.WOS_PERSON_METRIC_TYPE)
                                                    .withMetricCount(22)
                                                    .isLast(true).build();

            context.restoreAuthSystemState();

            String[] args = new String[] { "update-metrics", "-s", "wos", "-p", "person" };
            TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

            assertEquals(0, handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, admin));

            CrisMetrics wosMetric = crisMetriscService.findLastMetricByResourceIdAndMetricsTypes(context,
                                                       UpdateWOSMetrics.WOS_PERSON_METRIC_TYPE, itemA.getID());

            assertEquals(metric.getId(), wosMetric.getId());
            assertEquals(wosMetric.getMetricCount(), 22, 0);
            assertEquals(wosMetric.getMetricType(), UpdateWOSMetrics.WOS_PERSON_METRIC_TYPE);
        } finally {
            CrisMetricsBuilder.deleteCrisMetrics(itemA);
            wosPersonRestConnector.setHttpClient(originalHttpClient);
        }
    }
}