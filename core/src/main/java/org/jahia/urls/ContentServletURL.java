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
import org.jahia.utils.InsertionSortedMap;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ContentServletURL extends ServletURL {
    
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContentServletURL.class);
    
    InsertionSortedMap pathInfoParameters = new InsertionSortedMap();
    
    public ContentServletURL(HttpServletRequest request)
    throws MalformedURLException {
        super(request);
    }
    
    public ContentServletURL(HttpServletRequest request, String url, boolean ignoringAuthorityInTest) throws MalformedURLException {
        super();
        if (!ContentServletURL.isContentServletURL(request, url,
                ignoringAuthorityInTest)) {
            throw new MalformedURLException("URL " + url +
                    " is not a valid content servlet URL");
        }
        
        /** @todo this code is not optimized and very similar to the
         * isContentServletURL method ! Find a cleaner way to do this !
         */
        ContentServletURL requestURL = new ContentServletURL(request);
        URI targetURI = new URI(url);
        QueryMapURL targetURL = null;
        if (targetURI.isRelative()) {
            String curRequestURI = requestURL.getRequestURI();
            int lastSlashPos = curRequestURI.lastIndexOf("/");
            if (lastSlashPos != -1) {
                String newURI = curRequestURI.substring(0, lastSlashPos+1) + url;
                targetURL = new QueryMapURL(newURI);
            } else {
                // this shouldn't happen !
                targetURL = new QueryMapURL(url);
            }
        } else {
            targetURL = new QueryMapURL(url);
        }
        setScheme(targetURL.getScheme());
        setUserInfo(targetURL.getUserInfo());
        setHostName(targetURL.getHostName());
        setPort(targetURL.getPort());
        // now let's compare with the request URL to see if they match. The
        // detail of this comparison depends on weather we are comparing
        // in relative mode or in fully qualified mode.
        String targetPath = targetURL.getPath();
        String targetServletPath = targetPath.substring(requestURL.
                getContextPath().length());
        int queryPos = targetServletPath.indexOf("?");
        int fragmentPos = targetServletPath.indexOf("#");
        String targetQueryString = null;
        String targetFragmentString = null;
        String targetPathInfo;
        if (queryPos > 0) {
            targetPathInfo = targetServletPath.substring(requestURL.
                    getServletPath().length(), queryPos);
            if (fragmentPos > 0) {
                targetQueryString = targetServletPath.substring(queryPos+1, fragmentPos);
                targetFragmentString = targetServletPath.substring(fragmentPos+1);
            } else {
                targetQueryString = targetServletPath.substring(queryPos+1);
            }
        } else if (fragmentPos > 0) {
            targetPathInfo = targetServletPath.substring(requestURL.
                    getServletPath().length(), fragmentPos);
            targetFragmentString = targetServletPath.substring(fragmentPos+1);
        } else {
            targetPathInfo = targetServletPath.substring(requestURL.
                    getServletPath().length());
        }
        setPathInfo(targetPathInfo);
        setQueryString(targetQueryString);
        setFragmentString(targetFragmentString);
    }
    
    static public boolean isContentServletURL(HttpServletRequest request,
            final String url, boolean ignoringAuthorityInTest)
            throws MalformedURLException {
        final ContentServletURL requestURL = new ContentServletURL(request);
        final URI targetURL;
        try {
            targetURL = new URI(url);
        } catch (Exception e) {
            logger.warn("URI '"+ url + "' is not valid");
            return false;
        }
        // now let's compare with the request URL to see if they match. The
        // detail of this comparison depends on weather we are comparing
        // in relative mode or in fully qualified mode.
        if ((!targetURL.isURIStartingAtPath()) &&
                (!targetURL.isRelative()) &&
                (!ignoringAuthorityInTest)) {
            if (!requestURL.getScheme().equals(targetURL.getScheme())) {
                return false;
            }
            if (!requestURL.getHostName().equals(targetURL.getHostName())) {
                return false;
            }
            if (requestURL.getPort() != targetURL.getPort()) {
                return false;
            }
        }
        if (!targetURL.isRelative()) {
            String targetPath = targetURL.getPath();
            if (!targetPath.startsWith(requestURL.getContextPath())) {
                return false;
            }
            String targetServletPath = targetPath.substring(requestURL.
                    getContextPath().length());
            if (!targetServletPath.startsWith(requestURL.getServletPath())) {
                return false;
            }
        }
        // if we got this far this means we have successfully matched both the
        // context and the servlet path.

        // SCSE 14.06.2005: check if the URL contains only JavaScript block
        if (targetURL.getScheme() != null
            && "javascript".equals(targetURL.getScheme().toLowerCase())) {
           return false;
        }

        /**
         * @todo we can add here checks on the path info to make sure the
         * number of path info parameters is a multiple of two, etc, but for
         * the time being we will stop here.
         */
        return true;
    }
    
    public String getPathInfoParameter(String name) {
        return (String) pathInfoParameters.get(name);
    }
    
    public String setPathInfoParameter(String name, String value) {
        return (String) pathInfoParameters.put(name, value);
    }
    
    public String getPathInfo() {
        if (pathInfoParameters.size() == 0) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        java.util.Iterator parameterIter = pathInfoParameters.entrySet().iterator();
        while (parameterIter.hasNext()) {
            Entry curEntry = (Entry) parameterIter.next();
            result.append("/");
            result.append(curEntry.getKey());
            result.append("/");
            result.append(curEntry.getValue());
        }
        return result.toString();
        
    }
    
    public void setPathInfo(String pathInfo) {
        super.setPathInfo(pathInfo);
        parsePathInfoParameters(pathInfo);
    }
    
    private void parsePathInfoParameters(String pathInfo) {
        // Parse the PathInfo and build a custom parameter map
        
        if (pathInfo != null) {
            
            if (pathInfo.lastIndexOf(";jsessionid=") != -1) {
                // let's remove the session ID from the parameters if it was attached.
                int sessionIDPos = pathInfo.lastIndexOf(";jsessionid=");
                pathInfo = pathInfo.substring(0, sessionIDPos);
                logger.debug("Removed session ID marker from end of path info");
            }
            
            if (pathInfo.lastIndexOf(".") != -1) {
                // let's remove false static ending.
                int lastSlash = pathInfo.lastIndexOf("/");
                if (lastSlash != -1) {
                    String fakeStaticName = pathInfo.substring(lastSlash + 1);
                    pathInfo = pathInfo.substring(0, lastSlash);
                    logger.debug("Removed fake static ending. pathInfo=[" +
                            pathInfo + "] fakeEnding=[" +
                            fakeStaticName + "]");
                }
            }
            
            try {
                StringTokenizer st = new StringTokenizer(pathInfo, "/");
                
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    pathInfoParameters.put(token, st.nextToken());
                }
                
            } catch (NoSuchElementException nee) {
                // stop parsing token
            }
        }
    }
    
}