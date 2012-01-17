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

package org.jahia.bin;

import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.BasicSessionState;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ProcessingContextFactory;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.test.JahiaAdminUser;
import org.jahia.test.TestHelper;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;

/**
 * Servlet to create sites and publish them.
 * User: Dorth
 * Date: 9 sept. 2010
 * Time: 15:50:42
 */
@SuppressWarnings("serial")
public class TestCreateSiteServlet extends HttpServlet implements Controller, ServletContextAware {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(TestCreateSiteServlet.class);
    private int numberOfParents = 0;
    private int numberOfChilds = 0;
    private int numberOfSubChilds = 0;
    private String siteKey = null;

    public void setNumberOfParents(int numberOfParents) {
        this.numberOfParents = numberOfParents;
    }

    public void setNumberOfChilds(int numberOfChilds) {
        this.numberOfChilds = numberOfChilds;
    }

    public void setNumberOfSubChilds(int numberOfSubChilds) {
        this.numberOfSubChilds = numberOfSubChilds;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    private ServletContext servletContext;

    @SuppressWarnings("unchecked")
    protected void handleGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        final ProcessingContextFactory pcf = (ProcessingContextFactory) SpringContextSingleton.
                getInstance().getContext().getBean(ProcessingContextFactory.class.getName());
        ProcessingContext ctx = null;

        try {
            // should send response wrapper !
            ctx = pcf.getContext(httpServletRequest, httpServletResponse, servletContext);
        } catch (JahiaException e) {
            logger.error("Error while trying to build ProcessingContext", e);
            return;
        }

        try {
            ctx.setOperationMode(ParamBean.EDIT);
//            ctx.setEntryLoadRequest(new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, ctx.getLocales()));
            JahiaUser admin = JahiaAdminUser.getAdminUser(0);
            JCRSessionFactory.getInstance().setCurrentUser(admin);
            ctx.setTheUser(admin);
        } catch (JahiaException e) {
            logger.error("Error getting user", e);
            return;
        }

        if (httpServletRequest.getParameter("site").equals("ACME")) {
            if (httpServletRequest.getParameter("parents") != null) {
                this.setNumberOfParents(Integer.valueOf(httpServletRequest.getParameter("parents")));
            }
            if (httpServletRequest.getParameter("childs") != null) {
                this.setNumberOfChilds(Integer.valueOf(httpServletRequest.getParameter("childs")));
            }
            if (httpServletRequest.getParameter("subchilds") != null) {
                this.setNumberOfSubChilds(Integer.valueOf(httpServletRequest.getParameter("subchilds")));
            }
            if (httpServletRequest.getParameter("siteKey") != null) {
                this.setSiteKey(httpServletRequest.getParameter("siteKey"));
            }
            final JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        int numberOfSites = 0;
                        try {
                            numberOfSites = ServicesRegistry.getInstance().getJahiaSitesService().getNbSites();
                            if (siteKey == null) {
                                siteKey = "ACME" + numberOfSites;
                            }
                            TestHelper.createSite(siteKey, "localhost" + numberOfSites, TestHelper.WEB_BLUE_TEMPLATES,
                                    SettingsBean.getInstance().getJahiaVarDiskPath()
                                            + "/prepackagedSites/acme.zip", "ACME.zip");
                            JCRNodeWrapper homeNode = session.getRootNode().getNode("sites/" + siteKey + "/home");
                            if (numberOfParents != 0) {
                                for (int i = 0; i < numberOfParents; i++) {
                                    session.checkout(homeNode);
                                    homeNode.getNode("news").copy(homeNode, "parents" + i, true);
                                    session.save();
                                    if (numberOfChilds != 0) {
                                        for (int j = 0; j < numberOfChilds; j++) {
                                            session.checkout(homeNode);
                                            homeNode.getNode("news").copy(homeNode.getNode("parents" + i), "news" + j, true);
                                            session.save();
                                            if (numberOfSubChilds != 0) {
                                                for (int k = 0; k < numberOfSubChilds; k++) {
                                                    session.checkout(homeNode);
                                                    homeNode.getNode("news").copy(homeNode.getNode("parents" + i + "/news" + j), "subnews" + k, true);
                                                    session.save();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            jcrService.publishByMainId(session.getRootNode().getNode("sites/" + siteKey)
                                    .getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
                            session.save();
                            siteKey = null;

                        } catch (Exception e) {
                            logger.error("Cannot create site", e);
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (httpServletRequest.getParameter("site").equals("mySite")) {
            try {
                int numberOfSites = ServicesRegistry.getInstance().getJahiaSitesService().getNbSites();
                TestHelper.createSite("mySite" + numberOfSites, "localhost" + numberOfSites, TestHelper.INTRANET_TEMPLATES);
            } catch (Exception e) {
                logger.warn("Exception during mySite Creation", e);
            }
        } else if (httpServletRequest.getParameter("site").equals("delete")) {
            try {
                Iterator<JahiaSite> sites = ServicesRegistry.getInstance().getJahiaSitesService().getSites();
                while (sites.hasNext()) {
                    JahiaSite siteToDelete = sites.next();
                    if (siteToDelete.getID() != 1) {
                        TestHelper.deleteSite(siteToDelete.getSiteKey());
                    }
                }
            } catch (Exception e) {
                logger.warn("Exception during test tearDown", e);
            }
        }


    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getMethod().equalsIgnoreCase("get")) {
            handleGet(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return null;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
