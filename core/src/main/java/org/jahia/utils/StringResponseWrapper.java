/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Response wrapper to retrieve included output as string. As an example the
 * {@link org.apache.taglibs.standard.tag.common.core.ImportSupport} was used.
 * 
 * @author Sergiy Shyrkov
 */
public class StringResponseWrapper extends HttpServletResponseWrapper {

    private static final String DEFAULT_ENCODING = "UTF-8";

    private ByteArrayOutputStream bos = new ByteArrayOutputStream();

    private boolean isStreamUsed;

    private boolean isWriterUsed;

    private ServletOutputStream sos = new ServletOutputStream() {
        @Override
        public void write(byte[] b) throws IOException {
            bos.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            bos.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            bos.write(b);
        }
    };

    private int status = HttpServletResponse.SC_OK;

    private StringWriter sw = new StringWriter();

    /**
     * Initializes an instance of this class.
     * 
     * @param response
     *            response object, whose output stream should be wrapped
     */
    public StringResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        if (isWriterUsed)
            throw new IllegalStateException(
                    "The getWriter() was already called before on this object");
        isStreamUsed = true;
        return sos;
    }

    public int getStatus() {
        return status;
    }

    public String getString() throws UnsupportedEncodingException {
        if (isWriterUsed)
            return sw.toString();
        else if (isStreamUsed) {
            return bos.toString(DEFAULT_ENCODING);
        } else
            return ""; // target didn't write anything
    }

    @Override
    public PrintWriter getWriter() {
        if (isStreamUsed)
            throw new IllegalStateException(
                    "The getOutputStream() was already called before on this object");
        isWriterUsed = true;
        return new PrintWriter(sw);
    }

    @Override
    public void setContentType(String x) {
        // ignore
    }

    @Override
    public void setLocale(Locale x) {
        // ignore
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }
}
