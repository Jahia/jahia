package org.jahia.bin;

import org.jahia.api.Constants;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContextFactory;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 26, 2010
 * Time: 5:49:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class WelcomeServlet extends HttpServlet {

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        defaultRedirect(request, response, getServletContext());
    }

    public static void defaultRedirect(HttpServletRequest request, HttpServletResponse response, ServletContext context) throws IOException, ServletException {
        try {
        JahiaSite site = JahiaSitesBaseService.getInstance().getDefaultSite();
        if (site == null) {
                response.sendRedirect(request.getContextPath() + "/administration");
            } else {

                final ProcessingContextFactory pcf = (ProcessingContextFactory) SpringContextSingleton.
                        getInstance().getContext().getBean(ProcessingContextFactory.class.getName());

                ParamBean jParams = pcf.getContext(request, response, context);

            
                String base;

                final String jcrPath = "/sites/" + site.getSiteKey() + "/home";

                try {
                    JCRStoreService.getInstance().getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE)
                            .getNode(jcrPath);
                    base = jParams.getRequest().getContextPath() + Render.getRenderServletPath() + "/" +
                            Constants.LIVE_WORKSPACE + "/" + jParams.getLocale();
                } catch (PathNotFoundException e) {
                    try {
                        JCRStoreService.getInstance().getSessionFactory().getCurrentUserSession().getNode(jcrPath);
                        base = jParams.getRequest().getContextPath() + Edit.getEditServletPath() + "/" +
                                Constants.EDIT_WORKSPACE + "/" + jParams.getLocale();
                    } catch (PathNotFoundException e2) {
                        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                session.getNode(jcrPath);
                                throw new AccessDeniedException();
                            }
                        });
                        throw new AccessDeniedException();
                    }
                }

                response.sendRedirect(base + jcrPath + ".html");

            }
        } catch (Exception e) {
            List<ErrorHandler> handlers = ServicesRegistry.getInstance()
                    .getJahiaTemplateManagerService().getErrorHandler();
            for (ErrorHandler handler : handlers) {
                if (handler.handle(e, request, response)) {
                    return;
                }
            }
            DefaultErrorHandler.getInstance().handle(e, request, response);
        }

    }
}