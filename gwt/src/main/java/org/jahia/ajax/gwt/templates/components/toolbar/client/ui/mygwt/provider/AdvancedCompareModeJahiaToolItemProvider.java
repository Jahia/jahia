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

package org.jahia.ajax.gwt.templates.components.toolbar.client.ui.mygwt.provider;

import java.util.HashMap;
import java.util.Map;

import org.jahia.ajax.gwt.commons.client.beans.GWTAjaxActionResult;
import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaContext;
import org.jahia.ajax.gwt.commons.client.beans.GWTProperty;
import org.jahia.ajax.gwt.commons.client.beans.GWTRevision;
import org.jahia.ajax.gwt.commons.client.rpc.SessionManagerService;
import org.jahia.ajax.gwt.commons.client.rpc.SessionManagerServiceAsync;
import org.jahia.ajax.gwt.commons.client.ui.dialog.ErrorDialog;
import org.jahia.ajax.gwt.commons.client.util.ResourceBundle;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.engines.versioning.client.form.VersionField;
import org.jahia.ajax.gwt.engines.versioning.client.form.VersionFieldPanel;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbarItem;
import org.jahia.ajax.gwt.templates.components.toolbar.client.service.ToolbarService;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.Constants;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 */
public class AdvancedCompareModeJahiaToolItemProvider extends AbstractJahiaToolItemProvider {

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
    private GWTJahiaPageContext pageContext;
    public AdvancedCompareModeJahiaToolItemProvider(){
        pageContext = this.getJahiaGWTPageContext();
   }

    /**
     * Executed when the item is clicked
     *
     * @param gwtToolbarItem
     * @return
     */
    public SelectionListener<ComponentEvent> getSelectListener(final GWTToolbarItem gwtToolbarItem) {
        SelectionListener listener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {

                if (window == null){
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
                    enabledCheckBox.setFieldLabel(ResourceBundle.getNotEmptyResource("advc_enable",
                            "Enable advanced compare mode settings"));
                    panel.add(enabledCheckBox);

                    versionField1 = new VersionField(pageContext.getPid(),null,null,false);
                    versionField1.setFieldLabel(ResourceBundle.getNotEmptyResource("advc_version1",
                            "Version 1"));
                    versionFieldPanel1 = new VersionFieldPanel(versionField1);

                    versionField2 = new VersionField(pageContext.getPid(),null,null,false);
                    versionField2.setFieldLabel(ResourceBundle.getNotEmptyResource("advc_version2",
                            "Version 2"));
                    versionFieldPanel2 = new VersionFieldPanel(versionField2);

                    AdapterField fieldAdapter = new AdapterField(versionFieldPanel1);
                    fieldAdapter.setFieldLabel(versionField1.getFieldLabel());
                    panel.add(fieldAdapter);
                    fieldAdapter = new AdapterField(versionFieldPanel2);
                    fieldAdapter.setFieldLabel(versionField2.getFieldLabel());
                    panel.add(fieldAdapter);

                    window.add(panel);

                    window.setHeading(gwtToolbarItem.getTitle());
                }
                if (save == null){
                    save = new Button(ResourceBundle.getNotEmptyResource("save","Save"));
                    save.addSelectionListener(new SelectionListener<ComponentEvent>() {


                        public void componentSelected(ComponentEvent event) {
                            Log.debug("*****  advanced item clicked:" + enabledCheckBox.isEnabled() + ","
                                    +versionField1.getRawValue() + "," + versionField2.getRawValue());
                            Map<String, GWTProperty> properties = gwtToolbarItem.getProperties();
                            if (properties == null) {
                                properties = new HashMap<String, GWTProperty>();
                            }
                            GWTProperty property = new GWTProperty();
                            property.setName(Constants.CLASS_ACTION);
                            property.setValue("org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl.AdvCompareModeAjaxActionImpl");
                            properties.put(Constants.CLASS_ACTION, property);

                            // enabled option
                            property = new GWTProperty();
                            property.setName("enabled");
                            property.setValue(enabledCheckBox.getValue().toString());
                            properties.put("enabled", property);

                            // version 1
                            storeSettings(versionField1,properties,"version1");

                            // version 2
                            storeSettings(versionField2,properties,"version2");

                            // execute
                            ToolbarService.App.getInstance().execute(getJahiaGWTPageContext(), properties, new AsyncCallback<GWTAjaxActionResult>() {
                                public void onFailure(Throwable throwable) {
                                    window.hide();
                                }

                                public void onSuccess(GWTAjaxActionResult result) {
                                    if (result != null && result.getErrors().isEmpty() && result.getValue() != null
                                            && !"".equals(result.getValue().trim())) {
                                        com.google.gwt.user.client.Window.Location.replace(result.getValue());
                                    } else {
                                        if (result != null && !result.getErrors().isEmpty()){
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
                                GWTProperty> properties, String propName){
                            GWTRevision revision =
                                    new GWTRevision();
                            revision.setName(propName);
                            if (versionField.getSelectedDate() != null){
                                revision.setDate(versionField.getSelectedDate().getTime());
                            }
                            if (versionField.getSelectedVersion() != null){
                                revision.setVersion(versionField.getSelectedVersion());
                            }
                            revision.setUseVersion(versionField.isUseVersion());
                            properties.put(propName, revision);
                        }
                    });

                    cancel = new Button(ResourceBundle.getNotEmptyResource("cancel","Cancel"));
                    cancel.addSelectionListener(new SelectionListener<ComponentEvent>() {
                        public void componentSelected(ComponentEvent event) {
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
                            gwtContext.setAdvCompareModeSettings(context.getAdvCompareModeSettings());
                            if ( gwtContext.getAdvCompareModeSettings() != null ){
                                if (enabledCheckBox != null){
                                    enabledCheckBox.setValue(gwtContext.getAdvCompareModeSettings().isEnabled());
                                }
                                if (versionFieldPanel1 != null){
                                    versionFieldPanel1.applyRevisionValuesToVersionField(
                                        gwtContext.getAdvCompareModeSettings().getRevision1());
                                }
                                if (versionFieldPanel2 != null){
                                    versionFieldPanel2.applyRevisionValuesToVersionField(
                                        gwtContext.getAdvCompareModeSettings().getRevision2());
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
    public ToolItem createNewToolItem(GWTToolbarItem gwtToolbarItem) {
        return new TextToolItem();
    }

}