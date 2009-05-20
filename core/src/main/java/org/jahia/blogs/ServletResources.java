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
package org.jahia.blogs;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Holds ThreadLocal variables containing needed resources throughout a 
 * request processing.
 *
 * @author Xavier Lawrence
 */
public class ServletResources {
    
    private static ThreadLocal currentConfig = new ThreadLocal();
    private static ThreadLocal currentRequest = new ThreadLocal();
    private static ThreadLocal currentResponse = new ThreadLocal();
    
    /**
     * Returns the HttpServletRequest object for the current Thread
     */
    public static HttpServletRequest getCurrentRequest() {
        return (HttpServletRequest)currentRequest.get();
    }
    
    /**
     * Sets the HttpServletRequest object for the current Thread
     */
    public static void setCurrentRequest(HttpServletRequest request) {
        currentRequest.set(request);
    }
    
    /**
     * Returns the HttpServletResponse object for the current Thread
     */
    public static HttpServletResponse getCurrentResponse() {
        return (HttpServletResponse)currentResponse.get();
    }
    
    /**
     * Sets the HttpServletResponse object for the current Thread
     */
    public static void setCurrentResponse(HttpServletResponse response) {
        currentResponse.set(response);
    }
    
    /**
     * Returns the ServletConfig object for the current Thread
     */
    public static ServletConfig getCurrentConfig() {
        return (ServletConfig)currentConfig.get();
    }
    
    /**
     * Sets the ServletConfig object for the current Thread
     */
    public static void setCurrentConfig(ServletConfig config) {
        currentConfig.set(config);
    }
}
