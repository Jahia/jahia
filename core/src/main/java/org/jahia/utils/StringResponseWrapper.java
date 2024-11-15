/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
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

    private String redirect;

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

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
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

    public String getRedirect() {
        return redirect;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        this.redirect = location;
    }

    @Override
    public void flushBuffer() throws IOException {
        sos.flush();
        sw.flush();
    }
}
