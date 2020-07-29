/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.authority.factory;

import java.util.Map;

import org.dspace.content.authority.service.ItemAuthorityService;

/**
 * Factory implementation to get services for the content.authority package, use
 * ItemAuthorityServiceFactory.getInstance() to retrieve an implementation
 *
 * @author Giuseppe Digilio (giuseppe dot digilio at 4science dot it)
 */
public class ItemAuthorityServiceFactory {

    private Map<String, ItemAuthorityService> itemAthorityRelationshipTypeMap;

    public Map<String, ItemAuthorityService> getItemAthorityRelationshipTypeMap() {
        return itemAthorityRelationshipTypeMap;
    }

    public void setItemAthorityRelationshipTypeMap(
            Map<String, ItemAuthorityService> itemAthorityRelationshipTypeMap
    ) {
        this.itemAthorityRelationshipTypeMap = itemAthorityRelationshipTypeMap;
    }

    public ItemAuthorityService getInstance(String entityType) {
        return (entityType != null
                && itemAthorityRelationshipTypeMap.containsKey(entityType))
                    ? itemAthorityRelationshipTypeMap.get(entityType) :
                        itemAthorityRelationshipTypeMap.get("default");
    }
}
