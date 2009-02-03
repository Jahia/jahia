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

package org.jahia.services.containers;

import org.jahia.registries.ServicesRegistry;

import java.util.*;

/**
 * Used to load container's acl Ids using batch sql query.
 * 
 * User: hollis Date: 22 janv. 2008 Time: 09:19:48
 */
public class ContainerACLRetriever {

    private Map<Integer, Integer> ctnACLs = new HashMap<Integer, Integer>();
    private int batchSize = 50;
    private List<Integer> ctnIds;

    /**
     * The List of all container ids.
     * 
     * @param ctnIds
     *            the List containing all ctnids
     * @param batchSize
     *            the batch size used to load the next acl ids
     */
    public ContainerACLRetriever(List<Integer> ctnIds, int batchSize) {
        this.ctnIds = ctnIds;
        this.batchSize = batchSize;
    }

    /**
     * 
     * @param ctnId
     *            The current container id
     * @param index
     *            The index position of the ctnId in the List
     * @return -1 if not found
     */
    public int getACL(int ctnId, int index) {
        Integer aclId = ctnACLs.get(new Integer(ctnId));
        if (aclId != null) {
            return aclId.intValue();
        }
        loadACLs(index);
        aclId = ctnACLs.get(new Integer(ctnId));
        if (aclId != null) {
            return aclId.intValue();
        }
        return -1;
    }

    private void loadACLs(int index) {
        int maximumEndIndex = index + this.batchSize;
        List<Integer> ids = ctnIds.subList(index, maximumEndIndex < ctnIds
                .size() ? maximumEndIndex : ctnIds.size());
        
        Map<Integer, Integer> acls = ServicesRegistry.getInstance().getJahiaContainersService()
                .getContainerACLIDs(ids);
        
        if (acls != null) {
            ctnACLs.putAll(acls);
        }
    }

}
