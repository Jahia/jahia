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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import java.util.HashMap;
import java.util.Map;

import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.GWTJahiaRevision;
import org.jahia.ajax.gwt.client.service.SessionManagerService;
import org.jahia.ajax.gwt.client.service.SessionManagerServiceAsync;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.widget.dialog.ErrorDialog;
import org.jahia.ajax.gwt.client.widget.versioning.VersionField;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.widget.versioning.VersionFieldPanel;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaContext;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 */
public class AdvancedCompareModeActionItem extends BaseActionItem {

    SessionManagerServiceAsync sessionServ = SessionManagerService.App.getInstance();

    private Window window;
    private CheckBox enabledCheckBox;
    private VersionFieldPanel versionFieldPanel1;
    private VersionFieldPanel versionFieldPanel2;
    private VersionField versionField1;
    private VersionField versionField2;
    private FormPanel panel;
    private Button save;
    private Button cancel;


    @Override
    public void onSelection() {
        if (window == null) {
            window = new Window();
            window.setMaximizable(true);
            window.setAutoHeight(true);
            window.setWidth(500);
            panel = new FormPanel();
            panel.setHeaderVisible(false);
            panel.setFieldWidth(210);
            panel.setLabelWidth(200);
            panel.setLabelAlign(FormPanel.LabelAlign.LEFT);

            boolean enabled = true;

            enabledCheckBox = new CheckBox();
            enabledCheckBox.setValue(enabled);
            enabledCheckBox.setFieldLabel(Messages.getNotEmptyResource("advc_enable",
                    "Enable advanced compare mode settings"));
            panel.add(enabledCheckBox);

            versionField1 = new VersionField(getJahiaGWTPageContext().getPid(), null, null, false);
            versionField1.setFieldLabel(Messages.getNotEmptyResource("advc_version1",
                    "Version 1"));
            versionFieldPanel1 = new VersionFieldPanel(versionField1);

            versionField2 = new VersionField(getJahiaGWTPageContext().getPid(), null, null, false);
            versionField2.setFieldLabel(Messages.getNotEmptyResource("advc_version2",
                    "Version 2"));
            versionFieldPanel2 = new VersionFieldPanel(versionField2);

            AdapterField fieldAdapter = new AdapterField(versionFieldPanel1);
            fieldAdapter.setFieldLabel(versionField1.getFieldLabel());
            panel.add(fieldAdapter);
            fieldAdapter = new AdapterField(versionFieldPanel2);
            fieldAdapter.setFieldLabel(versionField2.getFieldLabel());
            panel.add(fieldAdapter);

            window.add(panel);

            window.setHeading(getGwtToolbarItem().getTitle());
        }
        if (save == null) {
            save = new Button(Messages.getNotEmptyResource("save", "Save"));
            save.addSelectionListener(new SelectionListener<ButtonEvent>() {


                public void componentSelected(ButtonEvent event) {
                    Log.debug("*****  advanced item clicked:" + enabledCheckBox.isEnabled() + ","
                            + versionField1.getRawValue() + "," + versionField2.getRawValue());
                    Map<String, GWTJahiaProperty> properties = getGwtToolbarItem().getProperties();
                    if (properties == null) {
                        properties = new HashMap<String, GWTJahiaProperty>();
                    }
                    GWTJahiaProperty property = new GWTJahiaProperty();
                    property.setName(ToolbarConstants.CLASS_ACTION);
                    property.setValue("org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl.AdvCompareModeAjaxActionImpl");
                    properties.put(ToolbarConstants.CLASS_ACTION, property);

                    // enabled option
                    property = new GWTJahiaProperty();
                    property.setName("enabled");
                    property.setValue(enabledCheckBox.getValue().toString());
                    properties.put("enabled", property);

                    // version 1
                    storeSettings(versionField1, properties, "version1");

                    // version 2
                    storeSettings(versionField2, properties, "version2");

                    // execute
                    ToolbarService.App.getInstance().execute(getJahiaGWTPageContext(), properties, new AsyncCallback<GWTJahiaAjaxActionResult>() {
                        public void onFailure(Throwable throwable) {
                            window.hide();
                        }

                        public void onSuccess(GWTJahiaAjaxActionResult result) {
                            if (result != null && result.getErrors().isEmpty() && result.getValue() != null
                                    && !"".equals(result.getValue().trim())) {
                                com.google.gwt.user.client.Window.Location.replace(result.getValue());
                            } else {
                                if (result != null && !result.getErrors().isEmpty()) {
                                    final ErrorDialog errorDialog = new ErrorDialog(result.getErrors());
                                    errorDialog.show();
                                } else {
                                    window.hide();
                                }
                            }
                        }
                    });
                }

                private void storeSettings(VersionField versionField, Map<String,
                        GWTJahiaProperty> properties, String propName) {
                    GWTJahiaRevision revision =
                            new GWTJahiaRevision();
                    revision.setName(propName);
                    if (versionField.getSelectedDate() != null) {
                        revision.setDate(versionField.getSelectedDate().getTime());
                    }
                    if (versionField.getSelectedVersion() != null) {
                        revision.setVersion(versionField.getSelectedVersion());
                    }
                    revision.setUseVersion(versionField.isUseVersion());
                    properties.put(propName, revision);
                }
            });

            cancel = new Button(Messages.getNotEmptyResource("cancel", "Cancel"));
            cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    window.hide();
                }
            });

            panel.addButton(save);
            panel.addButton(cancel);
        }
        window.recalculate();

        final GWTJahiaContext gwtContext = new GWTJahiaContext();
        final MessageBox alertMsg = new MessageBox();
        sessionServ.getCoreSessionContext(new AsyncCallback<GWTJahiaContext>() {
            public void onFailure(Throwable throwable) {
                alertMsg.setMessage("Failed to retrieve jahia gwt context " + "\n\n" + throwable);
            }

            public void onSuccess(GWTJahiaContext context) {
                if (context != null) {
                    gwtContext.setAdvCompareModeSettings(context.getAdvCompareModeSettings());
                    if (gwtContext.getAdvCompareModeSettings() != null) {
                        if (enabledCheckBox != null) {
                            enabledCheckBox.setValue(gwtContext.getAdvCompareModeSettings().isEnabled());
                        }
                        if (versionFieldPanel1 != null) {
                            versionFieldPanel1.applyRevisionValuesToVersionField(
                                    gwtContext.getAdvCompareModeSettings().getRevision1());
                        }
                        if (versionFieldPanel2 != null) {
                            versionFieldPanel2.applyRevisionValuesToVersionField(
                                    gwtContext.getAdvCompareModeSettings().getRevision2());
                        }
                    }
                    window.show();
                }
            }
        }
        );
    }


}