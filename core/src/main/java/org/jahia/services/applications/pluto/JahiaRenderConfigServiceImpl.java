/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.applications.pluto;

import org.apache.pluto.driver.PortalDriverServlet;
import org.apache.pluto.driver.services.impl.resource.RenderConfigServiceImpl;
import org.apache.pluto.driver.services.impl.resource.ResourceConfig;
import org.apache.pluto.driver.services.portal.PageConfig;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRPortletNode;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jahia
 * Date: 8 avr. 2009
 * Time: 16:12:17
 */
public class JahiaRenderConfigServiceImpl extends RenderConfigServiceImpl {

    // todo actually we never clear this threadlocal. We should probably remove it at some point and replace it
    // with a more "static" configuration that is updated using a JCR listener for portlet object creation/delete.
    ThreadLocal<PageConfig> pageConfigThreadLocal = new ThreadLocal<PageConfig>();

    public JahiaRenderConfigServiceImpl(ResourceConfig config) {
        super(config);
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaRenderConfigServiceImpl.class);
    private JCRSessionFactory sessionFactory;
    public final static String PAGE_CONFIG_ATTR = "org.jahia.services.applications.pluto.pageconfig";
    public final String DEFAULT_PAGE_NAME = "Portlet manager";

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
    public List<PageConfig> getPages() {
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
     * Get a "fake" page that contains ALL portlets (portlet instances)
     */
    public PageConfig getDefaultPage() {
        PageConfig pageConfig = pageConfigThreadLocal.get();
        if ((pageConfig == null) || (pageConfig.getPortletIds().isEmpty())) {
            try {
                pageConfig = JCRTemplate.getInstance().doExecuteWithSystemSessionInSameWorkspaceAndLocale(new JCRCallback<PageConfig>() {
                    public PageConfig doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        PageConfig pageConfig = new PageConfig();
                        pageConfig.setUri(PortalDriverServlet.DEFAULT_PAGE_URI);
                        pageConfig.setName(DEFAULT_PAGE_NAME);
                        Query q = createAllPortletsQuery(session);
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
                        return pageConfig;
                    }
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            if (pageConfig.getPortletIds().isEmpty()) {
                pageConfigThreadLocal.set(null);
            } else {
                pageConfigThreadLocal.set(pageConfig);
            }
            return pageConfig;
        }
        return pageConfig;
    }


    /**
     * Create query that returns all portlet-instances depending on the connected user
     *
     * @return a Query
     * @throws RepositoryException in case of error
     */
    private Query createAllPortletsQuery(JCRSessionWrapper session) throws RepositoryException {
        String s = "select * from [jnt:portlet]";
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        return queryManager.createQuery(s, Query.JCR_SQL2);
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
