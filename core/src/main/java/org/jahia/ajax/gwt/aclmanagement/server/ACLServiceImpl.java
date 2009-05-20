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
package org.jahia.ajax.gwt.aclmanagement.server;

import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
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
public class ACLServiceImpl extends AbstractJahiaGWTServiceImpl implements ACLService {

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
