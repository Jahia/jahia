/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
//
//
//
//

package org.jahia.data.containers;


import org.jahia.exceptions.JahiaException;

import java.util.BitSet;
import java.util.Map;

public interface ContainerFilterInterface {


    //--------------------------------------------------------------------------
    /**
     * Perform filtering.
     * The expected result is a bit set of matching container ids.
     *
     * @param ctnListID, the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    public abstract BitSet doFilter(int ctnListID)
    throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for all container list of the site.
     *
     * @param ctnListID, the container list id
     * @return String , the sql statement. Null on error
     */
    public abstract String getSelect(int ctnListID, int filterId, Map<String, Object> parameters);

    //--------------------------------------------------------------------------
    /**
     * Set reference to a containerFilters
     *
     * @return
     * @throws JahiaException
     */
    public abstract void setContainerFilters(ContainerFilters containerFilters);

    //--------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     *
     * The expected result is a bit set of matching container ids.
     *
     * If the containerDefinitionName is null, return result from all containers
     * no regards to it definition !
     *
     * @param siteId if -1 all sites are allowed
     * @param containerDefinitionName  if null or empty, all definitions are allowed
     * @param listId , optionally, a listID can be passed to returns all containers of this containerlist.
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @deprecated  use doFilterBySite(int[] siteIds, String[] containerDefinitionNames, int listId);
     * @throws JahiaException
     */
    public abstract BitSet doFilterBySite(int siteId, String containerDefinitionName, int listId)
    throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     *
     * The expected result is a bit set of matching container ids.
     *
     * If the containerDefinitionName is null, return result from all containers
     * no regards to it definition !
     *
     * @param siteIds if null or empty, all sites are allowed
     * @param containerDefinitionNames  if null or empty, all definition are allowed
     * @param listId , optionally, a listID can be passed to returns all containers of this containerlist.
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    public abstract BitSet doFilterBySite(Integer[] siteIds, String[] containerDefinitionNames, int listId)
    throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for a given site.
     * If siteId = -1 -> build query for all sites
     *
     * If the containerDefinitionName is null, return result from all containers
     * no regards to it definition !
     *
     * @param siteIds
     * @param containerDefinitionNames
     * @return
     */
    public abstract String getSelectBySiteID(Integer[] siteIds,
            String[] containerDefinitionNames, int filterId, Map<String, Object> parameters);

    //--------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for a given site.
     * If siteId = -1 -> build query for all sites
     *
     * If the containerDefinitionName is null, return result from all containers
     * no regards to it definition !
     *
     * @param siteId
     * @param containerDefinitionName
     * @deprecated use getSelectBySiteID(int[] siteIds, String[] containerDefinitionNames)
     * @return
     */
    public abstract String getSelectBySiteID(int siteId,
            String containerDefinitionName, int filterId, Map<String, Object> parameters);

}
