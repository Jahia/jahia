package org.jahia.bin;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.context.ServletConfigAware;
import org.jahia.data.events.JahiaEvent;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.params.ParamBean;
import org.jahia.params.valves.CookieAuthConfig;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 17, 2009
 * Time: 1:47:45 PM
 * To change this template use File | Settings | File Templates.
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
        ParamBean jParams = Jahia.createParamBean(request, response, request.getSession());

        // now let's destroy the cookie authentication if there was one
        // set for this user.
        JahiaUser curUser = jParams.getUser();
        String cookieAuthKey = curUser.getProperty(cookieAuthConfig.getUserPropertyName());
        Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieAuthKey);
        authCookie.setPath(jParams.getContextPath());
        authCookie.setMaxAge(0); // means we want it deleted now !
        jParams.getRealResponse().addCookie(authCookie);
        curUser.removeProperty(cookieAuthConfig.getUserPropertyName());

        jParams.setUserGuest();

        String redirect = request.getParameter("redirect");
        if (redirect == null) {
            redirect = request.getHeader("referer");
        }
        response.sendRedirect(redirect);
        return null;
    }
}
