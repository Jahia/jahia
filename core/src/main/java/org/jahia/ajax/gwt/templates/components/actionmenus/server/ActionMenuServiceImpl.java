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
package org.jahia.ajax.gwt.templates.components.actionmenus.server;

import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuService;
import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingDetails;
import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingState;
import org.jahia.ajax.gwt.client.data.actionmenu.actions.*;
import org.jahia.ajax.gwt.client.data.actionmenu.workflow.GWTJahiaWorkflowState;
import org.jahia.ajax.gwt.client.data.actionmenu.GWTJahiaGlobalState;
import org.jahia.ajax.gwt.client.data.actionmenu.GWTJahiaIntegrityState;
import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffState;
import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffDetails;
import org.jahia.ajax.gwt.templates.components.actionmenus.server.helper.*;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
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
        return ClipboardHelper.clipboardPaste(session, processingContext,  destObjectKey, false) ;
    }

    public Boolean clipboardPasteReference(GWTJahiaPageContext page, String destObjectKey) {
        final HttpSession session = getThreadLocalRequest().getSession() ;
        final ProcessingContext processingContext  = retrieveParamBean(page) ;
        return ClipboardHelper.clipboardPaste(session, processingContext,  destObjectKey, true) ;
    }

    public void hack(GWTJahiaAction action) {
        // this is only a blank method to avoid a bug when a serializable type is unrecognized by gwt using a collection.
    }

}



