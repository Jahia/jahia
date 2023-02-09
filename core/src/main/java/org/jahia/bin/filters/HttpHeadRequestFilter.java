/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin.filters;

import org.jahia.bin.Render;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Http servlet filter that handle "HEAD" requests
 *
 * @author Kevan
 */
public class HttpHeadRequestFilter implements Filter{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (Render.METHOD_HEAD.equals(httpServletRequest.getMethod())) {

            // wrap response using NoBodyResponseWrapper
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            NoBodyResponseWrapper noBodyResponseWrapper = new NoBodyResponseWrapper(httpServletResponse);

            // continue the request
            chain.doFilter(new GetRequestWrapper(httpServletRequest), noBodyResponseWrapper);

            // set the content length header
            noBodyResponseWrapper.setContentLength();
        } else {
            // just continue the chain
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // do nothing
    }

    /**
     * A response that includes no body, for use in (dumb) "HEAD" support.
     * This just swallows that body, counting the bytes in order to set
     * the content length appropriately.  All other methods delegate directly
     * to the wrapped HTTP Servlet Response object.
     */
    private class NoBodyResponseWrapper extends HttpServletResponseWrapper {

        private NoBodyOutputStream noBody;
        private PrintWriter writer;
        private boolean didSetContentLength;
        private boolean usingOutputStream;

        NoBodyResponseWrapper(HttpServletResponse r) {
            super(r);
            noBody = new NoBodyOutputStream();
        }

        void setContentLength() {
            if (!didSetContentLength) {
                if (writer != null) {
                    writer.flush();
                }
                setContentLength(noBody.getContentLength());
            }
        }

        public void setContentLength(int len) {
            super.setContentLength(len);
            didSetContentLength = true;
        }

        public ServletOutputStream getOutputStream() throws IOException {

            if (writer != null) {
                throw new IllegalStateException("Illegal to call getOutputStream() after getWriter() has been called");
            }
            usingOutputStream = true;

            return noBody;
        }

        public PrintWriter getWriter() throws UnsupportedEncodingException {

            if (usingOutputStream) {
                throw new IllegalStateException("Illegal to call getWriter() after getOutputStream() has been called");
            }

            if (writer == null) {
                OutputStreamWriter w = new OutputStreamWriter(
                        noBody, getCharacterEncoding());
                writer = new PrintWriter(w);
            }

            return writer;
        }
    }

    /**
     * Servlet output stream that gobbles up all its data.
     */
    private class NoBodyOutputStream extends ServletOutputStream {

        private int contentLength = 0;

        NoBodyOutputStream() {}

        int getContentLength() {
            return contentLength;
        }

        public void write(int b) {
            contentLength++;
        }

        public void write(byte buf[], int offset, int len)
                throws IOException
        {
            if (len >= 0) {
                contentLength += len;
            } else {
                throw new IOException("Negative Length given in write method");
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }
    }

    /**
     * A request wrapper to force method to "GET"
     */
    private class GetRequestWrapper extends HttpServletRequestWrapper {
        GetRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        public String getMethod() {
            return Render.METHOD_GET;
        }
    }
}
