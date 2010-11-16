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
import org.jahia.utils.LanguageCodeConverters;
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

/**
 * Created by IntelliJ IDEA.
 * User: Dorth
 * Date: 9 sept. 2010
 * Time: 15:50:42
 * 
 */
@SuppressWarnings("serial")
public class TestCreateSiteServlet extends HttpServlet implements Controller, ServletContextAware {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(TestCreateSiteServlet.class);

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
            ctx = pcf.getContext(new BasicSessionState("123"));
        }

        try {
            ctx.setOperationMode(ParamBean.EDIT);
//            ctx.setEntryLoadRequest(new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, ctx.getLocales()));
            JahiaUser admin = JahiaAdminUser.getAdminUser(0);
            JCRSessionFactory.getInstance().setCurrentUser(admin);
            ctx.setTheUser(admin);
        } catch (JahiaException e) {
            logger.error("Error getting user", e);
        }

        if (httpServletRequest.getParameter("site").equals("ACME")) {

            final JahiaSite defaultSite = ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite();
            JCRNodeWrapper homeNode = null;
            if (defaultSite != null) {
                try {
                    JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                            LanguageCodeConverters.languageCodeToLocale("en"));
                    homeNode = session.getRootNode().getNode("sites/" + defaultSite.getSiteKey() + "/home");
                } catch (Exception e) {
                }
            }
            if (homeNode == null) {
                final JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
                try {
                    JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            try {
                                TestHelper.createSite("ACME", "localhost", TestHelper.ACME_TEMPLATES,
                                        SettingsBean.getInstance().getJahiaVarDiskPath()
                                                + "/prepackagedSites/webtemplates65.zip", "ACME.zip");
                                jcrService.publishByMainId(session.getRootNode().getNode("sites/ACME/home")
                                        .getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
                                session.save();
                            } catch (Exception e) {
                                logger.error("Cannot create or publish site", e);
                            }
                            return null;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (httpServletRequest.getParameter("site").equals("mySite")) {
            try {
                final JahiaSite mySite = ServicesRegistry.getInstance().getJahiaSitesService().getSite("localhosttest");
                if(mySite == null){
                    TestHelper.createSite("mySite", "localhosttest" , TestHelper.INTRANET_TEMPLATES);
                }else logger.warn("site already exist");
            } catch (Exception e) {
                logger.warn("Exception during mySite Creation", e);
            }
        } else if (httpServletRequest.getParameter("site").equals("delete")) {
            try {
                TestHelper.deleteSite("ACME");
                TestHelper.deleteSite("mySite");
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
