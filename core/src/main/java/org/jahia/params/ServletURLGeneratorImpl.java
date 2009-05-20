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
 package org.jahia.params;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Mar 5, 2005
 * Time: 3:44:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServletURLGeneratorImpl implements URLGenerator {

    private HttpServletResponse response;

    public ServletURLGeneratorImpl(HttpServletRequest request, HttpServletResponse response) {
        this.response = response;
    }

    public String encodeURL(String url) {
        return response.encodeURL(url);
    }
}
