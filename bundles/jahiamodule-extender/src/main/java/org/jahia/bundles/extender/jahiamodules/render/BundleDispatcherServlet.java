package org.jahia.bundles.extender.jahiamodules.render;

import org.jahia.bundles.extender.jahiamodules.JspServletWrapper;

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
    private static final long serialVersionUID = 2919659271526081659L;
    private Map<String, JspServletWrapper> jspMappings = new HashMap<String, JspServletWrapper>();

    @Override
    public void init(ServletConfig config)
        throws ServletException
    {
        super.init(config);

    }

    public Map<String, JspServletWrapper> getJspMappings() {
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
            if (req.getServletPath().startsWith(mapping)) {
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

}
