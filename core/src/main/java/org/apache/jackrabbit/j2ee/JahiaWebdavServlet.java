/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.apache.jackrabbit.j2ee;

import org.apache.jackrabbit.server.SessionProvider;
import org.apache.jackrabbit.server.SessionProviderImpl;
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

    private static transient Logger logger = LoggerFactory.getLogger(JahiaWebdavServlet.class);
    /**
     * the repository session provider
     */
    private SessionProvider sessionProvider;

    private DavLocatorFactory locatorFactory;

    private String resourcePathPrefix = "/repository";

    public DavLocatorFactory getLocatorFactory() {
        if (locatorFactory == null) {
            locatorFactory = new DavLocatorFactoryImpl(resourcePathPrefix);
        }
        return locatorFactory;
    }

    public synchronized SessionProvider getSessionProvider() {
        if (sessionProvider == null) {
            sessionProvider = new SessionProviderImpl(new JahiaSessionCredentials(""));
        }
        return sessionProvider;
    }

    protected boolean execute(WebdavRequest request, WebdavResponse response,
                              int method, DavResource resource)
            throws ServletException, IOException, DavException {
        if (logger.isDebugEnabled()) {
            switch (method) {
                case DavMethods.DAV_GET:
                    logger.debug("Calling GET method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_HEAD:
                    logger.debug("Calling HEAD method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_PROPFIND:
                    logger.debug("Calling PROPFIND method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_PROPPATCH:
                    logger.debug("Calling PROPPATCH method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_POST:
                    logger.debug("Calling POST method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_PUT:
                    logger.debug("Calling PUT method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_DELETE:
                    logger.debug("Calling DELETE method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_COPY:
                    logger.debug("Calling COPY method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_MOVE:
                    logger.debug("Calling MOVE method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_MKCOL:
                    logger.debug("Calling MKCOL method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_OPTIONS:
                    logger.debug("Calling OPTIONS method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_LOCK:
                    logger.debug("Calling LOCK method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_UNLOCK:
                    logger.debug("Calling UNLOCK method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_ORDERPATCH:
                    logger.debug("Calling ORDERPATCH method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_SUBSCRIBE:
                    logger.debug("Calling SUBSCRIBE method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_UNSUBSCRIBE:
                    logger.debug("Calling UNSUBSCRIBE method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_POLL:
                    logger.debug("Calling POLL method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_SEARCH:
                    logger.debug("Calling SEARCH method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_VERSION_CONTROL:
                    logger.debug("Calling VERSION method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_LABEL:
                    logger.debug("Calling LABEL method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_REPORT:
                    logger.debug("Calling REPORT method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_CHECKIN:
                    logger.debug("Calling CHECKIN method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_CHECKOUT:
                    logger.debug("Calling CHECKOUT method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_UNCHECKOUT:
                    logger.debug("Calling UNCHECKOUT method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_MERGE:
                    logger.debug("Calling MERGE method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_UPDATE:
                    logger.debug("Calling UPDATE method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_MKWORKSPACE:
                    logger.debug("Calling MKWORKSPACE method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_MKACTIVITY:
                    logger.debug("Calling MKACTIVITY method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_BASELINE_CONTROL:
                    logger.debug("Calling BASELINE method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_ACL:
                    logger.debug("Calling ACL method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_REBIND:
                    logger.debug("Calling REBIND method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_UNBIND:
                    logger.debug("Calling UNBIND method " + request.getPathTranslated());
                    break;
                case DavMethods.DAV_BIND:
                    logger.debug("Calling BIND method " + request.getPathTranslated());
                    break;
                default:
                    // any other method
                    logger.debug("Call to unknown method");
            }
        }
        if (method != DavMethods.DAV_GET && Boolean.TRUE.equals(request.getAttribute("isGuest"))) {
            throw new DavException(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return super.execute(request, response, method, resource);
    }

    @Override public void init() throws ServletException {
        super.init();
        setResourceFactory(new JahiaResourceFactoryImpl(getLockManager(), getResourceConfig()));
    }
}
