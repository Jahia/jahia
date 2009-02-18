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

package org.jahia.ajax.gwt.client.widget.mysettings;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.RootPanel;

import org.jahia.ajax.gwt.client.data.GWTJahiaUserProperty;
import org.jahia.ajax.gwt.client.service.JahiaContentService;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.core.JahiaModule;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ktlili
 * Date: 4 sept. 2008
 * Time: 16:20:11
 */
public class MySettingsPanel extends AbsolutePanel {
    private JahiaModule jahiaModule;
    private List<GWTJahiaUserProperty> userPorperties;
    private RootPanel rootPanel;

    public MySettingsPanel(RootPanel rootPanel, JahiaModule jahiaModule) {
        this.jahiaModule = jahiaModule;
        this.rootPanel = rootPanel;
        init();
    }

    private void init() {
        JahiaContentService.App.getInstance().getJahiaUserProperties(true, new LoadUserPropertieyAsyncCallback(this));
    }

    /**
     * Refresh my Settings panel
     *
     * @param userPorpeties List of user properties retreived from the server
     */
    public void refresh(List<GWTJahiaUserProperty> userPorpeties) {
        this.userPorperties = userPorpeties;
        clear();
        // creat a form panel
        final FormPanel formPanel = new FormPanel();
        formPanel.setBodyBorder(false);
        formPanel.setFrame(false);
        formPanel.setAutoHeight(true);
        formPanel.setHeaderVisible(false);
        formPanel.setFieldWidth(210);
        formPanel.setLabelWidth(100);
        formPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        formPanel.setStyleAttribute("padding", "20");

        // add pwd field
        final TextField pwdField = new TextField();
        pwdField.setPassword(true);
        pwdField.setFieldLabel(getResource("pwd"));
        formPanel.add(pwdField);

        // add confirm pwd field
        final TextField confirmPwdField = new TextField();
        confirmPwdField.setPassword(true);
        confirmPwdField.setFieldLabel(getResource("c_pwd"));
        formPanel.add(confirmPwdField);

        // add a text fiedl for each property
        boolean customPropertiesPresent = false;
        if (userPorpeties != null) {
            for (GWTJahiaUserProperty gwtJahiaUserProperty : userPorpeties) {
                if ("checkbox".equals(gwtJahiaUserProperty.getDisplay())) {
                    final CheckBox checkBox = new CheckBox();
                    final GWTJahiaBasicDataBean data = gwtJahiaUserProperty.getValue();
                    checkBox.setValue(Boolean.parseBoolean(data.getValue()));
                    checkBox.setName(gwtJahiaUserProperty.getKey());
                    checkBox.setFieldLabel(gwtJahiaUserProperty.getLabel());
                    checkBox.setReadOnly(gwtJahiaUserProperty.isReadOnly());

                    // it means that its a custom properties
                    if (!gwtJahiaUserProperty.isJahiaMySettingsProperty()) {
                        customPropertiesPresent = true;
                    }
                    formPanel.add(checkBox);

                } else if ("selectbox".equals(gwtJahiaUserProperty.getDisplay())) {
                    final ListStore<GWTJahiaBasicDataBean> store = new ListStore<GWTJahiaBasicDataBean>();
                    store.add(gwtJahiaUserProperty.getValues());

                    final ComboBox<GWTJahiaBasicDataBean> select = new ComboBox<GWTJahiaBasicDataBean>();
                    select.setDisplayField(GWTJahiaBasicDataBean.DISPLAY_NAME);
                    select.setStore(store);
                    select.setTypeAhead(true);
                    select.setTriggerAction(ComboBox.TriggerAction.ALL);
                    select.setValue(gwtJahiaUserProperty.getValue());

                    select.setName(gwtJahiaUserProperty.getKey());
                    select.setFieldLabel(gwtJahiaUserProperty.getLabel());
                    select.setReadOnly(gwtJahiaUserProperty.isReadOnly());

                    // it means that its a custom properties
                    if (!gwtJahiaUserProperty.isJahiaMySettingsProperty()) {
                        customPropertiesPresent = true;
                    }
                    formPanel.add(select);

                } else {
                    // create a text field
                    final TextField<String> field = new TextField<String>();
                    final GWTJahiaBasicDataBean data = gwtJahiaUserProperty.getValue();
                    field.setValue(data.getValue());
                    if (gwtJahiaUserProperty.isPassword()) {
                        field.setPassword(true);
                    }

                    field.setName(gwtJahiaUserProperty.getKey());
                    field.setFieldLabel(gwtJahiaUserProperty.getLabel());
                    field.setReadOnly(gwtJahiaUserProperty.isReadOnly());

                    // it means that its a custom properties
                    if (!gwtJahiaUserProperty.isJahiaMySettingsProperty()) {
                        customPropertiesPresent = true;
                    }
                    formPanel.add(field);
                }
            }
        }

        // save property listener
        SelectionListener<ComponentEvent> saveJahiaUserPropertiesListener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {
                String pwd = pwdField.getRawValue();
                String confirmPwd = confirmPwdField.getRawValue();
                if (pwd.equalsIgnoreCase(confirmPwd)) {
                    List<GWTJahiaUserProperty> newJahiaUserProperties = new ArrayList<GWTJahiaUserProperty>();
                    List<GWTJahiaUserProperty> removeJahiaUserProperties = new ArrayList<GWTJahiaUserProperty>();
                    for (Field field : formPanel.getFields()) {
                        // ignore password confirmation field
                        if (field.getId().equals(confirmPwdField.getId())) {
                            continue;
                        }
                        GWTJahiaUserProperty jahiaUserProperty = new GWTJahiaUserProperty();
                        jahiaUserProperty.setKey(field.getName());
                        if (field instanceof ComboBox) {
                            jahiaUserProperty.setValue(((ComboBox<GWTJahiaBasicDataBean>)field).getValue());
                        } else {
                            jahiaUserProperty.setValue(new GWTJahiaBasicDataBean(field.getRawValue(), null));
                        }
                        if (field instanceof TextField) {
                            jahiaUserProperty.setPassword(((TextField) field).isPassword());
                        }
                        newJahiaUserProperties.add(jahiaUserProperty);
                    }

                    // update user properties
                    JahiaContentService.App.getInstance().updateJahiaUserProperties(newJahiaUserProperties, removeJahiaUserProperties, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            Log.error("Can't update jahia user properties", throwable);
                            Info.display("MessageBox", "Error: Unable to save properties. See logs for details");
                        }

                        public void onSuccess(Object o) {
                            Info.display("MessageBox", "Properties were succesfully saved");
                            GWTJahiaAjaxActionResult gwtAjaxActionResult = (GWTJahiaAjaxActionResult) o;
                            handleAjaxActionResult(gwtAjaxActionResult);
                        }
                    });
                } else {
                    MessageBox.alert(getResource("title_alert"), "'Password' and 'Confirm password' are different.", null);
                }
            }
        };

        // add personal property button
        Button addFieldButton = new Button(getResource("button_add_personal_property"));
        addFieldButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {
                createNewPropertyWindow();
            }
        });
        formPanel.addButton(addFieldButton);

        //  remove personnal property button
        Button removeFieldButton = new Button(getResource("button_remove_personal_property"));
        removeFieldButton.setEnabled(customPropertiesPresent);
        removeFieldButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {
                createRemovePropertiesWindow();
            }
        });
        formPanel.addButton(removeFieldButton);

        // save properties button
        Button saveButton = new Button(getResource("button_save"));
        saveButton.addSelectionListener(saveJahiaUserPropertiesListener);
        formPanel.addButton(saveButton);

        // remove all
        add(formPanel);
    }

    private String getResource(String key) {
        if (jahiaModule != null && rootPanel != null) {
            return jahiaModule.getResource(rootPanel.getElement().getId(), key);
        }
        return key;
    }

    /**
     * Handle ajax actin result
     *
     * @param gwtAjaxActionResult Result of the Ajax action
     */
    private void handleAjaxActionResult(GWTJahiaAjaxActionResult gwtAjaxActionResult) {
        if (gwtAjaxActionResult != null) {
            List<GWTJahiaAjaxActionResultError> gwtAjaxActionResultErrors = gwtAjaxActionResult.getErrors();
            if (gwtAjaxActionResultErrors != null && !gwtAjaxActionResultErrors.isEmpty()) {
                // display errors
                String errors = null;
                for (GWTJahiaAjaxActionResultError error : gwtAjaxActionResultErrors) {
                    if (errors == null) {
                        errors = error.getErrorMsg();
                    } else {
                        errors = errors + "<br/>" + error.getErrorMsg();
                    }
                }
                MessageBox.alert(getResource("alert"), errors, null);
            } else {
                if (gwtAjaxActionResult.getValue() != null && gwtAjaxActionResult.getValue().length() > 0) {
                    // no error, display message
                    Info.display(getResource("title_mysettings"), gwtAjaxActionResult.getValue());
                }
            }
        }
    }

    /**
     * Create 'add new property' window
     */
    private void createRemovePropertiesWindow() {
        final Window removePropertiesWindow = new Window();
        removePropertiesWindow.setHeading(getResource("title_remove_properties"));
        removePropertiesWindow.setBodyBorder(false);
        removePropertiesWindow.setInsetBorder(false);
        removePropertiesWindow.setWidth(300);


        // add a text fiedl for each property
        if (userPorperties != null) {
            final DataList list = new DataList();
            list.setWidth(280);
            list.setHeight(100);
            list.setScrollMode(Style.Scroll.AUTO);
            list.setSelectionMode(Style.SelectionMode.MULTI);
            for (GWTJahiaUserProperty gwtJahiaUserProperty : userPorperties) {
                if (!gwtJahiaUserProperty.isJahiaMySettingsProperty()) {
                    DataListItem item = new DataListItem();
                    item.setItemId(gwtJahiaUserProperty.getKey());
                    item.setText(gwtJahiaUserProperty.getKey());
                    list.add(item);
                }
            }

            removePropertiesWindow.add(list);

            // save button
            Button save = new Button(getResource("button_remove"));
            save.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    // update user properties
                    List<GWTJahiaUserProperty> userProperties = new ArrayList<GWTJahiaUserProperty>();

                    // get selected properties and add it to "old properties list"
                    List<DataListItem> dataListItems = list.getSelectedItems();
                    for (DataListItem dataListItem : dataListItems) {
                        GWTJahiaUserProperty jahiaUserProperty = new GWTJahiaUserProperty();
                        jahiaUserProperty.setKey(dataListItem.getItemId());
                        userProperties.add(jahiaUserProperty);
                    }

                    Log.debug("Number of properties to: " + userProperties.size());
                    // apply modification
                    JahiaContentService.App.getInstance().updateJahiaUserProperties(new ArrayList<GWTJahiaUserProperty>(), userProperties, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            removePropertiesWindow.hide();
                            Log.error("Can't update jahia user properties", throwable);
                            Info.display("MessageBox", "Error: Unable to save properties. See logs for details");
                        }

                        public void onSuccess(Object o) {
                            Info.display("MessageBox", "Properties were succesfully saved");
                            removePropertiesWindow.hide();
                            GWTJahiaAjaxActionResult gwtAjaxActionResult = (GWTJahiaAjaxActionResult) o;
                            handleAjaxActionResult(gwtAjaxActionResult);
                            JahiaContentService.App.getInstance().getJahiaUserProperties(true, new LoadUserPropertieyAsyncCallback(MySettingsPanel.this));
                        }
                    });

                }
            });
            removePropertiesWindow.addButton(save);

        }
        removePropertiesWindow.show();
    }

    /**
     * Create 'add new property' window
     */
    private void createNewPropertyWindow() {
        final Window newFieldWindow = new Window();
        newFieldWindow.setHeading(getResource("title_new_property"));
        newFieldWindow.setBodyBorder(false);
        newFieldWindow.setInsetBorder(false);
        newFieldWindow.setWidth(550);

        FormPanel f = new FormPanel();
        f.setFieldWidth(210);
        f.setLabelWidth(100);
        f.setHeaderVisible(false);
        f.setBodyBorder(false);
        // label fiels
        final TextField labelField = new TextField();
        labelField.setFieldLabel(getResource("label_fieldname_new_property"));
        f.add(labelField);

        // value field
        final TextField valueField = new TextField();
        valueField.setFieldLabel(getResource("label_fieldvalue_new_property"));
        f.add(valueField);

        // save button
        Button save = new Button(getResource("button_save"));
        save.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                // update user properties
                List<GWTJahiaUserProperty> userProperties = new ArrayList<GWTJahiaUserProperty>();
                GWTJahiaUserProperty jahiaUserProperty = new GWTJahiaUserProperty();
                jahiaUserProperty.setKey(labelField.getRawValue());
                final GWTJahiaBasicDataBean data = new GWTJahiaBasicDataBean();
                data.setValue(valueField.getRawValue());
                jahiaUserProperty.setValue(data);
                userProperties.add(jahiaUserProperty);
                JahiaContentService.App.getInstance().updateJahiaUserProperties(userProperties, new ArrayList<GWTJahiaUserProperty>(), new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        newFieldWindow.hide();
                        Log.error("Can't update jahia user properties", throwable);
                        Info.display("MessageBox", "Error: Unable to save properties. See logs for details");
                    }

                    public void onSuccess(Object o) {
                        Log.debug("User properties updated");
                        Info.display("MessageBox", "Properties were succesfully saved");
                        newFieldWindow.hide();
                        GWTJahiaAjaxActionResult gwtAjaxActionResult = (GWTJahiaAjaxActionResult) o;
                        handleAjaxActionResult(gwtAjaxActionResult);
                        JahiaContentService.App.getInstance().getJahiaUserProperties(true, new LoadUserPropertieyAsyncCallback(MySettingsPanel.this));
                    }
                });

            }
        });
        f.addButton(save);
        newFieldWindow.add(f);
        newFieldWindow.show();
    }


    /**
     * Asyncall that load user properties
     */
    private class LoadUserPropertieyAsyncCallback implements AsyncCallback {
        private MySettingsPanel mySettingsPanel;

        private LoadUserPropertieyAsyncCallback(MySettingsPanel mySettingsPanel) {
            this.mySettingsPanel = mySettingsPanel;
        }

        public void onFailure(Throwable throwable) {
            Log.error("Can't retrieve jahia user properties", throwable);
        }

        public void onSuccess(Object o) {
            // load user properties
            mySettingsPanel.refresh((List<GWTJahiaUserProperty>) o);
        }
    }
}
