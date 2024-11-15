/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaChannel;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

/**
 * Channel switcher action item
 */
public class ChannelDisplayActionItem extends BaseActionItem {
    private static final long serialVersionUID = 9115660301140902069L;
    protected transient HorizontalPanel horizontalPanel;
    protected boolean events = true;

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        initMainComponent();
    }

    /**
     * init main component
     */
    private void initMainComponent() {
        final ComboBox<GWTJahiaChannel> comboBox = new ComboBox<GWTJahiaChannel>();
        comboBox.setDisplayField("display");
        comboBox.setTriggerAction(ComboBox.TriggerAction.ALL);
        comboBox.setStore(new ListStore<GWTJahiaChannel>());
        comboBox.getStore().add(JahiaGWTParameters.getChannels());
        comboBox.setTemplate(getChannelTemplate());
        comboBox.setItemSelector("div.thumb-wrap");
        comboBox.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaChannel>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaChannel> event) {
                GWTJahiaChannel selectedChannel = event.getSelectedItem();
                if (linker instanceof EditLinker) {
                    ((EditLinker)linker).getMainModule().switchChannel(selectedChannel, null);
                }
            }
        });

        horizontalPanel = new HorizontalPanel();
        horizontalPanel.add(comboBox);
        horizontalPanel.addStyleName(getGwtToolbarItem().getClassName());
        horizontalPanel.addStyleName("action-bar-menu-item");

        setEnabled(true);

    }

    private native String getChannelTemplate() /*-{
     return ['<tpl for=".">',
     '<div class="thumb-wrap" id="{display}">',
     '<div class="thumb"><img src="{image}" title="{display}" width="64" height="64" style="margin-left:auto;margin-right:auto;display:block"></div>',
     '<div class="x-editable" style="text-align:center">{display}</div></div>',
     '</tpl>',
     '<div class="x-clear"></div>'].join("");

     }-*/;

    @Override
    public Component getCustomItem() {
        return horizontalPanel;
    }


    @Override
    public void setEnabled(boolean enabled) {
        horizontalPanel.setEnabled(enabled);
    }

}
