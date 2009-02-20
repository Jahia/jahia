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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
