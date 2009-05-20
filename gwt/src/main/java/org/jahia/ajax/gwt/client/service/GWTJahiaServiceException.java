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
package org.jahia.ajax.gwt.client.service;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 8 sept. 2008
 * Time: 16:01:21
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaServiceException extends Exception implements Serializable {

    public GWTJahiaServiceException() {
        super() ;
    }

    public GWTJahiaServiceException(String message) {
        super(message) ;
    }

}
