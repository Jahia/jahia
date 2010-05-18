package org.jahia.bin;

import org.apache.commons.lang.StringUtils;
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.CookieAuthConfig;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.*;

/**
 * Logout controller.
 * User: toto
 * Date: Nov 17, 2009
 * Time: 1:47:45 PM
 */
public class Logout extends HttpServlet implements Controller {

    private CookieAuthConfig cookieAuthConfig;

    public CookieAuthConfig getCookieAuthConfig() {
        return cookieAuthConfig;
    }

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }

    /**
     * Process the request and return a ModelAndView object which the DispatcherServlet
     * will render. A <code>null</code> return value is not an error: It indicates that
     * this object completed request processing itself, thus there is no ModelAndView
     * to render.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render, or <code>null</code> if handled directly
     * @throws Exception in case of errors
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // now let's destroy the cookie authentication if there was one
        // set for this user.
        JahiaUser curUser = JCRSessionFactory.getInstance().getCurrentUser();
        String cookieAuthKey = curUser.getProperty(cookieAuthConfig.getUserPropertyName());
        if (cookieAuthKey != null) {
            Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieAuthKey);
            authCookie.setPath(StringUtils.isNotEmpty(request.getContextPath()) ? request.getContextPath() : "/");
            authCookie.setMaxAge(0); // means we want it deleted now !
            response.addCookie(authCookie);
            curUser.removeProperty(cookieAuthConfig.getUserPropertyName());
        }

        final JahiaUserManagerService userMgr = ServicesRegistry.getInstance().getJahiaUserManagerService();
        final JahiaUser guest = userMgr.lookupUser(JahiaUserManagerService.GUEST_USERNAME);
        JCRSessionFactory.getInstance().setCurrentUser(guest);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(ProcessingContext.SESSION_USER);            
        }

        String redirectActiveStr = request.getParameter("redirectActive");
        boolean redirectActive = true;
        if (redirectActiveStr != null) {
            redirectActive = Boolean.parseBoolean(redirectActiveStr);
        }

        String redirect = request.getParameter("redirect");
        if (redirect == null) {
            redirect = request.getHeader("referer");
        }

        if (redirectActive) {
            response.sendRedirect(redirect);
        }
        return null;
    }

    public static String getLogoutServletPath() {
        // TODO move this into configuration
        return "/cms/logout";
    }

}
