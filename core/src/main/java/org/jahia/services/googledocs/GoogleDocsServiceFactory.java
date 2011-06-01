/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.googledocs;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaUnauthorizedException;

import com.google.gdata.util.AuthenticationException;

/**
 * Document service that uses Google Data API to view/edit documents or perform
 * different format conversions.
 * 
 * @author Sergiy Shyrkov
 */
public class GoogleDocsServiceFactory {

    private String applicationName = "Jahia-xCM-v6.5";

    private String defaultTargetFolderName;

    public GoogleDocsService getDocsService(HttpServletRequest request, HttpServletResponse response)
            throws JahiaUnauthorizedException, AuthenticationException {
        GoogleDocsService docsService = null;
        String token = (String) request.getSession(true).getAttribute("org.jahia.googledocs.token");
        String username = null;
        String password = null;

        if (token == null) {
            String header = request.getHeader("Authorization");
            if (StringUtils.isNotEmpty(header) && header.startsWith("Basic ")) {
                try {
                    header = new String(Base64.decodeBase64(header.substring("Basic ".length())), "US-ASCII");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e);
                }
                username = StringUtils.substringBefore(header, ":");
                password = StringUtils.substringAfter(header, ":");
            }
        }
        if (token == null && (username == null || password == null)) {
            response.setHeader("WWW-Authenticate", "BASIC realm=\"Google Docs\"");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        docsService = token != null ? getGoogleDocsService(token) : getGoogleDocsService(username, password);
        if (token == null) {
            request.getSession(true).setAttribute("org.jahia.googledocs.token", docsService.getAuthToken());
        }
        return docsService;
    }

    public GoogleDocsService getGoogleDocsService(String userToken) throws JahiaUnauthorizedException,
            AuthenticationException {
        GoogleDocsService service = new GoogleDocsService(userToken);
        service.setApplicationName(applicationName);
        service.setDefaultTargetFolderName(defaultTargetFolderName);

        return service;
    }

    public GoogleDocsService getGoogleDocsService(String username, String password) throws JahiaUnauthorizedException,
            AuthenticationException {
        GoogleDocsService service = new GoogleDocsService(username, password);
        service.setApplicationName(applicationName);
        service.setDefaultTargetFolderName(defaultTargetFolderName);

        return service;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setDefaultTargetFolderName(String targetFolderName) {
        this.defaultTargetFolderName = targetFolderName;
    }

}
