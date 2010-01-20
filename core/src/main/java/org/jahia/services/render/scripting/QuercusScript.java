package org.jahia.services.render.scripting;

import org.apache.log4j.Logger;
import org.jahia.services.render.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Quercus specific script, since the JSR223 bridge doesn't give us enough flexibility.
 *
 * @author loom
 *         Date: Jan 19, 2010
 *         Time: 11:23:42 AM
 */
public class QuercusScript implements Script {

    private static final Logger logger = Logger.getLogger(QuercusScript.class);

    private static final String PHP_FILE_EXTENSION = "php";

    private RequestDispatcher rd;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private Template template;

    /**
     * Builds the script object
     *
     */
    public QuercusScript(Template template) {
        this.template = template;
    }

    /**
     * Execute the script and return the result as a string
     *
     * @param resource resource to display
     * @param context
     * @return the rendered resource
     * @throws org.jahia.services.render.RenderException
     */
    public String execute(Resource resource, RenderContext context) throws RenderException {
        if (template == null) {
            throw new RenderException("Template not found for : " + resource);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Template '" + template + "' resolved for resource: " + resource);
            }
        }

        this.request = context.getRequest();
        this.response = context.getResponse();
        rd = request.getRequestDispatcher(template.getPath());

        final boolean[] isWriter = new boolean[1];
        final StringWriter stringWriter = new StringWriter();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

        Object oldModule = request.getAttribute("currentModule");
        request.setAttribute("currentModule",template.getModule());
        try {
            rd.include(request, new HttpServletResponseWrapper(response) {
                @Override
                public ServletOutputStream getOutputStream() throws IOException {
                    return new ServletOutputStream() {
                        @Override
                        public void write(int i) throws IOException {
                            outputStream.write(i);
                        }
                    };
                }

                public PrintWriter getWriter() throws IOException {
                    isWriter[0] = true;
                    return new PrintWriter(stringWriter);
                }
            });
        } catch (ServletException e) {
            throw new RenderException(e.getRootCause() != null ? e.getRootCause() : e);
        } catch (IOException e) {
            throw new RenderException(e);
        } finally {
            request.setAttribute("currentModule",oldModule);
        }
        if (isWriter[0]) {
            return stringWriter.getBuffer().toString();
        } else {
            try {
                String s = outputStream.toString("UTF-8");
                return s;
            } catch (IOException e) {
                throw new RenderException(e);
            }
        }

    }

    /**
     * Provides access to the template associated with this script
     *
     * @return the Template instance that will be executed
     */
    public Template getTemplate() {
        return template;
    }
}
