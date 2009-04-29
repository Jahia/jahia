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
 package org.jahia.urls;

import java.net.MalformedURLException;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ServletURL extends QueryMapURL {

    private String contextPath;
    private String servletPath;
    private String pathInfo;

    public ServletURL () {
        super();
    }

    public ServletURL (HttpServletRequest request)
        throws MalformedURLException {
        this(request, true);
    }

    public ServletURL (HttpServletRequest request, boolean relative)
        throws MalformedURLException {
        this(request.getScheme(), request.getServerName(),
             request.getServerPort(), request.getContextPath(),
             request.getServletPath(), request.getPathInfo(),
             request.getQueryString(), relative);
    }

    public ServletURL (String contextPath, String servletPath, String pathInfo,
                       String queryString) throws MalformedURLException {
        this(null, null, -1, contextPath, servletPath, pathInfo, queryString, true);
    }

    public ServletURL (String scheme, String serverName, int serverPort,
                       String contextPath, String servletPath, String pathInfo,
                       String queryString, boolean relative)
        throws MalformedURLException {

        this();
        setScheme(scheme);
        setHostName(serverName);
        setPort(serverPort);

        this.contextPath = contextPath;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        setPath(contextPath, servletPath, pathInfo);
        setQueryString(queryString);

        setURIStartingAtPath(relative);

    }

    /**
     * Servlet API pattern matching resolution. This static method can be
     * used to test the validity of a servlet path against a Servlet API
     * mapping pattern.
     * @param pattern the pattern to be matched against, according to
     * Servlet API 2.3 Section 11.2 p71
     * @param servletPath the servlet path to test, to see if it matches
     * the pattern passed.
     * @return true if the servlet path matches the pattern, false otherwise.
     */
    static public boolean matchesServletPattern(String pattern, String servletPath) {
        if ((pattern == null) || (servletPath == null)) {
            return false;
        }
        // first we must check what type of mapping the pattern uses.
        if ((pattern.startsWith("/") && pattern.endsWith("/*"))) {
            // path mapping detected.
            String pathToMatch = pattern.substring(0, pattern.length()-2);
            if (servletPath.startsWith(pathToMatch)) {
                return true;
            } else {
                return false;
            }
        } else if (pattern.startsWith("*.")) {
            // extension mapping detected.
            int lastPointPos = servletPath.lastIndexOf(".");
            if (lastPointPos == -1) {
                return false;
            }
            String extension = servletPath.substring(lastPointPos + 1);
            String patternExt = pattern.substring(2);
            if (patternExt.equals(extension)) {
                return true;
            } else {
                return false;
            }
        } else if (!pattern.equals("/")) {
            // exact mapping detected.
            if (pattern.equals(servletPath)) {
                return true;
            } else {
                return false;
            }
        } else {
            // default servlet mapping "/"
            return true;
        }
    }

    public String getContextPath () {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
        setPath(getContextPath(), contextPath, getPathInfo());
    }

    public String getServletPath () {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
        setPath(getContextPath(), servletPath, getPathInfo());
    }

    public String getPathInfo () {
        return pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
        setPath(getContextPath(), getServletPath(), pathInfo);
    }

    public String getRequestURI () {
        return getPath();
    }

    private void setPath(String contextPath, String servletPath, String pathInfo) {
        StringBuffer result = new StringBuffer();
        result.append(contextPath);
        if (servletPath != null) {
            result.append(servletPath);
        }
        if (pathInfo != null) {
            result.append(pathInfo);
        }
        super.setPath(result.toString());
    }


}