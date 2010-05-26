/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.form;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.gwt.http.client.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 27 f�vr. 2009
 * Time: 16:43:07
 * To change this template use File | Settings | File Templates.
 */
public abstract class FormQuickRemotePublication extends FormPanel {
    private String folderPath;

    public FormQuickRemotePublication(String folderPath) {
        this.folderPath = folderPath;
        createUI();
    }

    protected void createUI() {
        setBodyBorder(false);
        setFrame(false);
        setAutoHeight(true);
        setHeaderVisible(false);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setStyleAttribute("padding", "4");


        final TextField nameField = new TextField();
        nameField.setName("name");
        nameField.setFieldLabel(Messages.getNotEmptyResource("name", "Name"));
        nameField.setAllowBlank(false);
        nameField.setMaxLength(200);
        add(nameField);

        final TextField remoteUrlField = new TextField();
        remoteUrlField.setName("remoteUrl");
        remoteUrlField.setEmptyText("http://www.targetsite.com/cms");
        remoteUrlField.setValidator(new Validator() {
            public String validate(Field<?> field, String s) {
                if (s.startsWith("http") && s.contains("://") && !s.endsWith("/")) {
                    return null;
                } else {
                    return "Remote URL should match format: http(s)://www.targetsite.com/cms";
                }
            }
        });
        remoteUrlField.setFieldLabel(Messages.getNotEmptyResource("remoteUrl", "Remote URL"));
        remoteUrlField.setAllowBlank(false);
        add(remoteUrlField);

        final TextField remotePath = new TextField();
        remotePath.setName("remotePath");
        remotePath.setEmptyText("/sites/targetSite");
        remotePath.setValidator(new Validator() {
            public String validate(Field<?> field, String s) {
                if (s.startsWith("/")) {
                    return null;
                } else {
                    return "Remote Path should be an absolut path to a node on the distant server";
                }
            }
        });
        remotePath.setFieldLabel(Messages.getNotEmptyResource("remotePath", "Remote Path"));
        remotePath.setAllowBlank(false);
        add(remotePath);

        final TextField remoteUser = new TextField();
        remoteUser.setName("remoteUser");
        remoteUser.setFieldLabel(Messages.getNotEmptyResource("remoteUser", "Remote User"));
        remoteUser.setAllowBlank(false);
        add(remoteUser);

        final TextField remotePassword = new TextField();
        remotePassword.setPassword(true);
        remotePassword.setName("remotePassword");
        remotePassword.setFieldLabel(Messages.getNotEmptyResource("remotePassword", "Remote Password"));
        remotePassword.setAllowBlank(false);
        add(remotePassword);

        final ContentPickerField localPath = new ContentPickerField(Messages.get("label.pagePicker", "Page picker"),
                Messages.get("picker_link_selection", "Selected page"), null, "/", null, null,
                ManagerConfigurationFactory.CONTENTPICKER, false);
        localPath.setName("node");
        localPath.setFieldLabel(Messages.getNotEmptyResource("node", "Node"));
        add(localPath);


        // save properties button
        Button saveButton = new Button(Messages.getResource("label.save"));
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent componentEvent) {

                String url = remoteUrlField.getValue().toString() + "/render/live/" +
                        localPath.getValue().get(0).getLanguageCode() + remotePath.getValue().toString();
                RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url + ".preparereplay.do");
                builder.setUser(remoteUser.getValue().toString());
                builder.setPassword(remotePassword.getValue().toString());
                try {
                    Request response = builder.sendRequest("testOnly=true", new RequestCallback() {
                        public void onError(Request request, Throwable exception) {
                            com.google.gwt.user.client.Window.alert("Cannot create connection");
                        }

                        public void onResponseReceived(Request request, Response response) {
                            if (response.getStatusCode() == 200) {
                                final Map<String, List<GWTJahiaNodeProperty>> langCodeProperties =
                                        new HashMap<String, List<GWTJahiaNodeProperty>>();
                                List<GWTJahiaNodeProperty> gwtJahiaNodeProperties =
                                        new ArrayList<GWTJahiaNodeProperty>();
                                gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("remoteUrl",
                                        new GWTJahiaNodePropertyValue(remoteUrlField.getValue().toString(),
                                                GWTJahiaNodePropertyType.STRING)));
                                gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("remotePath",
                                        new GWTJahiaNodePropertyValue(remotePath.getValue().toString(),
                                                GWTJahiaNodePropertyType.STRING)));
                                gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("remoteUser",
                                        new GWTJahiaNodePropertyValue(remoteUser.getValue().toString(),
                                                GWTJahiaNodePropertyType.STRING)));
                                gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("remotePassword",
                                        new GWTJahiaNodePropertyValue(remotePassword.getValue().toString(),
                                                GWTJahiaNodePropertyType.STRING)));
                                gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("node",
                                        new GWTJahiaNodePropertyValue(localPath.getValue().get(0).getUUID(),
                                                GWTJahiaNodePropertyType.STRING)));

                                final JahiaContentManagementServiceAsync service =
                                        JahiaContentManagementService.App.getInstance();
                                service.createNode("/remotePublications", nameField.getValue().toString(),
                                        "jnt:remotePublication", null, null, gwtJahiaNodeProperties, langCodeProperties,
                                        new BaseAsyncCallback<GWTJahiaNode>() {
                                            public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                                                if (getParent() instanceof Window) {
                                                    ((Window) getParent()).close();
                                                }
                                                onRemotePublicationCreated();
                                            }

                                            public void onApplicationFailure(Throwable throwable) {
                                                Log.error("Unable to create a remote publication", throwable);
                                                if (getParent() instanceof Window) {
                                                    ((Window) getParent()).hide();
                                                }
                                            }
                                        });
                            } else {
                                com.google.gwt.user.client.Window
                                        .alert("Cannot contact remote server : error " + response.getStatusCode());
                            }
                        }
                    });
                } catch (RequestException e) {
                    // Code omitted for clarity
                }
            }
        });
        addButton(saveButton);

        // remove all

        layout();
    }

    public abstract void onRemotePublicationCreated();

}