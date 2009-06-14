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
package org.jahia.services.integrity;

import static org.apache.commons.httpclient.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.commons.httpclient.HttpStatus.SC_MOVED_PERMANENTLY;
import static org.apache.commons.httpclient.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.commons.httpclient.HttpStatus.SC_OK;
import static org.apache.commons.httpclient.HttpStatus.SC_TEMPORARY_REDIRECT;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.log4j.Logger;

/**
 * Validator implementation for performing external HTTP link validity checks.
 * 
 * @author Sergiy Shyrkov
 */
public class ExternalLinkValidator extends BaseLinkValidator {

    private static final transient Logger logger = Logger
            .getLogger(ExternalLinkValidator.class);

    private HttpClient httpClient;

    private int maxRedirects = 10;

    private String method = "head";

    /**
     * Performs the check for a link considering redirects.<br>
     * Uses the code of the Maven LinkCheck Plug-in.
     * 
     * @param link
     *            the link URL to be checked
     * @param nbRedirect
     *            the current redirect count
     * @param redirectsChain
     *            the list results to track redirects
     * @return the method as the result of the HTTP call
     * @throws HttpException
     *             if a HTTP communication exception occurs
     * @throws IOException
     *             if a read/wrote communication exception occurs
     */
    private HttpMethod checkLink(String link, int nbRedirect,
            List<LinkValidationResult> redirectsChain) throws HttpException,
            IOException {
        if (nbRedirect > maxRedirects) {
            throw new HttpException("Maximum number of redirections ("
                    + maxRedirects + ") exceeded");
        }
        HttpMethod httpMethod;
        if ("head".equals(this.method)) {
            httpMethod = new HeadMethod(link);
        } else {
            httpMethod = new GetMethod(link);
        }
        try {
            httpMethod.setFollowRedirects(false);
            URL url = new URL(link);
            httpClient.getHostConfiguration().setHost(url.getHost(),
                    url.getPort(), url.getProtocol());
            httpClient.executeMethod(httpMethod);
            StatusLine statusLine = httpMethod.getStatusLine();

            if (statusLine == null) {
                logger
                        .warn("Cannot get response status line when validating link: "
                                + link);
                return null;
            }

            int statusCode = httpMethod.getStatusCode();
            if (statusCode == SC_MOVED_PERMANENTLY
                    || statusCode == SC_MOVED_TEMPORARILY
                    || statusCode == SC_TEMPORARY_REDIRECT) {
                Header locationHeader = httpMethod
                        .getResponseHeader("location");
                if (locationHeader == null) {
                    logger
                            .error("Site sent redirect, but did not set Location header");
                    return httpMethod;
                } else {
                    String newLink = locationHeader.getValue();
                    if (!newLink.startsWith("http://")
                            && !newLink.startsWith("https://")) {
                        if (newLink.startsWith("/")) {
                            URL oldUrl = new URL(link);
                            newLink = oldUrl.getProtocol()
                                    + "://"
                                    + oldUrl.getHost()
                                    + (oldUrl.getPort() > 0 ? ":"
                                            + oldUrl.getPort() : "") + newLink;
                        } else {
                            newLink = link + newLink;
                        }
                    }
                    HttpMethod oldHtppMethod = httpMethod;
                    if (logger.isDebugEnabled()) {
                        logger.debug("[" + link + "] is redirected to ["
                                + newLink + "]");
                    }
                    oldHtppMethod.releaseConnection();
                    redirectsChain.add(new LinkValidationResult(statusCode,
                            httpMethod.getStatusText(), newLink));
                    httpMethod = checkLink(newLink, nbRedirect + 1,
                            redirectsChain);
                    // Restore the hm to "Moved permanently" |
                    // "Moved temporarily" | "Temporary redirect"
                    // if the new location is found to allow us to report it
                    if (httpMethod.getStatusCode() == SC_OK && nbRedirect == 0) {
                        return oldHtppMethod;
                    }
                }
            }

        } finally {
            httpMethod.releaseConnection();
        }
        return httpMethod;
    }

    /**
     * Returns the used instance of the {@link HttpClient}.
     * 
     * @return the used instance of the {@link HttpClient}
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public LinkValidationResult validate(Link link) {
        HttpMethod httpMethod = null;
        List<LinkValidationResult> redirectsChain = new LinkedList<LinkValidationResult>();
        try {
            httpMethod = checkLink(link.getUrl(), 0, redirectsChain);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error validating link " + link + ". Cause: "
                        + e.toString(), e);
            } else {
                logger.info("Error validating link " + link + ". Cause: "
                        + e.toString());
            }
            return new LinkValidationResult(SC_INTERNAL_SERVER_ERROR, e
                    .toString());
        }

        if (httpMethod == null) {
            return new LinkValidationResult(SC_INTERNAL_SERVER_ERROR,
                    "Cannot retreive HTTP status");
        }
        int statusCode = httpMethod.getStatusCode();
        String statusText = httpMethod.getStatusText();
        if (statusCode == SC_OK) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received code " + SC_OK + " for link " + link);
            }
            return new LinkValidationResult(SC_OK, statusText);
        } else {
            logger.info("Received error code " + statusCode + " [" + statusText
                    + "] for link " + link);
            if (statusCode == SC_MOVED_PERMANENTLY
                    || statusCode == SC_MOVED_TEMPORARILY
                    || statusCode == SC_TEMPORARY_REDIRECT) {
                String newUrl = null;
                StringBuilder redirects = new StringBuilder();
                for (LinkValidationResult redirect : redirectsChain) {
                    newUrl = redirect.getUrl();
                    redirects.append(redirect.getErrorCode()).append(" [")
                            .append(redirect.getErrorMessage()).append(
                                    "] redirect to [" + redirect.getUrl()
                                            + "]\n");
                }
                String redirectHistory = redirects.toString();
                if (logger.isDebugEnabled()) {
                    logger.debug("Received redirect " + statusCode + " ["
                            + redirectHistory + "] for link " + link
                            + ". New URL: " + newUrl);
                }
                return new LinkValidationResult(statusCode, redirectHistory,
                        newUrl);
            } else {
                return new LinkValidationResult(statusCode, statusText);
            }
        }
    }
}
