/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
