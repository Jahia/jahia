/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
