/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An action item for executing configured action on the current JCR node.
 * 
 * @author david
 */
public class ExecuteActionItem extends NodeTypeAwareBaseActionItem {
    private static final long serialVersionUID = -1317342305404063292L;
    public static final int STATUS_CODE_OK = 200;
    protected String action;
    protected String confirmationMessageKey;
    private Set<String> requiredNodeTypes;
    protected String parameterData;

    public void onComponentSelection() {
        if (confirmationMessageKey != null) {
            MessageBox.confirm(
                    Messages.get("label.information", "Information"),
                    Messages.get(confirmationMessageKey, "You are about to execute action "
                            + action + ". Do you want to continue?"),
                    new Listener<MessageBoxEvent>() {
                        public void handleEvent(MessageBoxEvent be) {
                            if (Dialog.YES.equalsIgnoreCase(be.getButtonClicked().getItemId())) {
                                doAction();
                            }
                        }
                    });
        } else {
            doAction();
        }
    }

    protected void doAction() {
        final List<GWTJahiaNode> gwtJahiaNodes = linker.getSelectionContext().getMultipleSelection();
        for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
            String baseURL = org.jahia.ajax.gwt.client.util.URL.getAbsoluteURL(JahiaGWTParameters.getContextPath() + "/cms/render");
            String localURL = baseURL + "/default/" + JahiaGWTParameters.getLanguage() + URL.encode(gwtJahiaNode.getPath());
            linker.loading(Messages.get("label.executing", "Executing action ..."));
            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, localURL.replaceAll("#", "%23") + "." + action + ".do");
            try {
                String requestData = getRequestData();
                // Add parameters values to the request data to be sent.
                if (parameterData != null) {
                    requestData = requestData != null && requestData.length() > 0 ? (requestData + "&" + parameterData)
                            : parameterData;
                }
                if (requestData != null) {
                    builder.setHeader("Content-type", "application/x-www-form-urlencoded");
                }
                builder.setHeader("accept", "application/json");
                builder.sendRequest(requestData, new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                        com.google.gwt.user.client.Window.alert("Cannot create connection");
                        linker.loaded();
                        actionExecuted(500);
                    }

                    @SuppressWarnings("unchecked")
                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() != 200) {
                            com.google.gwt.user.client.Window.alert("Cannot contact remote server : error "+response.getStatusCode());
                        }
                        try {
                            JSONObject jsondata = JSONParser.parseStrict(response.getText()).isObject();
                            if (jsondata.get("refreshData") != null) {
                                JSONObject refreshData = jsondata.get("refreshData").isObject();
                                @SuppressWarnings("rawtypes")
                                Map data = new HashMap();
                                for (String s : refreshData.keySet()) {
                                    data.put(s, refreshData.get(s));
                                }
                                linker.refresh(data);
                            }
                            if (jsondata.get("messageDisplay") != null) {
                                JSONObject object = jsondata.get("messageDisplay").isObject();
                                String title = object.get("title").isString().stringValue();
                                String text = object.get("text").isString().stringValue();
                                String type = object.get("messageBoxType").isString().stringValue();
                                if ("alert".equals(type)) {
                                    MessageBox.alert(title, text, null);
                                } else {
                                    MessageBox.info(title, text, null);
                                }
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                        linker.loaded();
                        actionExecuted(response.getStatusCode());
                    }
                });


            } catch (RequestException e) {
                // Code omitted for clarity
            }
        }
    }

    protected void actionExecuted(int statusCode) {
        // do nothing
    }

    protected String getRequestData() {
        return null;
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();

        boolean enabled = lh.getMultipleSelection().size() > 0 && isNodeTypeAllowed(lh.getMultipleSelection());
        if (enabled && requiredNodeTypes != null && !requiredNodeTypes.isEmpty()) {
            for (GWTJahiaNode selected : lh.getMultipleSelection()) {
                if (!selected.isNodeType(requiredNodeTypes)) {
                    enabled = false;
                    break;
                }
            }
            
        }
        setEnabled(enabled);
        if (enabled) {
            super.handleNewLinkerSelection();
        }
    }

    public void setAction(String action) {
        this.action = action;
    }

	public void setConfirmationMessageKey(String confirmationMessageKey) {
    	this.confirmationMessageKey = confirmationMessageKey;
    }

    public void setRequiredNodeTypes(Set<String> requiredNodeTypes) {
        this.requiredNodeTypes = requiredNodeTypes;
    }

    /**
     * Set the parameter map to be used for an action.
     * 
     * @param parameters a map of action parameters
     */
    public void setParameters(Map<String, String> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                if (buffer.length() > 0) {
                    buffer.append("&");
                }
                buffer.append(param.getKey()).append("=").append(param.getValue());
            }
            parameterData = buffer.toString();
        }

    }
}

