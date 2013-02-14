package org.jahia.bundles.extender.jahiamodules;

import org.apache.jasper.Constants;
import org.apache.jasper.servlet.JspServlet;
import org.ops4j.pax.swissbox.core.ContextClassLoaderUtils;

import javax.servlet.*;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;

/**
 * A wrapper for the JspServlet class from Pax Web, but that will actually allow sharing of the JspServlet and
 * class loader instances.
 */
public class JspServletWrapper implements Servlet {

    private final JspServlet jspServlet;
    private final URLClassLoader urlClassLoader;
    private final String jspFile;
    private boolean firstInstance;

    public JspServletWrapper(JspServlet jspServlet, URLClassLoader urlClassLoader, String jspFile, boolean firstInstance) {
        this.jspServlet = jspServlet;
        this.urlClassLoader = urlClassLoader;
        this.jspFile = jspFile;
        this.firstInstance = firstInstance;
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        if (firstInstance) {
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
        if (jspFile != null)
            req.setAttribute(Constants.JSP_FILE, jspFile);

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
