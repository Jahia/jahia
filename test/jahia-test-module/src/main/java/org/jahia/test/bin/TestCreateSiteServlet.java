/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.test.bin;

import org.jahia.services.content.decorator.JCRSiteNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.settings.SettingsBean;
import org.jahia.test.TestHelper;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
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
public class TestCreateSiteServlet extends BaseTestController {

    private transient static Logger logger = LoggerFactory.getLogger(TestCreateSiteServlet.class);
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

    protected void handleGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

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
                logger.error(e.getMessage(), e);
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
                Iterator<JCRSiteNode> sites = ServicesRegistry.getInstance().getJahiaSitesService().getSitesNodeList().iterator();
                while (sites.hasNext()) {
                    JCRSiteNode siteToDelete = sites.next();
                    if (siteToDelete.getID() != 1) {
                        TestHelper.deleteSite(siteToDelete.getSiteKey());
                    }
                }
            } catch (Exception e) {
                logger.warn("Exception during test tearDown", e);
            }
        }


    }
}
