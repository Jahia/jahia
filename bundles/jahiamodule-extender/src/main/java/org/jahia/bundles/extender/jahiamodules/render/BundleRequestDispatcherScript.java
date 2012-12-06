package org.jahia.bundles.extender.jahiamodules.render;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.render.View;
import org.jahia.services.render.scripting.Script;
import org.jahia.utils.StringResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 */
public class BundleRequestDispatcherScript implements Script {

    private static final Logger logger = LoggerFactory.getLogger(BundleRequestDispatcherScript.class);

    private BundleDispatcherServlet bundleDispatcherServlet;

    private BundleView view;

    class BundleRequestDispatcher implements RequestDispatcher {

        private String path;
        private String queryString;

        RequestDispatcher parentRequestDispatcher;

        public BundleRequestDispatcher(String path, RequestDispatcher parentRequestDispatcher) {
            int queryPos = path.indexOf("?");
            if (queryPos > -1) {
                this.path = path.substring(0, queryPos);
                this.queryString = path.substring(queryPos+1);
            } else {
                this.path = path;
                this.queryString = null;
            }
            this.parentRequestDispatcher = parentRequestDispatcher;
        }

        @Override
        public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            parentRequestDispatcher.forward(request, response);
        }

        @Override
        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            if (bundleDispatcherServlet.canServePath(path)) {
                // First we save the old request attributes to restore them after the request
                Object oldLocalizationContext = request.getAttribute("javax.servlet.jsp.jstl.fmt.localizationContext.request");
                try {
                    Locale locale = (Locale) oldLocalizationContext.getClass().getMethod("getLocale").invoke(oldLocalizationContext);
                    ResourceBundle oldBundle = (ResourceBundle) oldLocalizationContext.getClass().getMethod("getResourceBundle").invoke(oldLocalizationContext);
                    request.setAttribute("javax.servlet.jsp.jstl.fmt.localizationContext.request", new LocalizationContext(oldBundle,locale));
                } catch (IllegalAccessException e) {
                    logger.error("Error setting localization context", e);
                } catch (InvocationTargetException e) {
                    logger.error("Error setting localization context", e);
                } catch (NoSuchMethodException e) {
                    logger.error("Error setting localization context", e);
                }
                Object oldRequestUri = request.getAttribute("javax.servlet.include.request_uri");
                Object oldContextPath = request.getAttribute("javax.servlet.include.context_path");
                Object oldServletPath = request.getAttribute("javax.servlet.include.servlet_path");
                Object oldPathInfo = request.getAttribute("javax.servlet.include.path_info");
                Object oldQueryString = request.getAttribute("javax.servlet.include.query_string");
                // Now let's set the new attribute values
                request.setAttribute("javax.servlet.include.request_uri", oldContextPath + path);
                request.setAttribute("javax.servlet.include.servlet_path", path);
                request.setAttribute("javax.servlet.include.path_info", null);
                request.setAttribute("javax.servlet.include.query_string", queryString);

                bundleDispatcherServlet.service(request, response);
                // Restore the old attribute values
                request.setAttribute("javax.servlet.include.request_uri", oldRequestUri);
                request.setAttribute("javax.servlet.include.context_path", oldContextPath);
                request.setAttribute("javax.servlet.include.servlet_path", oldServletPath);
                request.setAttribute("javax.servlet.include.path_info", oldPathInfo);
                request.setAttribute("javax.servlet.include.query_string", oldQueryString);
                request.setAttribute("javax.servlet.jsp.jstl.fmt.localizationContext.request", oldLocalizationContext);
            } else {
                parentRequestDispatcher.include(request, response);
            }
        }
    }

    protected static void dumpRequestAttributes(HttpServletRequest request) {
        // Let's enumerate request attribute to see what we are exposing.
        @SuppressWarnings("rawtypes")
        Enumeration attributeNamesEnum = request.getAttributeNames();
        while (attributeNamesEnum.hasMoreElements()) {
            String currentAttributeName = (String) attributeNamesEnum.nextElement();
            String currentAttributeValue = request.getAttribute(currentAttributeName).toString();
            if (currentAttributeValue.length() < 80) {
                logger.debug("Request attribute " + currentAttributeName + "=" + currentAttributeValue);
            } else {
                logger.debug("Request attribute " + currentAttributeName + "=" + currentAttributeValue.substring(0,
                        80) + " (first 80 chars)");
            }
        }
    }


    public BundleRequestDispatcherScript(BundleView view, BundleDispatcherServlet bundleDispatcherServlet) {
        this.view = view;
        this.bundleDispatcherServlet = bundleDispatcherServlet;
    }

    @Override
    public String execute(Resource resource, RenderContext context) throws RenderException {
        if (view == null) {
            throw new RenderException("View not found for : " + resource);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("View '" + view + "' resolved for resource: " + resource);
            }
        }

        HttpServletRequest request = context.getRequest();
        HttpServletResponse response = context.getResponse();

        Object oldModule = request.getAttribute("currentModule");
        Object oldModuleVersion = request.getAttribute("currentModuleVersion");
        request.setAttribute("currentModule", view.getModule());
        request.setAttribute("currentModuleVersion", view.getModuleVersion());

        if (logger.isDebugEnabled()) {
            dumpRequestAttributes(request);
        }

        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
            @Override
            public String getPathInfo() {
                return null;
            }

            @Override
            public String getQueryString() {
                return null;
            }

            @Override
            public String getRequestURI() {
                return getContextPath() + view.getPath();    //To change body of overridden methods use File | Settings | File Templates.
            }

            @Override
            public StringBuffer getRequestURL() {
                return new StringBuffer(getRequestURI());
            }

            @Override
            public String getServletPath() {
                return view.getPath();    //To change body of overridden methods use File | Settings | File Templates.
            }

            @Override
            public RequestDispatcher getRequestDispatcher(String path) {
                return new BundleRequestDispatcher(path, super.getRequestDispatcher(path));
            }
        };
        StringResponseWrapper responseWrapper = new StringResponseWrapper(response);
        try {
            // @todo can we find a more reliable (and portable) way of dispatching to a JSP ?
            requestWrapper.getRequestDispatcher(view.getPath()).include(requestWrapper, responseWrapper);
        } catch (ServletException e) {
            throw new RenderException(e.getRootCause() != null ? e.getRootCause() : e);
        } catch (IOException e) {
            throw new RenderException(e);
        } finally {
            request.setAttribute("currentModule", oldModule);
            request.setAttribute("currentModuleVersion", oldModuleVersion);
        }
        try {
            return responseWrapper.getString();
        } catch (IOException e) {
            throw new RenderException(e);
        }
    }

    @Override
    public View getView() {
        return view;
    }
}
