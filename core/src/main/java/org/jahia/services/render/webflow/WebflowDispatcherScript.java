package org.jahia.services.render.webflow;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.render.View;
import org.jahia.services.render.scripting.RequestDispatcherScript;
import org.jahia.utils.StringResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class WebflowDispatcherScript extends RequestDispatcherScript {
    private static final Logger logger = LoggerFactory.getLogger(WebflowDispatcherScript.class);

    private String flowPath;

    /**
     * Builds the script object
     */
    public WebflowDispatcherScript(View view) {
        super(view);
    }

    /**
     * Execute the script and return the result as a string
     *
     * @param resource resource to display
     * @param context
     * @return the rendered resource
     * @throws org.jahia.services.render.RenderException
     *
     */
    public String execute(Resource resource, RenderContext context) throws RenderException {
        View view = getView();
        if (view == null) {
            throw new RenderException("View not found for : " + resource);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("View '" + view + "' resolved for resource: " + resource);
            }
        }

        HttpServletRequest request = context.getRequest();
        HttpServletResponse response = context.getResponse();

        flowPath = view.getPath().replace("/modules/", "/flow/");

        String identifier;
        try {
            identifier = resource.getNode().getIdentifier();
        } catch (RepositoryException e) {
            throw new RenderException(e);
        }

        RequestDispatcher rd = request.getRequestDispatcher(flowPath);

        Object oldModule = request.getAttribute("currentModule");
        Object oldModuleVersion = request.getAttribute("currentModuleVersion");
        request.setAttribute("currentModule", view.getModule());
        request.setAttribute("currentModuleVersion", view.getModuleVersion());

        if (logger.isDebugEnabled()) {
            dumpRequestAttributes(request);
        }

        StringResponseWrapper responseWrapper = new StringResponseWrapper(response);

        try {
            rd.include(request, responseWrapper);

            while (responseWrapper.getRedirect() != null) {
                String qs = StringUtils.substringAfter(responseWrapper.getRedirect(), "?");
                final Map<String,String[]> params = new HashMap<String,String[]>();
                if (!StringUtils.isEmpty(qs)) {
                    params.put("webflow-execution-"+ identifier,new String[] {StringUtils.substringAfterLast(qs, "webflow-execution-" + identifier+"=")});
                }
                HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
                    @Override
                    public String getMethod() {
                        return "GET";
                    }

                    @Override
                    public Map getParameterMap() {
                        return params;
                    }

                    @Override
                    public String getParameter(String name) {
                        return params.containsKey(name) ? params.get(name) [0] : null;
                    }

                    @Override
                    public Enumeration getParameterNames() {
                        return new Vector(params.keySet()).elements();
                    }

                    @Override
                    public String[] getParameterValues(String name) {
                        return params.get(name);
                    }
                };

                rd = requestWrapper.getRequestDispatcher(flowPath + "?" + qs);
                responseWrapper = new StringResponseWrapper(response);
                rd.include(requestWrapper, responseWrapper);
            }
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

    public String getFlowPath() {
        return flowPath;
    }
}
