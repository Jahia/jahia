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
package org.jahia.ajax.gwt.client.widget.layoutmanager;

import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutManagerConfig;
import org.jahia.ajax.gwt.client.service.layoutmanager.LayoutmanagerService;
import org.jahia.ajax.gwt.client.messages.Messages;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * User: ktlili
 * Date: 20 nov. 2008
 * Time: 16:12:47
 */
public class JahiaPortalConfig extends Window {
    private GWTJahiaLayoutManagerConfig gwtPortalConfig;

    public JahiaPortalConfig(GWTJahiaLayoutManagerConfig gwtPortalConfig) {
        this.gwtPortalConfig = gwtPortalConfig;
        setModal(true);
        setSize(500, 200);
        setPlain(true);
        setHeading(Messages.getNotEmptyResource("p_config_title","Config"));
        setLayout(new FitLayout());
        add(createFormConfig());

    }

    private FormPanel createFormConfig() {
        FormPanel formPanel = new FormPanel();
        FormLayout layout = new FormLayout();
        layout.setLabelWidth(175);
        layout.setPadding(4);
        formPanel.setLayout(layout);
        formPanel.setBodyBorder(false);
        formPanel.setHeaderVisible(false);

        // numbercolumns
        final NumberField numberColumnsField = new NumberField();
        numberColumnsField.setFieldLabel(Messages.getNotEmptyResource("p_number_columns","Number of columns"));
        numberColumnsField.setValue(gwtPortalConfig.getNbColumns());
        numberColumnsField.setAllowBlank(false);
        numberColumnsField.setAllowNegative(false);
        formPanel.add(numberColumnsField);


        // draggable live mode
        final CheckBoxGroup refreshRadioGroup = new CheckBoxGroup();
        refreshRadioGroup.setFieldLabel(Messages.getNotEmptyResource("p_portal_editable_live_mode","My portal is editable in live mode"));
        final CheckBox dndLiveMode = new CheckBox();
        dndLiveMode.setName("draggableLiveMode");
        dndLiveMode.setValue(gwtPortalConfig.isLiveDraggable());
        refreshRadioGroup.add(dndLiveMode);
       // formPanel.add(refreshRadioGroup);


        // quickbar
        final CheckBoxGroup quickbarVisibleGroup = new CheckBoxGroup();
        quickbarVisibleGroup.setFieldLabel(Messages.getNotEmptyResource("p_add_mashup_live_mode","User can add mashup in live mode"));
        final CheckBox quickbarVisibleCheckBox = new CheckBox();
        quickbarVisibleCheckBox.setValue(gwtPortalConfig.isLiveQuickbarVisible());
        quickbarVisibleGroup.add(quickbarVisibleCheckBox);
       // formPanel.add(quickbarVisibleGroup);

        final Button saveButton = new Button(Messages.getNotEmptyResource("p_save","Save"));
        saveButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig = new GWTJahiaLayoutManagerConfig();
                gwtLayoutManagerConfig.setNbColumns(numberColumnsField.getValue().intValue());
                gwtLayoutManagerConfig.setLiveDraggable(dndLiveMode.getValue());
                gwtLayoutManagerConfig.setLiveQuickbarVisible(quickbarVisibleCheckBox.getValue());
                LayoutmanagerService.App.getInstance().saveLayoutmanagerConfig(JahiaPageEntryPoint.getJahiaGWTPage(), gwtLayoutManagerConfig, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Log.error("Error while saving layout manager config", throwable);
                    }

                    public void onSuccess(Object o) {
                        Log.debug("layout config saved");
                        JahiaPortalManager.getInstance().refreshPortal();
                    }
                });
            }
        });
        formPanel.addButton(saveButton);

        return formPanel;
    }
}
