/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.service.acl;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.core.client.GWT;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;

/**
 * User: rfelden
 * Date: 27 nov. 2008 - 10:48:14
 */
public interface ACLService  extends RemoteService {
    /**
     * Utility/Convenience class.
     * Use JahiaService.App.getInstance() to access static instance of MyServiceAsync
     */
    public static class App {
        private static ACLServiceAsync ourInstance = null;

        public static synchronized ACLServiceAsync getInstance() {
            if (ourInstance == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint()+"acl.gwt";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                ourInstance = (ACLServiceAsync) GWT.create(ACLService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(serviceEntryPoint);
            }

            return ourInstance;
        }
    }

    public GWTJahiaNodeACL getACL(int aclid)  throws GWTJahiaServiceException;

    public void setACL(int aclid, GWTJahiaNodeACL acl);

    public GWTJahiaNodeACL getACL(int aclid, boolean newAcl, String sessionIdentifier)  throws GWTJahiaServiceException;

    public void setACL(int aclid, boolean newAcl, String sessionIdentifier, GWTJahiaNodeACL acl);

}
