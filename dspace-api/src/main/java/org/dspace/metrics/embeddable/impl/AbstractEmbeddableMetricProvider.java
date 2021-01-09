/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.metrics.embeddable.impl;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.dspace.content.Item;
import org.dspace.content.logic.Filter;
import org.dspace.content.logic.LogicalStatementException;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.metrics.embeddable.EmbeddableMetricProvider;
import org.dspace.metrics.embeddable.model.EmbeddableCrisMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractEmbeddableMetricProvider implements EmbeddableMetricProvider {

    protected static final Logger log = LoggerFactory.getLogger(AbstractEmbeddableMetricProvider.class);

    @Autowired(required = true)
    protected ItemService itemService;

    private Filter filterService;

    private boolean enabled = false;

    @Override
    public boolean hasMetric(Context context, Item item) {
        if (!this.isEnabled()) {
            return false;
        }
        try {
            Boolean result = getFilterService().getResult(context, item);
            log.debug("Result of filter for " + item.getHandle() + " is " + result.toString());
            return result;
        } catch (LogicalStatementException e) {
            log.error("Error evaluating item with logical filter: " + e.getLocalizedMessage());
            throw new IllegalStateException(e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<EmbeddableCrisMetrics> provide(Context context, Item item) {
        if (!this.hasMetric(context, item)) {
            return Optional.empty();
        }
        EmbeddableCrisMetrics metric = new EmbeddableCrisMetrics();
        metric.setEmbeddableId(this.getId(context, item));
        metric.setMetricType(this.getMetricType());
        metric.setRemark(this.innerHtml(context, item));
        return Optional.of(metric);
    }

    protected String getRelationshipType(Item item) {
        return itemService.getMetadataFirstValue(item, "relationship", "type", null, Item.ANY);
    }

    @Override
    public Optional<EmbeddableCrisMetrics> provide(Context context, String metricId) throws SQLException {
        UUID itemUuid = UUID.fromString(metricId.split("/")[0]);
        Item item = itemService.find(context, itemUuid);
        return provide(context, item);
    }

    @Override
    public boolean support(String metricId) {
        return metricId.split("/")[1].equals(this.getMetricType());
    }

    @Override
    public String getId(Context context, Item item) {
        return item.getID() + "/" + this.getMetricType();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Filter getFilterService() {
        return filterService;
    }

    public void setFilterService(Filter filterService) {
        this.filterService = filterService;
    }


}
