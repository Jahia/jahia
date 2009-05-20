/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.commons.server;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 26 nov. 2007
 * Time: 15:30:53
 * To change this template use File | Settings | File Templates.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * Cette classe encapsule une HttpServletResponse avec un buffered output.
 * You can use this to forward or include a Servlet or JSP page
 * and capture the output from it.
 * <p/>
 * Use getOutput to get the output which was written to the response.
 * Only buffers the Writer. Not the OutputStream !!
 */
public class BufferedHttpResponseWrapper extends HttpServletResponseWrapper {

    private PrintWriter writer = null;
    private ByteArrayOutputStream byteArrayOutputStream = null;

    /**
     * Constructor for BufferedHttpResponseWrapper.
     * Create a new buffered Writer
     *
     * @param response The response object to wrap
     */
    public BufferedHttpResponseWrapper(HttpServletResponse response) {
        super(response);
        byteArrayOutputStream = new ByteArrayOutputStream();
        writer = new PrintWriter(byteArrayOutputStream);
    }

    /**
     * Return the buffered Writer
     *
     * @see javax.servlet.ServletResponse#getWriter()
     */
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    /**
     * Return the output written to the Writer.
     * To get the output, the Writer must be flushed and closed.
     * The content is captured by the ByteArrayOutputStream.
     *
     * @return
     */
    public String getOutput() {
        writer.flush();
        writer.close();
        return byteArrayOutputStream.toString();
    }

    public InputStream getInputStream() {
        writer.flush();
        writer.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
