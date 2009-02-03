/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
