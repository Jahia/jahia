/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.seo.urlrewrite;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tuckey.web.filters.urlrewrite.Status;
import org.tuckey.web.filters.urlrewrite.UrlRewriteWrappedResponse;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;
import org.tuckey.web.filters.urlrewrite.utils.ServerNameMatcher;

/**
 * Jahia specific implementation of the {@link org.tuckey.web.filters.urlrewrite.UrlRewriteFilter} that adds multiple configuration
 * resources lookup support and other features.
 *
 * @author Sergiy Shyrkov
 */
public class UrlRewriteFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(UrlRewriteFilter.class);

    private boolean enabled = true;

    private boolean outboundRulesEnabled = true;

    private boolean statusEnabled = true;

    private String statusPath = "/modules/tools/rewrite-status";

    private ServerNameMatcher statusServerNameMatcher;

    private UrlRewriteService urlRewriteService;

    public void destroy() {
        // do nothing
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        UrlRewriter urlRewriter = urlRewriteService.getEngine();

        final HttpServletRequest hsRequest = (HttpServletRequest) request;
        HttpServletResponse hsResponse = outboundRulesEnabled ? new UrlRewriteWrappedResponse(
                (HttpServletResponse) response, hsRequest, urlRewriter)
                : (HttpServletResponse) response;

        hsResponse = new SessionidRemovalResponseWrapper(hsRequest, hsResponse);

        // check for status request
        String uri = hsRequest.getRequestURI();

        logger.debug("Handling {}", uri);

        if (statusEnabled
                && uri != null
                && uri.startsWith(hsRequest.getContextPath() + statusPath)
                && (statusServerNameMatcher == null || statusServerNameMatcher.isMatch(request
                        .getServerName()))) {
            showStatus(hsRequest, hsResponse);
            return;
        }

        urlRewriteService.prepareInbound(hsRequest, hsResponse);

        // if no rewrite has taken place continue as normal
        if (!urlRewriter.processRequest(hsRequest, hsResponse, chain)) {
            chain.doFilter(hsRequest, hsResponse);
        }

        Object sendErrorAttribute = hsRequest.getAttribute("UrlRewriteFilter.sendError");
        if (sendErrorAttribute != null) {
            // workaround: call sendError with the specified error code
            hsRequest.removeAttribute("UrlRewriteFilter.sendError");
            hsResponse.sendError(Integer.parseInt((String) sendErrorAttribute));
        }
    }

    public void init(FilterConfig cfg) throws ServletException {
        // do nothing
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOutboundRulesEnabled(boolean outboundRulesEnabled) {
        this.outboundRulesEnabled = outboundRulesEnabled;
    }

    public void setUrlRewriteService(UrlRewriteService urlRewriteService) {
        this.urlRewriteService = urlRewriteService;
    }

    /**
     * Show the status of the conf and the filter to the user.
     *
     * @param request
     *            to get status info from
     * @param response
     *            response to show the status on.
     * @throws java.io.IOException
     *             if the output cannot be written
     */
    protected void showStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Status status = new Status(urlRewriteService.getEngine().getConf());
        status.displayStatusInContainer(request);
        response.setContentType("text/html; charset=UTF-8");
        response.setContentLength(status.getBuffer().length());
        final PrintWriter out = response.getWriter();
        out.write(status.getBuffer().toString());
        out.close();
    }
}
