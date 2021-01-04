/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.template.generator;

import org.dspace.content.Item;
import org.dspace.core.Context;

/**
 * Defines the contract to generate custom metadata values in a dynamic fashion.
 *
 * @author Corrado Lombardi (corrado.lombardi at 4science.it)
 */
public interface TemplateValueGenerator {

    /**
     * Generate a dynamic value according to template item value syntax
     * @param context DSpace current context
     * @param targetItem item which metadata have to be set
     * @param templateItem item source of metadata value
     * @param extraParams custom params, related to the implementation
     * @return
     */
    String generator(Context context, Item targetItem, Item templateItem,
                     String extraParams);
}
