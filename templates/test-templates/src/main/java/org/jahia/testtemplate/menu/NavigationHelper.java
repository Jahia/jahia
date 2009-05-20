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
