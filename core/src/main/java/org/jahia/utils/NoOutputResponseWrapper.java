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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.StringUtils;

/**
 * Response wrapper that skips the written output.
 *
 * @author Sergiy Shyrkov
 */
public class NoOutputResponseWrapper extends HttpServletResponseWrapper {

    private boolean isStreamUsed;

    private boolean isWriterUsed;

    private ServletOutputStream sos = new ServletOutputStream() {
        @Override
        public void write(byte[] b) throws IOException {
            // do nothing
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            // do nothing
        }

        @Override
        public void write(int b) throws IOException {
            // do nothing
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

    /**
     * Initializes an instance of this class.
     *
     * @param response
     *            response object, whose output stream should be wrapped
     */
    public NoOutputResponseWrapper(HttpServletResponse response) {
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
        return StringUtils.EMPTY;
    }

    @Override
    public PrintWriter getWriter() {
        if (isStreamUsed)
            throw new IllegalStateException(
                    "The getOutputStream() was already called before on this object");
        isWriterUsed = true;
        return new PrintWriter(sos);
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
