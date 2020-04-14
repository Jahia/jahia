/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.j2ee;

import org.apache.jackrabbit.server.CredentialsProvider;
import org.apache.jackrabbit.webdav.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * User: toto
 * Date: 27 déc. 2007
 * Time: 18:44:34
 * 
 */
public class JahiaWebdavServlet extends SimpleWebdavServlet {

    private static final long serialVersionUID = 43821067248762234L;
    private static transient Logger logger = LoggerFactory.getLogger(JahiaWebdavServlet.class);

    @SuppressWarnings("squid:S1075")
    private static final String RESOURCE_PATH_PREFIX = "/repository";

    @Override
    protected CredentialsProvider getCredentialsProvider() {
        return new JahiaSessionCredentials();
    }

    @Override
    protected boolean execute(WebdavRequest request, WebdavResponse response, int method, DavResource resource)
            throws ServletException, IOException, DavException {

        if (logger.isDebugEnabled()) {
            logger.debug("Calling {} method {}", getDavMethod(method), request.getPathTranslated());
        }

        if (method != DavMethods.DAV_GET && Boolean.TRUE.equals(request.getAttribute("isGuest"))) {
            throw new DavException(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return super.execute(request, response, method, resource);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        setLocatorFactory(new DavLocatorFactoryImpl(RESOURCE_PATH_PREFIX));
        setResourceFactory(new JahiaResourceFactoryImpl(getLockManager(), getResourceConfig()));
    }

    @SuppressWarnings("squid:S1479")
    private static String getDavMethod(int code) {
        switch (code) {
            case DavMethods.DAV_GET:
                return DavMethods.METHOD_GET;
            case DavMethods.DAV_HEAD:
                return DavMethods.METHOD_HEAD;
            case DavMethods.DAV_PROPFIND:
                return DavMethods.METHOD_PROPFIND;
            case DavMethods.DAV_PROPPATCH:
                return DavMethods.METHOD_PROPPATCH;
            case DavMethods.DAV_POST:
                return DavMethods.METHOD_POST;
            case DavMethods.DAV_PUT:
                return DavMethods.METHOD_PUT;
            case DavMethods.DAV_DELETE:
                return DavMethods.METHOD_DELETE;
            case DavMethods.DAV_COPY:
                return DavMethods.METHOD_COPY;
            case DavMethods.DAV_MOVE:
                return DavMethods.METHOD_MOVE;
            case DavMethods.DAV_MKCOL:
                return DavMethods.METHOD_MKCOL;
            case DavMethods.DAV_OPTIONS:
                return DavMethods.METHOD_OPTIONS;
            case DavMethods.DAV_LOCK:
                return DavMethods.METHOD_LOCK;
            case DavMethods.DAV_UNLOCK:
                return DavMethods.METHOD_UNLOCK;
            case DavMethods.DAV_ORDERPATCH:
                return DavMethods.METHOD_ORDERPATCH;
            case DavMethods.DAV_SUBSCRIBE:
                return DavMethods.METHOD_SUBSCRIBE;
            case DavMethods.DAV_UNSUBSCRIBE:
                return DavMethods.METHOD_UNSUBSCRIBE;
            case DavMethods.DAV_POLL:
                return DavMethods.METHOD_POLL;
            case DavMethods.DAV_SEARCH:
                return DavMethods.METHOD_SEARCH;
            case DavMethods.DAV_VERSION_CONTROL:
                return DavMethods.METHOD_VERSION_CONTROL;
            case DavMethods.DAV_LABEL:
                return DavMethods.METHOD_LABEL;
            case DavMethods.DAV_REPORT:
                return DavMethods.METHOD_REPORT;
            case DavMethods.DAV_CHECKIN:
                return DavMethods.METHOD_CHECKIN;
            case DavMethods.DAV_CHECKOUT:
                return DavMethods.METHOD_CHECKOUT;
            case DavMethods.DAV_UNCHECKOUT:
                return DavMethods.METHOD_UNCHECKOUT;
            case DavMethods.DAV_MERGE:
                return DavMethods.METHOD_MERGE;
            case DavMethods.DAV_UPDATE:
                return DavMethods.METHOD_UPDATE;
            case DavMethods.DAV_MKWORKSPACE:
                return DavMethods.METHOD_MKWORKSPACE;
            case DavMethods.DAV_MKACTIVITY:
                return DavMethods.METHOD_MKACTIVITY;
            case DavMethods.DAV_BASELINE_CONTROL:
                return DavMethods.METHOD_BASELINE_CONTROL;
            case DavMethods.DAV_ACL:
                return DavMethods.METHOD_ACL;
            case DavMethods.DAV_REBIND:
                return DavMethods.METHOD_REBIND;
            case DavMethods.DAV_UNBIND:
                return DavMethods.METHOD_UNBIND;
            case DavMethods.DAV_BIND:
                return DavMethods.METHOD_BIND;
            default:
                return "unknown";
        }
    }

}
