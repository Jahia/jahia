/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
