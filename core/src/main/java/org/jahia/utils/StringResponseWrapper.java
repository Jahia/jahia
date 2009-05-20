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
