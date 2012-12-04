package org.jahia.bundles.servlet.basic;

import org.jahia.bin.Jahia;
import org.json.JSONException;
import org.json.JSONWriter;
import org.osgi.framework.BundleContext;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Basic OSGi servlet example that outputs a JSON output of Jahia's version and copyright, as well as request info.
 * User: loom
 * Date: Oct 20, 2010
 * Time: 2:37:29 PM
 */
public class BasicServlet extends HttpServlet {

    protected BundleContext bundleContext;

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        JSONWriter json = new JSONWriter(out);
        try {
            json.array();
            json.object();
            json.key("version").value(Jahia.VERSION);
            json.key("copyright").value(Jahia.COPYRIGHT);
            json.endObject();
            json.object();
            json.key("contextPath").value(req.getContextPath());
            json.key("servletPath").value(req.getServletPath());
            json.key("pathInfo").value(req.getPathInfo());
            json.key("requestURI").value(req.getRequestURI());
            json.key("queryString").value(req.getQueryString());
            json.key("authType").value(req.getAuthType());
            json.key("httpMethod").value(req.getMethod());
            json.key("remoteAddr").value(req.getRemoteAddr());
            json.key("remoteHost").value(req.getRemoteHost());
            json.key("remotePort").value(req.getRemotePort());
            json.key("remoteUser").value(req.getRemoteUser());
            json.key("requestedSessionId").value(req.getRequestedSessionId());
            Cookie[] cookies = req.getCookies();
            if (cookies != null && cookies.length > 0) {
                json.key("cookies");
                json.array();
                for (int i=0; i < cookies.length; i++) {
                    Cookie cookie = cookies[i];
                    json.object();
                    json.key("name").value(cookie.getName());
                    json.key("path").value(cookie.getPath());
                    json.key("value").value(cookie.getValue());
                    json.key("domain").value(cookie.getDomain());
                    json.key("maxAge").value(cookie.getMaxAge());
                    json.key("version").value(cookie.getVersion());
                    json.key("secure").value(cookie.getSecure());
                    json.key("comment").value(cookie.getComment());
                    json.endObject();
                }
                json.endArray();
            }
            json.endObject();
            json.endArray();
        } catch (JSONException jsone) {
            throw new ServletException("JSON error", jsone);
        }
    }

}
