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
package org.jahia.taglibs.template.box;

import org.jahia.taglibs.template.containerlist.ContainerListTag;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;

import javax.servlet.jsp.JspException;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class BoxListTag extends ContainerListTag {
    protected int pageLevel = -1;
    protected int pageID = -1;
    protected String pageKey;

    public void setPageKey(String pageKey) {
        this.pageKey = pageKey;
    }

    public void setPageLevel(int pageLevel) {
        this.pageLevel = pageLevel;
    }

    public void setPageID(int pageID) {
        this.pageID = pageID;
    }

    // reads the container list from a container set
    protected JahiaContainerList getContainerList(final JahiaData jData,
                                                  final String listName) throws JahiaException {
        if(listName ==null || "".equals(listName)) return null;
        final JahiaContainerList containerList;
        if (pageID > 0 || pageLevel > 0 || (pageKey != null && pageKey.length() > 0)) {
            int id = pageID;
            if (id == -1 && pageLevel != -1) {
                id = jData.gui().getLevelID(pageLevel);
            }
            if (id == -1 && pageKey != null) {
                containerList = jData.containers().getAbsoluteContainerList(listName, pageKey);
            } else {
                containerList = jData.containers().getAbsoluteContainerList(listName, id);
            }
            if (containerList != null) {
                containerList.setMaxSize(this.getMaxSize());
            }
        } else {
            containerList = jData.containers().getContainerList(listName);
        }
        return containerList;
    }

    public int doEndTag() throws JspException {
        pageID = -1;
        pageLevel = -1;
        pageKey = null;
        return super.doEndTag();
    }
}
