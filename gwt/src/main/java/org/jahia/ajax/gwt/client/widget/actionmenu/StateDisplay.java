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
package org.jahia.ajax.gwt.client.widget.actionmenu;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuService;
import org.jahia.ajax.gwt.client.data.actionmenu.GWTJahiaGlobalState;
import org.jahia.ajax.gwt.client.data.actionmenu.workflow.GWTJahiaWorkflowState;
import org.jahia.ajax.gwt.client.widget.actionmenu.timebasedpublishing.TimebasedPublishingIcon;
import org.jahia.ajax.gwt.client.widget.actionmenu.acldiff.AclDiffIcon;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;

/**
 * This is a status display without action menu.
 *
 * User: rfelden
 * Date: 29 sept. 2008
 */
public class StateDisplay extends FlexTable {

    public StateDisplay(final GWTJahiaPageContext page,
                        final String languageCode,
                        final String objectKey,
                        final String wfKey,
                        final boolean extendedDisplay) {
        super() ;
        if (extendedDisplay) {
            ActionMenuService.App.getInstance().getGlobalStateForObject(page, objectKey, wfKey, languageCode, new AsyncCallback<GWTJahiaGlobalState>() {
                public void onFailure(Throwable throwable) {
                    Log.error("Failed to retrieve object state for " + objectKey + "\n\n", throwable) ;
                }
                public void onSuccess(GWTJahiaGlobalState globalState) {
                    if (globalState != null) {
                        int row = getRowCount() ;
                        // add icons if available
                        if (globalState.hasWorkflowState()) {
                            setWidget(0, row++, new StateIcon(globalState.getWorkflowState(), "workflow-" + globalState.getWorkflowState().getExtendedWorkflowState(), true)) ;
                        }
                        if (globalState.hasTimebasedPublishingState()) {
                            setWidget(0, row++, new TimebasedPublishingIcon(globalState.getTimebasedPublishingState(), page)) ;
                        }
                        if (globalState.hasAclDiffState()) {
                            setWidget(0, row++, new AclDiffIcon(globalState.getAclDiffState(), page)) ;
                        }
                        if (globalState.hasIntegrityState()) {
                            setWidget(0, row++, new StateIcon(globalState.getIntegrityState(), globalState.getIntegrityState().isBlocker() ? "integrity_blocker" : "integrity", true)) ;
                        }
                    }
                }
            });
        } else {
            ActionMenuService.App.getInstance().getWorkflowStateForObject(page, objectKey, wfKey, languageCode, new AsyncCallback<GWTJahiaWorkflowState>() {
                public void onFailure(Throwable throwable) {
                    Log.error("Failed to retrieve object state for " + objectKey + "\n\n", throwable) ;
                }
                public void onSuccess(GWTJahiaWorkflowState globalState) {
                    if (globalState != null) {
                        int row = getRowCount() ;
                        // add icons if available
                        setWidget(0, row, new StateIcon(globalState, "workflow-" + globalState.getExtendedWorkflowState(), true)) ;
                    }
                }
            });
        }
    }

}
