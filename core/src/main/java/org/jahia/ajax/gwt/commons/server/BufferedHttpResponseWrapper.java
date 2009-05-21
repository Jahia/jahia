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
