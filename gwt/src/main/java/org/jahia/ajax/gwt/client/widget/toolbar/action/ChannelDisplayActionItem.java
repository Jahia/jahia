/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
