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
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.testtemplate.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.beans.RequestBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.services.pages.JahiaPage;

public class NavigationHelper {
    private static final Logger logger = Logger
            .getLogger(NavigationHelper.class);

    private RequestBean currentRequest;

    public NavigationHelper() {
        super();
    }

    public RequestBean getCurrentRequest() {
        return currentRequest;
    }

    public void setCurrentRequest(RequestBean currentRequest) {
        this.currentRequest = currentRequest;
    }

    public Collection getItems() throws JahiaException {

        ParamBean jParams = (ParamBean) getCurrentRequest().getProcessingContext();
        int startPageId = jParams.getSite().getHomePageID();
        JahiaData jData = (JahiaData) jParams.getRequest().getAttribute(
                "org.jahia.data.JahiaData");

        Collection treeItems = getPageSubTree(jData, startPageId, 1, true, true);

        return (treeItems);
    }

    private Collection getPageSubTree(JahiaData jData, int currentPageId,
            int level, boolean isFirst, boolean isNotALink)
            throws JahiaException {
        Collection treeItems = new ArrayList();
        JahiaContainerList leftMenuContainerList = jData.containers()
                .getAbsoluteContainerList("leftMenuContainerList",
                        currentPageId);
        boolean editMode = jData.gui().isEditMode();

        if (leftMenuContainerList != null) {
            Iterator navigationContainers = leftMenuContainerList
                    .getContainers();
            isNotALink = leftMenuContainerList.getContentContainerList()
                    .getPickedObject() == null &&
                    isNotALink;
            while (navigationContainers.hasNext()) {
                JahiaContainer navigationContainer = (JahiaContainer) navigationContainers
                        .next();
                JahiaPage leftLink = (JahiaPage) navigationContainer
                        .getFieldObject("leftLink");
                NavigationTreeItem navItem = new NavigationTreeItem();
                navItem.setFirst(isFirst);
                navItem.setLevel(level);
                navItem.setContentBean(new ContainerBean(navigationContainer,
                        jData.getProcessingContext()));

                if (leftLink != null) {
                    navItem.setUrl(leftLink
                            .getURL(jData.getProcessingContext()));
                    navItem.setTitle(leftLink.getHighLightDiffTitle(jData
                            .getProcessingContext()));
                    if (leftLink.getID() == jData.page().getID()) {
                        navItem.setCurrent(true);
                    }
                    if (editMode &&
                            leftLink.getContentPage().isMarkedForDelete()) {
                        navItem.setMarkedForDelete(true);
                    }
                }
                if (editMode && (leftLink == null || isNotALink)){
                    navItem.setAddActionMenu(true);
                }

                treeItems.add(navItem);

                if (leftLink != null &&
                        jData.gui().isPageInPath(leftLink.getID())) {
                    treeItems.addAll(getPageSubTree(jData, leftLink.getID(),
                            level + 1, false, isNotALink));
                }

            }

            if (editMode && isNotALink) {
                NavigationTreeItem navItem = new NavigationTreeItem();
                navItem.setLevel(level + 1);
                navItem.setContentBean(new ContainerListBean(
                        leftMenuContainerList, jData.getProcessingContext()));

                treeItems.add(navItem);
            }

        }
        return (treeItems);
    }

}
