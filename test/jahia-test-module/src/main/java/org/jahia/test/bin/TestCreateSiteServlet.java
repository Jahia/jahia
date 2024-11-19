/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
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
                                    "prepackagedSites/acme.zip", "ACME.zip");
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
                TestHelper.createSite("mySite" + numberOfSites, "localhost" + numberOfSites, TestHelper.WEB_TEMPLATES);
            } catch (Exception e) {
                logger.warn("Exception during mySite Creation", e);
            }
        } else if (httpServletRequest.getParameter("site").equals("delete")) {
            try {
                Iterator<JCRSiteNode> sites = ServicesRegistry.getInstance().getJahiaSitesService().getSitesNodeList().iterator();
                while (sites.hasNext()) {
                    JCRSiteNode siteToDelete = sites.next();
                    TestHelper.deleteSite(siteToDelete.getSiteKey());
                }
            } catch (Exception e) {
                logger.warn("Exception during test tearDown", e);
            }
        }


    }
}
