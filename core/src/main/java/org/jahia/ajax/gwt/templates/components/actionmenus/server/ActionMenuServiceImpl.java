/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.actionmenus.server;

import org.jahia.ajax.gwt.templates.components.actionmenus.client.ActionMenuService;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.timebasedpublishing.GWTJahiaTimebasedPublishingDetails;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.timebasedpublishing.GWTJahiaTimebasedPublishingState;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.actions.*;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.workflow.GWTJahiaWorkflowState;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.GWTJahiaGlobalState;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.GWTJahiaIntegrityState;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.acldiff.GWTJahiaAclDiffState;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.acldiff.GWTJahiaAclDiffDetails;
import org.jahia.ajax.gwt.templates.components.actionmenus.server.helper.*;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
import org.jahia.ajax.gwt.commons.client.rpc.GWTJahiaServiceException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.registries.ServicesRegistry;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Implementaiton of GWT servoce for action menus and extras such as ACL, workflow and timebased publishing.
 *
 * @author rfelden
 * @version 22 janv. 2008 - 12:00:25
 */
public class ActionMenuServiceImpl extends AbstractJahiaGWTServiceImpl implements ActionMenuService {

    private final static Logger logger = Logger.getLogger(ActionMenuServiceImpl.class) ;
    private static final JahiaPreferencesService JAHIA_PREFERENCES_SERVICE = ServicesRegistry.getInstance().getJahiaPreferencesService();

    public GWTJahiaGlobalState getGlobalStateForObject(GWTJahiaPageContext page, String objectKey, String wfKey, String languageCode) {
        final ProcessingContext jParams = retrieveParamBean(page) ;
        GWTJahiaWorkflowState wf = null ;
        if (wfKey != null && wfKey.length() > 0) {
            wf = getWorkflowStateForObject(page, objectKey, wfKey,  languageCode) ;
        }
        GWTJahiaAclDiffState acl = null ;
        boolean aclDiff = JAHIA_PREFERENCES_SERVICE.getGenericPreferenceBooleanValue("acldiff.activated", false, jParams) ;
        if (aclDiff) {
            acl = getAclDiffState(page, objectKey) ;
        }
        GWTJahiaTimebasedPublishingState tbp = null ;
        boolean timebasepublishing = JAHIA_PREFERENCES_SERVICE.getGenericPreferenceBooleanValue("timebasepublishing.activated", false, jParams) ;
        if (timebasepublishing) {
            tbp = getTimebasedPublishingState(page, objectKey) ;
        }
        GWTJahiaIntegrityState integrityState = null ;
        boolean integrity = JAHIA_PREFERENCES_SERVICE.getGenericPreferenceBooleanValue("integrity.activated", false, jParams) ;
        if (integrity) {
            integrityState = getIntegrityState(page, objectKey) ;
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug(new StringBuilder("Server call details :\n")
                    .append("\t\t\t\t\tObject key : ").append(objectKey).append(" - Workflow key : ").append(wfKey).append("\n")
                    .append("\t\t\t\t\tWorkflow state (").append(String.valueOf(wfKey != null)).append(") : ").append((wf != null ? wf.getExtendedWorkflowState() : "null")).append("\n")
                    .append("\t\t\t\t\tACL diff state (").append(aclDiff).append(") : ").append(acl != null ? acl.getObjectKey() : "null").append("\n")
                    .append("\t\t\t\t\tTBP state (").append(timebasepublishing).append(") : ").append((tbp != null ? tbp.getState() : "null"))
                    .append("\t\t\t\t\tIntegrity state (").append(integrity).append(") : ").append((integrityState != null ? "not OK" : "OK")).toString());
        }
        GWTJahiaGlobalState state = new GWTJahiaGlobalState(acl, tbp, wf);
        state.setIntegrityState(integrityState);
        
        return state;
    }

    private GWTJahiaIntegrityState getIntegrityState(GWTJahiaPageContext page,
            String objectKey) {
        return IntegrityHelper.getState(objectKey, retrieveParamBean(page));
    }

    public GWTJahiaWorkflowState getWorkflowStateForObject(GWTJahiaPageContext page, String objectKey, String wfKey, String languageCode) {
        final ProcessingContext jParams = retrieveParamBean(page) ;
        return WorkflowHelper.getWorkflowStateForObject(jParams, wfKey, languageCode) ;
    }

    public GWTJahiaTimebasedPublishingState getTimebasedPublishingState(GWTJahiaPageContext page, String objectKey) {
        // parameters for submethod calls
        final boolean isDevMode = true ;//org.jahia.settings.SettingsBean.getInstance().isDevelopmentMode();
        final HttpServletRequest request = getThreadLocalRequest() ;
        final ProcessingContext jParams = retrieveParamBean(page) ;
        return TimebasedPublishingHelper.getTimebasePublishingState(request, jParams, isDevMode, objectKey) ;
    }

    public GWTJahiaTimebasedPublishingDetails getTimebasedPublishingDetails(GWTJahiaPageContext page, GWTJahiaTimebasedPublishingState state) {
        return TimebasedPublishingHelper.getTimebasedPublishingDetails(retrieveParamBean(page), state) ;
    }

    public GWTJahiaAclDiffState getAclDiffState(GWTJahiaPageContext page, String objectKey) {
        final boolean isDevMode = true ;//org.jahia.settings.SettingsBean.getInstance().isDevelopmentMode();
        final HttpServletRequest request = getThreadLocalRequest() ;
        final ProcessingContext jParams = retrieveParamBean(page) ;
        try {
            return AclDiffHelper.getAclDiffState(request, jParams, isDevMode, objectKey) ;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null ;
        }
    }

    public GWTJahiaAclDiffDetails getAclDiffDetails(GWTJahiaPageContext page, String objectKey) {
        try {
            return AclDiffHelper.getAclDiffDetails(retrieveParamBean(page), objectKey) ;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null ;
        }
    }

    public String isActionMenuAvailable(GWTJahiaPageContext page, String objectKey, String bundleName, String labelKey) {
        ProcessingContext processingContext = retrieveParamBean(page) ;
        return ActionMenuHelper.isActionMenuAvailable(processingContext, page, objectKey, bundleName, labelKey) ;
    }

    public List<GWTJahiaAction> getAvailableActions(final GWTJahiaPageContext page, final String objectKey, final String bundleName, final String namePostFix) {
        final ProcessingContext jParams = retrieveParamBean(page) ;
        HttpSession session = getThreadLocalRequest().getSession() ;
        return ActionMenuHelper.getAvailableActions(session, jParams, page, objectKey, bundleName, namePostFix) ;
    }

    public Boolean clipboardIsEmpty() {
        Object clipboardContent = getThreadLocalRequest().getSession().getAttribute(GWTJahiaAction.CLIPBOARD_CONTENT) ;
        return clipboardContent == null ;
    }

    public Boolean clipboardCopy(GWTJahiaPageContext page, String objectKey) {
        HttpSession session = getThreadLocalRequest().getSession() ;
        final ProcessingContext jParams = retrieveParamBean(page) ;
        return ClipboardHelper.clipboardCopy(session, jParams, objectKey) ;
    }

    public Boolean clipboardPaste(GWTJahiaPageContext page, String destObjectKey) {
        final HttpSession session = getThreadLocalRequest().getSession() ;
        final ProcessingContext processingContext  = retrieveParamBean(page) ;
        return ClipboardHelper.clipboardPaste(session, processingContext,  destObjectKey) ;
    }

    public void hack(GWTJahiaAction action) {
        // this is only a blank method to avoid a bug when a serializable type is unrecognized by gwt using a collection.
    }

}



