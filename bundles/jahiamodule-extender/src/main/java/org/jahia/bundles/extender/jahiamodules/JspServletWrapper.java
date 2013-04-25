package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.lang.StringUtils;
import org.apache.jasper.Constants;
import org.apache.jasper.servlet.JspServlet;
import org.ops4j.pax.swissbox.core.ContextClassLoaderUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * A wrapper for the JspServlet class from Pax Web, but that will actually allow sharing of the JspServlet and
 * class loader instances.
 */
public class JspServletWrapper implements Servlet {

    private final JspServlet jspServlet;
    private final URLClassLoader urlClassLoader;
    private final String jspFile;
    private boolean performInit;
    private String jspFilePrefix;

    public JspServletWrapper(JspServlet jspServlet, URLClassLoader urlClassLoader, String jspFile, String jspFilePrefix, boolean performInit) {
        this.jspServlet = jspServlet;
        this.urlClassLoader = urlClassLoader;
        this.jspFile = jspFile;
        this.jspFilePrefix = jspFilePrefix;
        this.performInit = performInit;
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        if (performInit) {
            try {
                ContextClassLoaderUtils.doWithClassLoader(urlClassLoader,
                        new Callable<Void>() {
                            public Void call()
                                    throws Exception {
                                jspServlet.init(config);
                                return null;
                            }
                        }
                );
            } catch (Exception e) {
                throw new ServletException("Error during servlet init", e);
            }
        }
    }

    @Override
    public ServletConfig getServletConfig() {
        return jspServlet.getServletConfig();
    }

    @Override
    public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException {
        String jspPath = getJspFilePath(req);
        if (jspPath != null) {
            req.setAttribute(Constants.JSP_FILE, jspPath);
        }

        Object currentLocaleResolver = req.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
        req.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new LocaleResolver() {

            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                HttpSession session = request.getSession();
                if (session != null) {
                    Locale currentLocale = (Locale) session.getAttribute(org.jahia.api.Constants.SESSION_LOCALE);
                    if (currentLocale != null) {
                        return currentLocale;
                    }
                }
                return request.getLocale();
            }

            @Override
            public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
            }
        });
        try {
            ContextClassLoaderUtils.doWithClassLoader(urlClassLoader,
                    new Callable<Void>() {
                        public Void call()
                                throws Exception {
                            jspServlet.service(req, res);
                            return null;
                        }
                    }
            );
        } catch (Exception e) {
            throw new ServletException("Error during servlet servicing", e);
        }
        req.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, currentLocaleResolver);
    }

    private String getJspFilePath(final ServletRequest req) {
        String jspPath = jspFile;
        if (jspPath == null && jspFilePrefix != null && req instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) req;
            String servletPath = StringUtils.defaultIfEmpty(
                    (String) httpRequest.getAttribute("javax.servlet.include.servlet_path"),
                    httpRequest.getServletPath());
            String pathInfo = StringUtils.defaultIfEmpty(
                    (String) httpRequest.getAttribute("javax.servlet.include.path_info"), httpRequest.getPathInfo());
            if (pathInfo != null && servletPath != null && "/modules".equals(servletPath)
                    && pathInfo.startsWith(jspFilePrefix) && pathInfo.length() > jspFilePrefix.length()) {
                jspPath = pathInfo.substring(jspFilePrefix.length());
            }
        }
        return jspPath;
    }

    @Override
    public String getServletInfo() {
        return jspServlet.getServletInfo();
    }

    @Override
    public void destroy() {
        // we never destroy the JSP servlet here, we do it at a higher level.
    }
}
