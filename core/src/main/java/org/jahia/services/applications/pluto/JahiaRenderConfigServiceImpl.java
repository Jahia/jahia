/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.applications.pluto;

import org.apache.pluto.driver.services.portal.PageConfig;
import org.apache.pluto.driver.services.impl.resource.RenderConfigServiceImpl;
import org.apache.pluto.driver.PortalDriverServlet;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPortletNode;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;


import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.QueryManager;
import javax.jcr.RepositoryException;
import javax.jcr.NodeIterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 8 avr. 2009
 * Time: 16:12:17
 */
public class JahiaRenderConfigServiceImpl extends RenderConfigServiceImpl {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaRenderConfigServiceImpl.class);
    private static JCRStoreService jcrStoreService;
    public final static String PAGE_CONFIG_ATTR = "org.jahia.services.applications.pluto.pageconfig";
    public final String DEFAULT_PAGE_NAME = "Mashup manager";

    public static JCRStoreService getJcrStoreService() {
        if (jcrStoreService == null) {
            jcrStoreService = ServicesRegistry.getInstance().getJCRStoreService();
        }
        return jcrStoreService;
    }

    @Override
    public void addPage(PageConfig pageConfig) {
        logger.warn("addPage(...) not implemented.");
    }

    @Override
    /** This method is used by Pluto to deal with event.
     * Pluto get all pages and for each page gets all portlets and execute processEvent() if nested
     * In case of Jahia, there is a portlet instances are jcr node. Each of this portlet instance has to
     * be handled by pluto. So we create a "fake" pluto-page that has all porlet instances.
     **/
    public List getPages() {
        List<PageConfig> pageConfigList = new ArrayList<PageConfig>();
        pageConfigList.add(getDefaultPage());
        return pageConfigList;
    }


    @Override
    /**
     * Return a "fake" default page
     */
    public PageConfig getPage(String pageId) {
        return getDefaultPage();
    }

    @Override
    /**
     * Not implemented. Pluto can't remove pages
     */
    public void removePage(PageConfig pageConfig) {
        logger.warn("removePage(...) not implemented.");
    }

    @Override
    /**
     * Get a "fake" page that contains ALL mashups (portlet instances)
     */
    public PageConfig getDefaultPage() {
        ProcessingContext processingContext = Jahia.getThreadParamBean();
        Object pageConfigObj = processingContext.getAttribute(PAGE_CONFIG_ATTR);
        if (pageConfigObj == null) {
            PageConfig pageConfig = new PageConfig();
            pageConfig.setUri(PortalDriverServlet.DEFAULT_PAGE_URI);
            pageConfig.setName(DEFAULT_PAGE_NAME);
            try {
                Query q = createAllPortletsQuery();
                if (q != null) {
                    QueryResult qr = q.execute();
                    NodeIterator ni = qr.getNodes();
                    List<String> portletIds = new ArrayList<String>();
                    while (ni.hasNext()) {
                        JCRPortletNode nodeWrapper = new JCRPortletNode((JCRNodeWrapper) ni.nextNode());
                        portletIds.add(PortletWindowConfig.fromId(nodeWrapper));
                    }
                    pageConfig.setPortletIds(portletIds);

                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            processingContext.setAttribute(PAGE_CONFIG_ATTR, pageConfig);
            return pageConfig;
        }
        return (PageConfig) pageConfigObj;
    }


    /**
     * Create query that returns all portlet-instances depending on the connected user
     *
     * @return
     * @throws RepositoryException
     */
    private static Query createAllPortletsQuery() throws RepositoryException {
        String s = "//element(*, jnt:portlet)";
        QueryManager queryManager = getJcrStoreService().getQueryManager(Jahia.getThreadParamBean().getUser());
        return queryManager.createQuery(s, Query.XPATH);
    }


}
