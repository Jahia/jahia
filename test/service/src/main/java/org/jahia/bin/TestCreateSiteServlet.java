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
 * Created by IntelliJ IDEA.
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
                            JCRNodeWrapper homeNode = session.getRootNode().getNode("sites/ACME" + (numberOfSites) + "/home");
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
                            jcrService.publishByMainId(session.getRootNode().getNode("sites/ACME" + (numberOfSites))
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
