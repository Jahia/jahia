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

package org.jahia.ajax.gwt.client.service.actionmenu;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.actionmenu.actions.GWTJahiaAction;
import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingState;
import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingDetails;
import org.jahia.ajax.gwt.client.data.actionmenu.GWTJahiaGlobalState;
import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffState;
import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffDetails;
import org.jahia.ajax.gwt.client.data.actionmenu.workflow.GWTJahiaWorkflowState;

import java.util.List;

public interface ActionMenuServiceAsync {

    void getGlobalStateForObject(GWTJahiaPageContext page, String objectKey, String wfKey, String languageCode, AsyncCallback<GWTJahiaGlobalState> async);

    void getWorkflowStateForObject(GWTJahiaPageContext page, String objectKey, String wfKey, String languageCode, AsyncCallback<GWTJahiaWorkflowState> async);

    void getTimebasedPublishingState(GWTJahiaPageContext page, String objectKey, AsyncCallback<GWTJahiaTimebasedPublishingState> async);

    void getTimebasedPublishingDetails(GWTJahiaPageContext page, GWTJahiaTimebasedPublishingState state, AsyncCallback<GWTJahiaTimebasedPublishingDetails> async);

    void getAclDiffState(GWTJahiaPageContext page, String objectKey, AsyncCallback<GWTJahiaAclDiffState> async);

    void getAclDiffDetails(GWTJahiaPageContext page, String objectKey, AsyncCallback<GWTJahiaAclDiffDetails> async);

    void isActionMenuAvailable(GWTJahiaPageContext page, String objectKey, String bundleName, String labelKey, AsyncCallback<String> async);

    void getAvailableActions(GWTJahiaPageContext page, String objectKey, String bundleName, String namePostFix, AsyncCallback<List<GWTJahiaAction>> async);

    void clipboardIsEmpty(AsyncCallback<Boolean> async);

    void clipboardCopy(GWTJahiaPageContext page, String objectKey, AsyncCallback<Boolean> async);

    void clipboardPaste(GWTJahiaPageContext page, String objectKey, AsyncCallback<Boolean> async);

    void hack(GWTJahiaAction action, AsyncCallback async);
    
}
