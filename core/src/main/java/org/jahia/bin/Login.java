package org.jahia.bin;

import org.jahia.services.sites.JahiaSite;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.services.sites.JahiaSitesBaseService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Login action controller.
 * User: toto
 * Date: Nov 17, 2009
 * Time: 1:47:38 PM
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

        boolean restMode = Boolean.valueOf(request.getParameter("restMode"));
        boolean redirectActive = !restMode;
        if (redirectActive) {
            String redirectActiveStr = request.getParameter("redirectActive");
            if (redirectActiveStr != null) {
                redirectActive = Boolean.parseBoolean(redirectActiveStr);    
            }
        }

        String redirect = null;
        if (redirectActive) {
            redirect = request.getParameter("redirect");
            if (redirect == null || redirect.length() == 0) {
                final JahiaSite site = JahiaSitesBaseService.getInstance().getDefaultSite();
                redirect = request.getContextPath()+"/cms/render/default/"+ site.getDefaultLanguage() +"/sites/" +
                        site.getSiteKey() + "/home.html";
            }
        }

        String result = (String) request.getAttribute(LoginEngineAuthValveImpl.VALVE_RESULT);
        if ("ok".equals(result)) {
            if (redirectActive) {
                response.sendRedirect(redirect);
            } else {
                response.getWriter().append("OK");
            }
        } else {
            if (!restMode) {
                request.setAttribute("javax.servlet.error.request_uri", redirect);
                request.getRequestDispatcher("/errors/error_401.jsp").forward(request, response);
            } else {
                response.getWriter().append("unauthorized");
            }
        }
        return null;
    }
}
