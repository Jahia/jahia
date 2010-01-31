package org.jahia.bin;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.context.ServletConfigAware;
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.data.JahiaData;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 17, 2009
 * Time: 1:47:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class Login extends HttpServlet implements Controller {
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
        // Login done by parambean and auth-valve
        ProcessingContext jParams = Jahia.createParamBean(request, response, request.getSession());

        JahiaData jData = new JahiaData(jParams, false);
        jParams.setAttribute(JahiaData.JAHIA_DATA, jData);

        String redirectActiveStr = request.getParameter("redirectActive");
        boolean redirectActive = true;
        if (redirectActiveStr != null) {
            redirectActive = Boolean.parseBoolean(redirectActiveStr);    
        }

        String redirect = request.getParameter("redirect");
        if (redirect == null || redirect.length() == 0) {
            redirect = request.getContextPath()+"/cms/render/default/"+ jParams.getLocale() +"/sites/" +
                    JahiaSitesBaseService.getInstance().getDefaultSite().getSiteKey() + "/home.html";
        }

        String result = (String) jParams.getAttribute(LoginEngineAuthValveImpl.VALVE_RESULT);
        if ("ok".equals(result)) {
            if (redirectActive) {
                response.sendRedirect(redirect);
            }
        } else {
            request.setAttribute("javax.servlet.error.request_uri", redirect);
            request.getRequestDispatcher("/errors/error_401.jsp").forward(request, response);
        }
        return null;
    }
}
