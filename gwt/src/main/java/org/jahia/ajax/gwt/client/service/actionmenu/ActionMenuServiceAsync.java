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

    void clipboardPasteReference(GWTJahiaPageContext page, String objectKey, AsyncCallback<Boolean> async);

    void hack(GWTJahiaAction action, AsyncCallback async);
    
}
