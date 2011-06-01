package org.jahia.bin;

import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.RequestLoadAverage;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;

/**
 * Simple servlet to expose server statistics in JSON.
 */
public class ServerStats implements Controller {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ServerStats.class);

    private RequestLoadAverage requestLoadAverage;

    public void setRequestLoadAverage(RequestLoadAverage requestLoadAverage) {
        this.requestLoadAverage = requestLoadAverage;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            if (request.getMethod().equals("GET") || request.getMethod().equals("POST")) {
                handle(request, response);
            } else if (request.getMethod().equals("OPTIONS")) {
                response.setHeader("Allow", "GET, OPTIONS, POST");
            } else {
                response.sendError(SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            DefaultErrorHandler.getInstance().handle(e, request, response);
        } finally {
            if (logger.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("Rendered [").append(request.getRequestURI());
                JahiaUser user = JCRTemplate.getInstance().getSessionFactory().getCurrentUser();
                if (user != null) {
                    sb.append("] user=[").append(user.getUsername());
                }
                sb.append("] ip=[").append(request.getRemoteAddr()).append("] sessionID=[").append(
                        request.getSession(true).getId()).append("] in [").append(
                        System.currentTimeMillis() - startTime).append("ms]");
                logger.info(sb.toString());
            }
        }
        return null;
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response) throws RenderException,
            IOException, RepositoryException, JSONException {

        response.setContentType("application/json; charset=UTF-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("currentRequestCount", JahiaContextLoaderListener.getRequestCount());
        jsonObject.put("currentSessionCount", JahiaContextLoaderListener.getSessionCount());
        jsonObject.put("oneMinuteRequestLoadAverage", requestLoadAverage.getOneMinuteLoad());
        jsonObject.put("fiveMinuteRequestLoadAverage", requestLoadAverage.getFiveMinuteLoad());
        jsonObject.put("fifteenMinuteRequestLoadAverage", requestLoadAverage.getFifteenMinuteLoad());
        jsonObject.write(response.getWriter());
    }
}
