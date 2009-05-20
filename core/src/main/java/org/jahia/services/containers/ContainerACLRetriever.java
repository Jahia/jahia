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
