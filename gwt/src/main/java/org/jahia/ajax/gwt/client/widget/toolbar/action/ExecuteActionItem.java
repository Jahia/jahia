/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.http.client.*;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.List;
import java.util.Set;

/**
 * User: david
 * Date: Apr 28, 2010
 * Time: 2:26:32 PM
 * 
 */
public class ExecuteActionItem extends BaseActionItem {
    private static final long serialVersionUID = -1317342305404063292L;
	public static final int STATUS_CODE_OK = 200;
    private String action;
    private String confirmationMessageKey;
    private Set<String> requiredNodeTypes;

    public void onComponentSelection() {
		if (confirmationMessageKey != null) {
			MessageBox.confirm(
			        Messages.get("label.information", "Information"),
			        Messages.get(confirmationMessageKey, "You are about to execute action "
			                + action + ". Do you want to continue?"),
			        new Listener<MessageBoxEvent>() {
				        public void handleEvent(MessageBoxEvent be) {
					        if (Dialog.YES.equalsIgnoreCase(be.getButtonClicked().getText())) {
						        doAction();
					        }
				        }
			        });
		} else {
			doAction();
		}
    }

    private void doAction() {
        final List<GWTJahiaNode> gwtJahiaNodes = linker.getSelectionContext().getMultipleSelection();
        for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
            String baseURL = org.jahia.ajax.gwt.client.util.URL.getAbsoluteURL(JahiaGWTParameters.getContextPath() + "/cms/render");
            String localURL = baseURL + "/default/" + JahiaGWTParameters.getLanguage() + gwtJahiaNode.getPath();
            linker.loading("Executing action ...");
            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, localURL + "." + action + ".do");
            try {
                builder.sendRequest(null, new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                        com.google.gwt.user.client.Window.alert("Cannot create connection");
                        linker.loaded();
                    }

                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() != 200) {
                            com.google.gwt.user.client.Window.alert("Cannot contact remote server : error "+response.getStatusCode());
                        }
                        linker.loaded();
                    }
                });


            } catch (RequestException e) {
                // Code omitted for clarity
            }
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();

        boolean enabled = lh.getMultipleSelection().size() > 0;
        if (enabled && requiredNodeTypes != null && !requiredNodeTypes.isEmpty()) {
            for (GWTJahiaNode selected : lh.getMultipleSelection()) {
                if (!selected.isNodeType(requiredNodeTypes)) {
                    enabled = false;
                    break;
                }
            }
            
        }
        setEnabled(enabled);
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

