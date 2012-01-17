/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 13:41:08
 */
public class ClipboardActionItem extends BaseActionItem {

    private static ClipboardActionItem instance;

    private transient List<List<GWTJahiaNode>> copiedStuff = new ArrayList<List<GWTJahiaNode>>();
    private transient MenuItem clearAll;

    protected ClipboardActionItem() {}

    @Override public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        instance = this;
    }

    public static void removeCopied(List<GWTJahiaNode> copiedPath) {
        instance.copiedStuff.remove(copiedPath);
        refreshView();
    }

    public static void setCopied(List<GWTJahiaNode> copiedPath) {
        if (instance == null) instance = new ClipboardActionItem();

        // todo handle history, keeps old items
        instance.copiedStuff.clear();

        instance.copiedStuff.add(0,copiedPath);
        if (instance.copiedStuff.size() == 10) {
            instance.copiedStuff.remove(9);
        }
        refreshView();
    }

    private static void refreshView() {
        if (instance == null) instance = new ClipboardActionItem();
        if (instance.linker != null ) {
            Button b = (Button) instance.getTextToolItem();
            if (instance.copiedStuff.isEmpty()) {
                b.setEnabled(false);
                b.setMenu(null);
            } else {
                final List<GWTJahiaNode> copiedNodes = instance.copiedStuff.get(0);
                final Menu menu = new Menu();
                b.setMenu(menu);
                for (List<GWTJahiaNode> c : instance.copiedStuff) {
                    MenuItem m = new MenuItem();
                    if (copiedNodes.size() > 1) {
                        m.setText(copiedNodes.size() + " "+Messages.get("label.items", " Items"));
                    } else {
                        m.setText(copiedNodes.get(0).getDisplayName());
                    }
                    m.setEnabled(false);
                    menu.add(m);
                }
                b.setEnabled(true);
            }
        }
    }


    @Override public Component createNewToolItem() {
        Button b = new Button();
        b.setEnabled(false);
        b.setText(Messages.get("label.clipboard","Clipboard"));
        return b;
    }
}
