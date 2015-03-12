/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
 * User: david
 * Date: Apr 28, 2010
 * Time: 2:26:32 PM
 * 
 */
public class ExecuteActionItem extends NodeTypeAwareBaseActionItem {
    private static final long serialVersionUID = -1317342305404063292L;
    public static final int STATUS_CODE_OK = 200;
    protected String action;
    protected String confirmationMessageKey;
    private Set<String> requiredNodeTypes;

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
            String localURL = baseURL + "/default/" + JahiaGWTParameters.getLanguage() + gwtJahiaNode.getPath();
            linker.loading(Messages.get("label.executing", "Executing action ..."));
            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, localURL + "." + action + ".do");
            try {
                String requestData = getRequestData();
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

                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() != 200) {
                            com.google.gwt.user.client.Window.alert("Cannot contact remote server : error "+response.getStatusCode());
                        }
                        try {
                            JSONObject refreshData = JSONParser.parseStrict(response.getText()).isObject().get("refreshData").isObject();
                            Map data = new HashMap();
                            for (String s : refreshData.keySet()) {
                                data.put(s, refreshData.get(s));
                            }
                            linker.refresh(data);
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
}

