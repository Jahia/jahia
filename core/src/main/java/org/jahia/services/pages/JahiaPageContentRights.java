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
package org.jahia.services.pages;

import java.util.HashSet;
import java.util.Set;

public class JahiaPageContentRights {
    private Integer pageID;
    private Integer parentPageID;
    private Integer aclID;
    private Set<Integer> childrenPages = new HashSet<Integer>();

    public JahiaPageContentRights(Integer pageID, Integer parentPageID,
            Integer aclID) {
        super();
        this.pageID = pageID;
        this.aclID = aclID;
        this.parentPageID = parentPageID;
    }

    public Integer getPageID() {
        return pageID;
    }

    public Integer getAclID() {
        return aclID;
    }

    public Set<Integer> getChildrenPages() {
        return childrenPages;
    }

    public Integer getParentPageID() {
        return parentPageID;
    }
}
