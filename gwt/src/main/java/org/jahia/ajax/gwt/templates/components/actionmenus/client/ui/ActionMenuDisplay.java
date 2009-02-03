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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.actionmenus.client.ui;

import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.ActionMenuService;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.GWTJahiaGlobalState;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.ui.actions.ActionMenuIcon;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.ui.timebasedpublishing.TimebasedPublishingIcon;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.ui.acldiff.AclDiffIcon;

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
