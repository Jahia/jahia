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

package org.jahia.data.containers;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaContainerManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.version.EntryLoadRequest;
import org.springframework.context.ApplicationContext;

/**
 * Retrieves the unpublished container IDs in the site by the container
 * definition name.
 * 
 * @author Sergiy Shyrkov
 */
public class StagedContainerFilterByContainerDefinition extends
        ContainerFilterByContainerDefinition {
    private Set pagesID;

    /**
     * Initializes an instance of this class.
     * 
     * @param containerDefinitionName
     *            target container definition name
     * @param siteId
     *            the site ID
     */
    public StagedContainerFilterByContainerDefinition(
            String containerDefinitionName, int siteId) {
        super(containerDefinitionName, EntryLoadRequest.STAGED);
    }
    /**
     * Initializes an instance of this class. That will filter for the list id found in the tag
     *
     * @param siteId
     *            the site ID
     */
    public StagedContainerFilterByContainerDefinition(int siteId) {
        super(null,EntryLoadRequest.STAGED);
    }
    /**
     * Initializes an instance of this class. That will filter for the definition name in all pages passed in the Set.
     *
     * @param siteId the site ID
     * @param pagesID set of Integer referrring to pages id
     * @param containerDefinitionName the container definition to look at in the pages
     */
    public StagedContainerFilterByContainerDefinition(int siteId, Set pagesID, String containerDefinitionName) {
        super(containerDefinitionName,EntryLoadRequest.STAGED);
        this.pagesID = pagesID;
    }
    /**
     * The expected result is a bit set of matching container ids.
     *
     * @param ctnListID, the container list id
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     */
    private BitSet doFiltering(int ctnListID) throws JahiaException {
        return doFilteringBySite(null, null, ctnListID);
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
     * @param siteIds
     * @param containerDefinitionNames
     * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
     * @throws JahiaException
     */
    public BitSet doFilterBySite(Integer[] siteIds, String[] containerDefinitionNames, int listId)
    throws JahiaException
    {

        BitSet result = null;
        result = doFilteringBySite(siteIds, containerDefinitionNames, listId);
        return result;
    }

    /**
    *
    * The expected result is a bit set of matching container ids for a given siteId.
    * if siteId = -1 , return result from all sites
    *
    * If the containerDefinitionName is null, return result from all containers
    * no regards to it definition !
    *
    * @param siteIds
     * @param containerDefinitionNames
    * @return BitSet bits, the expected result as a bit set of matching ctn ids,each bit position set to true correspond to matching ctn ids.
    * @throws JahiaException
    */
   private BitSet doFilteringBySite(Integer[] siteIds,
            String[] containerDefinitionNames, int listId) throws JahiaException {
        ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        JahiaContainerManager containerMgr = (JahiaContainerManager) context
                .getBean(JahiaContainerManager.class.getName());
        List ctnIds = containerMgr.getContainerIds(new Integer(listId),
                siteIds, new Boolean((siteIds != null && siteIds.length>0)),
                containerDefinitionNames, this.entryLoadRequest, true, true,
                true, (siteIds != null && siteIds.length>0),pagesID);

        BitSet bits = new BitSet();

        for (Iterator it = ctnIds.iterator(); it.hasNext();) {
            Object[] row = (Object[]) it.next();
            int ctnID = ((Integer) row[0]).intValue();
            bits.set(ctnID);

        }
        return bits;
    }

}