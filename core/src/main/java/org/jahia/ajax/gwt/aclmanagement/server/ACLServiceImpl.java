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
package org.jahia.ajax.gwt.aclmanagement.server;

import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.ajax.gwt.commons.server.rpc.JahiaContentServiceImpl;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.acl.ACLService;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.pages.JahiaPage;
import org.jahia.exceptions.JahiaException;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ParamBean;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * User: rfelden
 * Date: 27 nov. 2008 - 10:59:06
 */
public class ACLServiceImpl extends JahiaRemoteService implements ACLService {

    private static final Logger logger = Logger.getLogger(JahiaContentServiceImpl.class);

    public GWTJahiaNodeACL getACL(int aclid)  throws GWTJahiaServiceException {
        return getACL(aclid, false, null);
    }


    public void setACL(int aclid, GWTJahiaNodeACL acl) {
        setACL(aclid, false, null, acl);
    }

    public GWTJahiaNodeACL getACL(int aclid, boolean newAcl, String sessionIdentifier)  throws GWTJahiaServiceException {
        try {
            return ACLHelper.getGWTJahiaNodeACL(new JahiaBaseACL(aclid), newAcl, retrieveParamBean());
        } catch (JahiaException e) {
            logger.error("unable to get acl",e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }


    public void setACL(int aclid, boolean newAcl, String sessionIdentifier, GWTJahiaNodeACL acl) {
        try {
            JahiaBaseACL baseACL = new JahiaBaseACL(aclid);
            baseACL = ACLHelper.saveACL(acl, baseACL, newAcl);
            final ParamBean bean = retrieveParamBean();
            Map engineMap = (Map) bean.getSession().getAttribute("jahia_session_engineMap");
            if (newAcl) {
                engineMap.put("newacl_"+sessionIdentifier, baseACL);
            }
            JahiaEvent setRightsEvent = null;
            if (engineMap.containsKey("thePage")) {
                JahiaPage jahiaPage = ((JahiaPage) engineMap.get("thePage"));
                setRightsEvent = new JahiaEvent(jahiaPage, bean, acl);
            } else if (engineMap.containsKey("theContainer")) {
                JahiaContainer jahiaContainer = ((JahiaContainer) engineMap.get("theContainer"));
                setRightsEvent = new JahiaEvent(jahiaContainer, bean, acl);
            } else if (engineMap.containsKey("theContainerList")) {
                JahiaContainerList jahiaContainerList = ((JahiaContainerList) engineMap.get("theContainerList"));
                setRightsEvent = new JahiaEvent(jahiaContainerList, bean, acl);
            }
            if (setRightsEvent!=null) {
                ServicesRegistry.getInstance ().getJahiaEventService ().fireSetRights(setRightsEvent);
            }
        } catch (JahiaException e) {
            logger.error("Unable to set ACL for acl [" + aclid + "] due to:", e);
        }

    }

}
