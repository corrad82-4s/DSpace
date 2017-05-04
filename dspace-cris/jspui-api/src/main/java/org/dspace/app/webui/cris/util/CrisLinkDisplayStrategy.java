/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.cris.util;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.dspace.app.webui.util.IDisplayMetadataValueStrategy;
import org.dspace.browse.BrowseDSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.discovery.IGlobalSearchResult;

public class CrisLinkDisplayStrategy implements IDisplayMetadataValueStrategy
{
    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, String field,
            List<MetadataValue> metadataArray, BrowseDSpaceObject item,
            boolean disableCrossLinks, boolean emph)
    {
        String metadata = "";
        metadata = internalDisplay(metadataArray, metadata);
        return metadata;
    }

    private String internalDisplay(List<MetadataValue> metadataArray, String metadata)
    {
        if (metadataArray!=null && metadataArray.size() > 0)
        {
        	String[] splitted = metadataArray.get(0).getValue().split("\\|\\|\\|");
		    if (splitted.length > 2)
		    {
		        throw new IllegalArgumentException("Invalid text string for link: "+ metadataArray.get(0).getValue());
		    }
		    
		    metadata = (splitted.length == 2?"<a href=\""+splitted[1]+"\">"+splitted[1]+"</a>":splitted[0]);
        }
        return metadata;
    }
    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, String field,
            List<MetadataValue> metadataArray, Item item, boolean disableCrossLinks,
            boolean emph)
    {
        // not used
        return null;
    }
    @Override
    public String getExtraCssDisplay(HttpServletRequest hrq, int limit,
            boolean b, String browseType, int colIdx, String field,
            List<MetadataValue> metadataArray, Item item, boolean disableCrossLinks,
            boolean emph) throws JspException
    {
        return null;
    }

    @Override
    public String getExtraCssDisplay(HttpServletRequest hrq, int limit,
            boolean b, String browseType, int colIdx, String field,
            List<MetadataValue> metadataArray, BrowseDSpaceObject browseItem,
            boolean disableCrossLinks, boolean emph)
            throws JspException
    {
        return null;
    }
    
	@Override
	public String getMetadataDisplay(HttpServletRequest hrq, int limit, boolean viewFull, String browseType,
			int colIdx, String field, List<MetadataValue> metadataArray, IGlobalSearchResult item, boolean disableCrossLinks,
			boolean emph) throws JspException {
        String metadata = "";
        metadata = internalDisplay(metadataArray, metadata);
        return metadata;
	}
}