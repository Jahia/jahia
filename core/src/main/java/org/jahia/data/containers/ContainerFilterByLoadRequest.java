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


import java.util.BitSet;
import java.util.Map;
import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.version.EntryLoadRequest;
import java.io.Serializable;



/**
 * This filter can be used to filter containers that exists for a given
 * EntryLoadRequest :
 *
 *  - live, staging workflow mode...
 *  - multi-langue
 *
 * @see ContainerFilters
 * @see JahiaContainerSet
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */
public class ContainerFilterByLoadRequest implements Serializable, ContainerFilterInterface {

    private EntryLoadRequest entryLoadRequest = EntryLoadRequest.CURRENT;

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param entryLoadRequest
     */
    public ContainerFilterByLoadRequest(EntryLoadRequest entryLoadRequest){

        if ( entryLoadRequest != null ){
            this.entryLoadRequest = entryLoadRequest;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering.
     * The expected result is a bit set of matching container ids.
     *
     * @param ctnListID, the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    public BitSet doFilter(int ctnListID)
    throws JahiaException
    {
        BitSet result = null;

        result = doFiltering(ctnListID);

        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * The expected result is a bit set of matching container ids.
     *
     * @param ctnListID, the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    private BitSet doFiltering(int ctnListID)
    throws JahiaException
    {
        BitSet result = new BitSet();
        List<Integer> ids = ServicesRegistry.getInstance().getJahiaContainersService()
                .getctnidsInList(ctnListID,this.entryLoadRequest);
        int size = ids.size();
        for (int i=0; i<size; i++){
            Integer I = (Integer)ids.get(i);
            result.set(I.intValue());
        }
        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the select statement, build with the clauses for all container list of the site.
     *
     * @param ctnListID, the container list id
     * @return String , the sql statement. Null on error
     */
    public String getSelect(int ctnListID, int filterId, Map<String, Object> parameters)
    {
        return ctnListID + "_" + this.entryLoadRequest.toString();
    }

    //--------------------------------------------------------------------------
    /**
     * Set reference to a containerFilters
     *
     * @return
     * @throws JahiaException
     */
    public void setContainerFilters(ContainerFilters containerFilters){
        // do nothing
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     *
     * The expected result is a bit set of matching container ids.
     *
     * If siteId = -1 , returns results from all sites
     *
     * If the containerDefinitionName is null, return result from all containers
     * no regards to it definition !
     *
     * @todo : actually, return all containers no distinction of type
     *
     * @param siteId
     * @param containerDefinitionName
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(int siteId, String containerDefinitionName, int listId)
    throws JahiaException
    {
        Integer[] siteIds = null;
        if (siteId != -1){
            siteIds = new Integer[]{new Integer(siteId)};
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null && !"".equals(containerDefinitionName.trim())){
            containerDefinitionNames = new String[]{containerDefinitionName};
        }
        return doFilterBySite(siteIds,containerDefinitionNames,listId);
    }

    //--------------------------------------------------------------------------
    /**
     * Perform filtering on a given site or all sites
     *
     * The expected result is a bit set of matching container ids.
     *
     *
     * @todo : actually, return all containers no distinction of type
     *
     * @param siteIds
     * @param containerDefinitionNames
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(Integer[] siteIds, String[] containerDefinitionNames, int listId)
    throws JahiaException
    {

        BitSet result = null;
        result = doFilteringBySite(siteIds, containerDefinitionNames);
        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * siteId and containerDefinitionName are ignored here
     *
     * @param siteIds
     * @param containerDefinitionNames
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    private BitSet doFilteringBySite(Integer[] siteIds,
            String[] containerDefinitionNames)
    throws JahiaException
    {
        BitSet result = new BitSet();
        for (Integer I : ServicesRegistry.getInstance().getJahiaContainersService()
                .getCtnIds(this.entryLoadRequest)){
            result.set(I.intValue());
        }
        return result;
    }

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
     * @return
     */
    public String getSelectBySiteID(int siteId, String containerDefinitionName, int filterId, Map<String, Object> parameters)
    {
        // It's a dummy select
        return siteId + "_" + containerDefinitionName + "_"
                + this.entryLoadRequest.toString();
    }

    /**
     * Return the select statement, build with the clauses for a given site.
     *
     * @param siteIds
     * @param containerDefinitionNames
     * @return
     */
    public String getSelectBySiteID(Integer[] siteIds, String[] containerDefinitionNames, int filterId, Map<String, Object> parameters)
    {
        // It's a dummy select
        StringBuffer buffer = new StringBuffer();
        if (siteIds!=null && siteIds.length>0){
            for (int i=0; i<siteIds.length; i++){
                buffer.append(siteIds[i].toString());
                if (i<siteIds.length-1){
                    buffer.append("_");
                }
            }
        } else {
            buffer.append("null");
        }
        buffer.append("_");
        if (containerDefinitionNames!=null && containerDefinitionNames.length>0){
            for (int i=0; i<containerDefinitionNames.length; i++){
                buffer.append(containerDefinitionNames[i]);
                if (i<containerDefinitionNames.length-1){
                    buffer.append("_");
                }
            }
        } else {
            buffer.append("null");
        }
        return buffer.toString() + "_" + this.entryLoadRequest.toString();
    }

}
