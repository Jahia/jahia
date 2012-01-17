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

package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.content.CronField;

import java.util.*;

/**
 * Form panel component for creating a remote publishing configuration. 
 */
public abstract class FormQuickRemotePublication extends FormPanel {

	public FormQuickRemotePublication() {
		super();
    	createUI();
	}

    protected void createUI() {
    	setLabelWidth(130);
    	setFieldWidth(500);
        setBodyBorder(false);
        setFrame(false);
        setAutoHeight(true);
        setHeaderVisible(false);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setStyleAttribute("padding", "4");


        final TextField<String> nameField = new TextField<String>();
        nameField.setName("name");
        nameField.setFieldLabel(Messages.get("label.name", "Name"));
        nameField.setAllowBlank(false);
        nameField.setMaxLength(200);
        add(nameField);

        final ComboBox<GWTJahiaNode> localPath = new ComboBox<GWTJahiaNode>();
        final ListStore<GWTJahiaNode> store = new ListStore<GWTJahiaNode>();

        JahiaContentManagementService.App.getInstance().getRoot(Arrays.asList("/sites/*"), Arrays.asList("jnt:virtualsite"), null, null,null,null,null, false, false, null, null, new BaseAsyncCallback<List<GWTJahiaNode>>() {
            public void onSuccess(List<GWTJahiaNode> result) {
                store.add(result);
            }
        });
        localPath.setStore(store);
        localPath.setDisplayField("displayName");
        localPath.setValueField("uuid");
        localPath.setTypeAhead(true);
        localPath.setTriggerAction(ComboBox.TriggerAction.ALL);
        localPath.setForceSelection(true);

        localPath.setName("node");
        localPath.setFieldLabel(Messages.get("label.source", "Source"));
        add(localPath);

        final TextField<String> remoteUrlField = new TextField<String>();
        remoteUrlField.setName("remoteUrl");
        remoteUrlField.setEmptyText("http://www.target-acme-site.com");
        remoteUrlField.setFieldLabel(Messages.get("label.remoteUrl", "Target server URL"));
        remoteUrlField.setAllowBlank(false);
        add(remoteUrlField);

        final TextField<String> remotePath = new TextField<String>();
        remotePath.setName("remotePath");
        remotePath.setEmptyText("/sites/targetSite");
        remotePath.setValidator(new Validator() {
            public String validate(Field<?> field, String s) {
                if (s.startsWith("/")) {
                    return null;
                } else {
					return Messages
							.get("failure.remotePublication.remotePath.invalid",
									"Remote path should be an absolute path to a node on the distant server");
                }
            }
        });
        remotePath.setFieldLabel(Messages.get("label.remotePath", "Remote path"));
        remotePath.setAllowBlank(false);
        add(remotePath);

        final TextField<String> remoteUser = new TextField<String>();
        remoteUser.setName("remoteUser");
        remoteUser.setFieldLabel(Messages.get("label.remoteUser", "Remote user"));
        remoteUser.setAllowBlank(false);
        add(remoteUser);

        final TextField<String> remotePassword = new TextField<String>();
        remotePassword.setPassword(true);
        remotePassword.setName("remotePassword");
        remotePassword.setFieldLabel(Messages.get("label.remotePassword", "Remote password"));
        remotePassword.setAllowBlank(false);
        add(remotePassword);

        final CronField schedule = new CronField();
        schedule.setName("schedule");
        schedule.setFieldLabel(Messages.get("label.remoteSchedule", "Schedule"));
        add(schedule);

        final CheckBox doValidate = new CheckBox();
        doValidate.setFieldLabel(Messages.get("label.remoteValidate", "Test settings"));
        doValidate.setValue(Boolean.TRUE);
        add(doValidate);


        // save properties button
        Button saveButton = new Button(Messages.get("label.save", "Save"));
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent componentEvent) {

				Map<String, String> props = new HashMap<String, String>();
				props.put("remoteUrl", remoteUrlField.getValue());
				props.put("remotePath", remotePath.getValue());
				props.put("remoteUser", remoteUser.getValue());
				props.put("remotePassword", remotePassword.getValue());
				props.put("node", localPath.getValue().getUUID());
				props.put("schedule", schedule.getValue());
            	
				JahiaContentManagementService.App.getInstance().createRemotePublication(nameField.getValue(), props, doValidate.getValue(), new BaseAsyncCallback<Boolean>() {
					public void onSuccess(Boolean result) {
	                    if (getParent() instanceof Window) {
	                        ((Window) getParent()).hide();
	                    }
	                    onRemotePublicationCreated();
					}
					
					@Override
					public void onApplicationFailure(Throwable caught) {
						MessageBox.alert(Messages.get("label.error", "Error"), caught.getMessage(), null);
					}
				});
            }
        });
        addButton(saveButton);

        // remove all

        layout();
    }

    public abstract void onRemotePublicationCreated();
}