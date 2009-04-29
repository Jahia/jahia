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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jahia.ajax.gwt.client.data.GWTJahiaContext;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.util.URL;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 25 juil. 2008
 * Time: 12:40:05
 * To change this template use File | Settings | File Templates.
 */
public interface SessionManagerService extends RemoteService {

    public static class App {
        private static SessionManagerServiceAsync serv = null;

        public static synchronized SessionManagerServiceAsync getInstance() {
            if (serv == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint()+"sessionManager/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                serv = (SessionManagerServiceAsync) GWT.create(SessionManagerService.class);
                ((ServiceDefTarget) serv).setServiceEntryPoint(serviceEntryPoint);
            }
            return serv;
        }
    }

    public GWTJahiaContext getCoreSessionContext();

}