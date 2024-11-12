/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaChannel;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChannelsTabItem extends SidePanelTabItem {
    protected transient Grid<GWTJahiaBasicDataBean> grid;
    protected transient ComboBox<GWTJahiaChannel> deviceCombo;
    protected transient ComboBox<GWTJahiaBasicDataBean> orientationComponent;
    protected transient Image deviceImage;
    protected transient Label deviceNameLabel;


    @Override
    public TabItem create(GWTSidePanelTab sidePanelTab) {
        super.create(sidePanelTab);
        tab.setScrollMode(Style.Scroll.AUTO);
        tab.setId("JahiaGxtChannelsTab");

        VBoxLayout verticalEastLayout = new VBoxLayout();
        verticalEastLayout.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        tab.setLayout(verticalEastLayout);

        VerticalPanel deviceDetailsPanel = new VerticalPanel();
        deviceDetailsPanel.setTableWidth("100%");
        deviceDetailsPanel.setHorizontalAlign(Style.HorizontalAlignment.CENTER);

        deviceImage = new Image();
        deviceDetailsPanel.add(deviceImage, new TableData("64px", "64px"));
        deviceNameLabel = new Label(Messages.get("label.deviceName", "Device name"));
        deviceDetailsPanel.add(deviceNameLabel);

        FormPanel formPanel = new FormPanel();
        formPanel.setPadding(2);
        formPanel.setBorders(false);
        formPanel.setBodyBorder(false);
        formPanel.setHeaderVisible(false);

        ListStore<GWTJahiaChannel> channelListStore = new ListStore<GWTJahiaChannel>();
        channelListStore.add(JahiaGWTParameters.getChannels());
        deviceCombo = new ComboBox<GWTJahiaChannel>();
        deviceCombo.setEmptyText(Messages.get("label.selectDevice", "Select device..."));
        deviceCombo.setDisplayField("display");
        deviceCombo.setStore(channelListStore);
        if (channelListStore.getCount() > 0) {
            GWTJahiaChannel channel = channelListStore.getAt(0);
            deviceCombo.setValue(channel);
            deviceImage.setUrl(JahiaGWTParameters.getContextPath() + channel.getCapability("device-image"));
        } else {
            deviceCombo.setValue(null);
            deviceCombo.disable();
        }
        deviceCombo.setTypeAhead(true);
        deviceCombo.setWidth(100);
        deviceCombo.setFieldLabel(Messages.get("label.device", "Device"));
        deviceCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
        deviceCombo.setTemplate(getChannelTemplate());
        deviceCombo.setItemSelector("div.thumb-wrap");
        deviceCombo.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaChannel>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaChannel> event) {
                GWTJahiaChannel selectedChannel = event.getSelectedItem();
                editLinker.getMainModule().switchChannel(selectedChannel, null);
                doRefresh();
            }
        });
        formPanel.add(deviceCombo);

        // we will setup the right elements now because we will need to reference them in the event listener
        final ListStore<GWTJahiaBasicDataBean> orientations = new ListStore<GWTJahiaBasicDataBean>();
        orientationComponent = new ComboBox<GWTJahiaBasicDataBean>();
        orientationComponent.setEmptyText(Messages.get("label.selectVariant", "Select variant..."));
        orientationComponent.setDisplayField("displayName");
        orientationComponent.setName("previewChannelOrientation");
        orientationComponent.setStore(orientations);
        if (orientations.getCount() > 0) {
            orientationComponent.setValue(orientations.getAt(0));
        } else {
            orientationComponent.disable();
        }
        orientationComponent.setTypeAhead(true);
        orientationComponent.setWidth(100);
        orientationComponent.setFieldLabel(Messages.get("label.variant", "Variant"));
        orientationComponent.setTriggerAction(ComboBox.TriggerAction.ALL);
        orientationComponent.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaBasicDataBean>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaBasicDataBean> se) {
                editLinker.getMainModule().switchChannel(editLinker.getActiveChannel(), se.getSelectedItem().getValue());
            }
        });
        formPanel.add(orientationComponent, new FormData("100%"));


        deviceDetailsPanel.add(formPanel);

        VBoxLayoutData firstBoxLayoutData = new VBoxLayoutData();
        firstBoxLayoutData.setFlex(1);

        tab.add(deviceDetailsPanel, firstBoxLayoutData);

        ContentPanel gridContentPanel = new ContentPanel(new FitLayout());
        gridContentPanel.setHeadingHtml(Messages.get("label.capabilities", "Capabilities"));

        ListStore<GWTJahiaBasicDataBean> capabilitiesStore = new ListStore<GWTJahiaBasicDataBean>();
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig();
        column.setId("displayName");
        column.setHeaderHtml(Messages.get("label.name", "Name"));
        column.setRowHeader(true);
        column.setWidth(100);
        configs.add(column);

        column = new ColumnConfig();
        column.setId("value");
        column.setHeaderHtml(Messages.get("label.value", "Value"));
        column.setWidth(100);
        configs.add(column);
        ColumnModel cm = new ColumnModel(configs);

        grid = new Grid<GWTJahiaBasicDataBean>(capabilitiesStore, cm);
        grid.setAutoExpandColumn("value");
        gridContentPanel.add(grid);
        // grid.setAutoExpandColumn(cm.getAutoExpand());
        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(3);
        tab.add(gridContentPanel, contentVBoxData);
        BorderLayoutData eastData = new BorderLayoutData(Style.LayoutRegion.EAST);
        eastData.setSplit(true);
        eastData.setCollapsible(true);
        eastData.setSize(200);
        eastData.setHidden(true);

        return tab;
    }

    @Override
    public boolean needRefresh(Map<String, Object> data) {
        return data.containsKey("event") && "channelChanged".equals(data.get("event"));
    }

    @Override
    public void doRefresh() {
        GWTJahiaChannel activeChannel = editLinker.getActiveChannel();
        int activeChannelIndex = 0;

        deviceImage.setUrl("");
        deviceNameLabel.setHtml(Messages.get("label.default", "Default"));
        if (activeChannel != null) {
            if (activeChannel.getCapability("device-image") != null) {
                deviceImage.setUrl(JahiaGWTParameters.getContextPath() + activeChannel.getCapability("device-image"));
            }
            deviceNameLabel.setHtml(activeChannel.getDisplay());
        }

        orientationComponent.getStore().removeAll();
        if (activeChannel != null && activeChannel.getCapability("variants") != null) {
            String[] variantValueArray = activeChannel.getCapability("variants").split(",");
            String[] variantDisplayNameArray = variantValueArray;
            if (activeChannel.getCapability("variants-displayNames") != null) {
                variantDisplayNameArray = activeChannel.getCapability("variants-displayNames").split(",");
            }

            activeChannelIndex = editLinker.getActiveChannelVariantIndex();

            List<GWTJahiaBasicDataBean> selectedItems = new ArrayList<GWTJahiaBasicDataBean>();
            int i = 0;
            for (String variantValue : variantValueArray) {
                String displayName = variantValue;
                if (i < variantDisplayNameArray.length) {
                    displayName = variantDisplayNameArray[i];
                }
                GWTJahiaBasicDataBean currentVariant = new GWTJahiaBasicDataBean(variantValue, displayName);
                if (activeChannelIndex == i) {
                    selectedItems.add(currentVariant);
                }
                orientationComponent.getStore().add(currentVariant);
                i++;
            }
            orientationComponent.disableEvents(true);
            orientationComponent.setSelection(selectedItems);
            orientationComponent.disableEvents(false);
            orientationComponent.enable();
        } else {
            orientationComponent.disableEvents(true);
            orientationComponent.setValue(null);
            orientationComponent.disableEvents(false);
            orientationComponent.disable();
        }

        grid.getStore().removeAll();
        if (activeChannel != null && activeChannel.getCapabilities() != null) {
            for (Map.Entry<String, String> capabilityEntry : activeChannel.getCapabilities().entrySet()) {
                grid.getStore().add(new GWTJahiaBasicDataBean(capabilityEntry.getValue(), capabilityEntry.getKey()));
            }
        }

        tab.layout();
    }

    private native String getChannelTemplate() /*-{
        return ['<tpl for=".">',
            '<div class="thumb-wrap" id="{display}">',
            '<div class="thumb"><img src="{image}" title="{display}" width="64" height="64" style="margin-left:auto;margin-right:auto;display:block"></div>',
            '<div class="x-editable" style="text-align:center">{display}</div></div>',
            '</tpl>',
            '<div class="x-clear"></div>'].join("");

    }-*/;


}

