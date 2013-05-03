/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
