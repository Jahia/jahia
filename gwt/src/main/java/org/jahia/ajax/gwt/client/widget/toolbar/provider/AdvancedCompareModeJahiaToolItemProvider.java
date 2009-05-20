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
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

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
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.widget.versioning.VersionFieldPanel;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaContext;

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
    public SelectionListener<ComponentEvent> getSelectListener(final GWTJahiaToolbarItem gwtToolbarItem) {
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
                    enabledCheckBox.setFieldLabel(Messages.getNotEmptyResource("advc_enable",
                            "Enable advanced compare mode settings"));
                    panel.add(enabledCheckBox);

                    versionField1 = new VersionField(pageContext.getPid(),null,null,false);
                    versionField1.setFieldLabel(Messages.getNotEmptyResource("advc_version1",
                            "Version 1"));
                    versionFieldPanel1 = new VersionFieldPanel(versionField1);

                    versionField2 = new VersionField(pageContext.getPid(),null,null,false);
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

                    window.setHeading(gwtToolbarItem.getTitle());
                }
                if (save == null){
                    save = new Button(Messages.getNotEmptyResource("save","Save"));
                    save.addSelectionListener(new SelectionListener<ComponentEvent>() {


                        public void componentSelected(ComponentEvent event) {
                            Log.debug("*****  advanced item clicked:" + enabledCheckBox.isEnabled() + ","
                                    +versionField1.getRawValue() + "," + versionField2.getRawValue());
                            Map<String, GWTJahiaProperty> properties = gwtToolbarItem.getProperties();
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
                            storeSettings(versionField1,properties,"version1");

                            // version 2
                            storeSettings(versionField2,properties,"version2");

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
                                GWTJahiaProperty> properties, String propName){
                            GWTJahiaRevision revision =
                                    new GWTJahiaRevision();
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

                    cancel = new Button(Messages.getNotEmptyResource("cancel","Cancel"));
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
    public ToolItem createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        return new TextToolItem();
    }

}