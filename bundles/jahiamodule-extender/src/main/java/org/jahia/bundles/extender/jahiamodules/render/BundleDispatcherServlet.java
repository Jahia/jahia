package org.jahia.bundles.extender.jahiamodules.render;

import org.osgi.framework.BundleContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet used to internally dispatch to OSGi dispatcher.
 */
public class BundleDispatcherServlet extends HttpServlet {
    private Map<String, org.ops4j.pax.web.jsp.JspServletWrapper> jspMappings = new HashMap<String, org.ops4j.pax.web.jsp.JspServletWrapper>();

    @Override
    public void init(ServletConfig config)
        throws ServletException
    {
        super.init(config);

    }

    public Map<String, org.ops4j.pax.web.jsp.JspServletWrapper> getJspMappings() {
        return jspMappings;
    }

    public boolean canServePath(String path) {
        for (String mapping : jspMappings.keySet()) {
            if (path.startsWith(mapping)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        for (String mapping : jspMappings.keySet()) {
            if (req.getRequestURI().startsWith(mapping)) {
                jspMappings.get(mapping).service(req, res);
                return;
            }
        }
    }

    @Override
    public void destroy()
    {
        jspMappings.clear();
        super.destroy();
    }

    private BundleContext getBundleContext()
        throws ServletException
    {
        Object context = getServletContext().getAttribute(BundleContext.class.getName());
        if (context instanceof BundleContext) {
            return (BundleContext)context;
        }

        throw new ServletException("Bundle context attribute [" + BundleContext.class.getName() +
                "] not set in servlet context");
    }

}
