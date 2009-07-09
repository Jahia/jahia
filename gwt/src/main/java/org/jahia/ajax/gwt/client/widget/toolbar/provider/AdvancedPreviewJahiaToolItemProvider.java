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
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.service.SessionManagerService;
import org.jahia.ajax.gwt.client.service.SessionManagerServiceAsync;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupSelect;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jahia
 * Date: 11 juil. 2008
 * Time: 10:19:39
 */
public class AdvancedPreviewJahiaToolItemProvider extends AbstractJahiaToolItemProvider {

    SessionManagerServiceAsync sessionServ = SessionManagerService.App.getInstance();

    private Window window = new Window();
    private CheckBox enabledCheckBox;
    private TriggerField<String> aliasingName;
    private CalendarField previewDateField;
    private FormPanel panel = new FormPanel();
    private Button save;
    private Button cancel;

    public AdvancedPreviewJahiaToolItemProvider(){
        window = new Window();
        window.setMaximizable(true);
        window.setAutoHeight(true);
        window.setWidth(500);

        panel.setHeaderVisible(false);
        panel.setFieldWidth(210);
        panel.setLabelWidth(200);
        panel.setLabelAlign(FormPanel.LabelAlign.LEFT);

        boolean enabled = "true".equals(JahiaGWTParameters.getEnableAdvPreviewSettings())
                || (JahiaGWTParameters.getEnableAdvPreviewSettings() == null);
        String previewDate = JahiaGWTParameters.getPreviewDate();

        enabledCheckBox = new CheckBox();
        enabledCheckBox.setValue(enabled);
        enabledCheckBox.setFieldLabel(Messages.getNotEmptyResource("advp_enable",
                            "Enable advanced preview settings"));
        panel.add(enabledCheckBox);

        aliasingName = new TriggerField<String>() {

            @Override
            protected void onTriggerClick(ComponentEvent ce) {
                super.onTriggerClick(ce);
                new UserGroupSelect(new UserGroupAdder() {
                    public void addUsers(List<GWTJahiaUser> users) {
                        if (!users.isEmpty()) {
                            setValue(users.get(0).getUsername());
                        }
                    }

                    public void addGroups(List<GWTJahiaGroup> groups) {
                        // we are not interested in groups
                    }
                }, UserGroupSelect.VIEW_USERS, "currentSite", true);
            }
        };
        aliasingName.setTriggerStyle("um-user");

        // principal key
        aliasingName.setFieldLabel(Messages.getNotEmptyResource("advp_username","Preview content for user"));
        aliasingName.setValue(JahiaGWTParameters.getCurrentUser());
        aliasingName.setAllowBlank(true);
        panel.add(aliasingName);

        previewDateField = new CalendarField();
        previewDateField.setFieldLabel(Messages.getNotEmptyResource("advp_dateofpreview","Date of preview"));

        if (previewDate != null && !"".equals(previewDate.trim())){
            previewDateField.setValue(new Date(Long.parseLong(previewDate)));
        }
        panel.add(previewDateField);
        window.add(panel);
    }

    /**
     * Executed when the item is clicked
     *
     * @param gwtToolbarItem
     * @return
     */
    public <T extends ComponentEvent> SelectionListener<T> getSelectListener(final GWTJahiaToolbarItem gwtToolbarItem) {
        SelectionListener<T> listener = new SelectionListener<T>() {
            public void componentSelected(T event) {

                window.setHeading(gwtToolbarItem.getTitle());

                if (save == null){
                    save = new Button(Messages.getNotEmptyResource("save","Save"));
                    save.addSelectionListener(new SelectionListener<ButtonEvent>() {


                        public void componentSelected(ButtonEvent event) {
                            Log.debug("*****  advanced item clicked:" + enabledCheckBox.isEnabled() + ","
                                    +previewDateField.getRawValue() + "," + aliasingName.getRawValue());
                            Map<String, GWTJahiaProperty> properties = gwtToolbarItem.getProperties();
                            if (properties == null) {
                                properties = new HashMap<String, GWTJahiaProperty>();
                            }
                            GWTJahiaProperty property = new GWTJahiaProperty();
                            property.setName(ToolbarConstants.CLASS_ACTION);
                            property.setValue("org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl.PrincipalAliasingAjaxActionImpl");
                            properties.put(ToolbarConstants.CLASS_ACTION, property);

                            // enabled option
                            property = new GWTJahiaProperty();
                            property.setName("enabled");
                            property.setValue(enabledCheckBox.getValue().toString());
                            properties.put("enabled", property);

                            // principal key
                            String selectedAliasingName = (String)aliasingName.getValue();
                            if (selectedAliasingName == null || "".equals(selectedAliasingName)){
                                selectedAliasingName = JahiaGWTParameters.getCurrentUser();
                            }

                            aliasingName.setValue(selectedAliasingName);

                            property = new GWTJahiaProperty();
                            property.setName("principalKey");
                            property.setValue((String)aliasingName.getValue());
                            properties.put("principalKey", property);

                            // date
                            property = new GWTJahiaProperty();
                            property.setName("date");
                            Date dateValue = previewDateField.getValue();
                            if (dateValue != null){
                                property.setValue(String.valueOf(dateValue.getTime()));
                                properties.put("date", property);
                            }

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
                                        // @todo: display errors from action result
                                        window.hide();
                                    }
                                }
                            });
                        }
                    });
                    cancel = new Button(Messages.getNotEmptyResource("cancel","Cancel"));
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
                        alertMsg.setMessage("Failed to retrieve jahia gwt context " +  "\n\n" + throwable) ;
                    }
                    public void onSuccess(GWTJahiaContext context) {
                        if (context != null) {
                            gwtContext.setAdvPreviewSettings(context.getAdvPreviewSettings());
                            if ( gwtContext.getAdvPreviewSettings() != null ){
                                if (enabledCheckBox != null){
                                    enabledCheckBox.setValue(gwtContext.getAdvPreviewSettings().isEnabled());
                                }
                                if ( gwtContext.getAdvPreviewSettings().getAliasedUser() != null ){
                                    if (aliasingName != null){
                                        aliasingName.setValue(gwtContext.getAdvPreviewSettings().getAliasedUser()
                                                .getUsername());
                                    }
                                }
                                if ( gwtContext.getAdvPreviewSettings().getPreviewDate() != 0 ){
                                    if (previewDateField != null){
                                        previewDateField.setValue(new Date(gwtContext.getAdvPreviewSettings().getPreviewDate()));
                                    }
                                }
                            }
                            window.show();
                        }
                    }}
                );
            }
        };
        return listener;
    }

    /**
     * Create a new toolItem
     *
     * @param gwtToolbarItem
     * @return
     */
    public Component createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        return new Button();
    }


}
