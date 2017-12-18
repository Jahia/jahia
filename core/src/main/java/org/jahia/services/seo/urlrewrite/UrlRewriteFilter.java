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
        } else if (hsRequest.getAttribute("UrlRewriteFilter.sendError") != null) {
            // workaround: call sendError with the specified error code 
            hsResponse.sendError(Integer.valueOf((String) hsRequest.getAttribute("UrlRewriteFilter.sendError")));
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
