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
package org.jahia.ajax.gwt.client.widget.layoutmanager;

import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutManagerConfig;
import org.jahia.ajax.gwt.client.service.layoutmanager.LayoutmanagerService;
import org.jahia.ajax.gwt.client.messages.Messages;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
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
        layout.setLabelPad(4);
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
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig = new GWTJahiaLayoutManagerConfig();
                gwtLayoutManagerConfig.setNbColumns(numberColumnsField.getValue().intValue());
                gwtLayoutManagerConfig.setLiveDraggable(dndLiveMode.getValue());
                gwtLayoutManagerConfig.setLiveQuickbarVisible(quickbarVisibleCheckBox.getValue());
                LayoutmanagerService.App.getInstance().saveLayoutmanagerConfig(gwtLayoutManagerConfig, new AsyncCallback() {
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
