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

import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuService;
import org.jahia.ajax.gwt.client.data.actionmenu.GWTJahiaGlobalState;
import org.jahia.ajax.gwt.client.widget.actionmenu.actions.ActionMenuIcon;
import org.jahia.ajax.gwt.client.widget.actionmenu.timebasedpublishing.TimebasedPublishingIcon;
import org.jahia.ajax.gwt.client.widget.actionmenu.acldiff.AclDiffIcon;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 22 janv. 2008 - 16:12:47
 */
public class ActionMenuDisplay extends FlexTable {

    public ActionMenuDisplay(final GWTJahiaPageContext page,
                             final String objectKey,
                             final String wfKey,
                             final String bundleName,
                             final String namePostFix,
                             final String labelKey,
                             final String iconStyle,
                             final boolean disableToolbarView) {
        super() ;

        // add the action menu icon
        setWidget(0, 0, new ActionMenuIcon(page, objectKey, bundleName, namePostFix, labelKey, iconStyle)) ;

        ActionMenuService.App.getInstance().getGlobalStateForObject(page, objectKey, wfKey, null, new AsyncCallback<GWTJahiaGlobalState>() {
            public void onFailure(Throwable throwable) {
                Log.error("Failed to retrieve object state for " + objectKey + "\n\n", throwable) ;
            }
            public void onSuccess(GWTJahiaGlobalState globalState) {
                if (globalState != null) {
                    int row = getRowCount() ;
                    // add icons if available
                    if (!disableToolbarView) {
                        // add fill item if requested
                        setWidget(0, row, new Label());
                        getCellFormatter().setStyleName(0, row++, "fillStyle");
                    }
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
    }

}
