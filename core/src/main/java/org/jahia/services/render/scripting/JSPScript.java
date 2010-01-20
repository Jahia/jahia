/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.scripting;

import org.apache.log4j.Logger;
import org.jahia.services.render.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

/**
 * This class uses the standard request dispatcher to execute a JSP script.
 *
 * @author toto
 */
public class JSPScript implements Script {

    private static final Logger logger = Logger.getLogger(JSPScript.class);

    private static final String JSP_FILE_EXTENSION = "jsp";

    private RequestDispatcher rd;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private Template template;

    /**
     * Builds the script object
     *
     */
    public JSPScript(Template template) {
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
