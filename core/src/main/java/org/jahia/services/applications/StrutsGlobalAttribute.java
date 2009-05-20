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
 package org.jahia.services.applications;

import java.util.Enumeration;

import javax.servlet.ServletException;

/**
 * Struts Global Attribute
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class StrutsGlobalAttribute {

    public static String REQUEST_SCOPE = "REQUEST";
    public static String SESSION_SCOPE = "SESSION";

    private String attributeName;
    private String attributeScope;
    private Object attributeValue;
    private boolean attributeExist = false;

    /**
     * @param attributeName
     * @param attributeScope REQUEST_SCOPE, SESSION_SCOPE
     */
    public StrutsGlobalAttribute (String attributeName, String attributeScope) {
        this.attributeName = attributeName;
        this.attributeScope = attributeScope;
    }

    /**
     * @param request
     * @param response
     *
     * @throws ServletException
     * @throws java.io.IOException
     */
    public void backupAttribute (ServletIncludeRequestWrapper request,
                                 ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException {
        if (isRequestScope ()) {
            if (checkAttribute (request.getAttributeNames ())) {
                this.attributeValue = request.getAttribute (this.attributeName);
                request.removeAttribute (this.attributeName);
            }
        } else {
            if (checkAttribute (request.getAttributeNames ())) {
                this.attributeValue = request.getSession ()
                        .getAttribute (this.attributeName);
                request.getSession ().removeAttribute (this.attributeName);
            }
        }
    }

    /**
     * @param request
     * @param response
     *
     * @throws ServletException
     * @throws java.io.IOException
     */
    public void restoreAttribute (ServletIncludeRequestWrapper request,
                                  ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException {
        if (this.attributeExist) {
            if (isRequestScope ()) {
                request.setAttribute (this.attributeName, this.attributeValue);
            } else {
                request.getSession ().setAttribute (this.attributeName,
                        this.attributeValue);
            }
        }
    }

    private boolean isRequestScope () {
        return REQUEST_SCOPE.equals (this.attributeScope);
    }

    private boolean checkAttribute (Enumeration names) {
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            if (name.equals (this.attributeName)) {
                this.attributeExist = true;
                break;
            }
        }
        return this.attributeExist;
    }

}
