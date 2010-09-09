/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

    @Override public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);

//        final Menu menu = new Menu();
//        setSubMenu(menu);
//
//        clearAll = new MenuItem("Clear");
//        clearAll.addSelectionListener(new SelectionListener<MenuEvent>() {
//            public void componentSelected(MenuEvent ce) {
//                copiedStuff.clear();
//                menu.removeAll();
//                menu.add(clearAll);
//            }
//        });
//        menu.add(clearAll);
//
        instance = this;
    }

    public static void removeCopied(List<GWTJahiaNode> copiedPath) {
        instance.copiedStuff.remove(copiedPath);
        refreshView();
    }

    public static void setCopied(List<GWTJahiaNode> copiedPath) {
        instance.copiedStuff.add(0,copiedPath);
        if (instance.copiedStuff.size() == 10) {
            instance.copiedStuff.remove(9);
        }
        refreshView();
    }

    private static void refreshView() {
        Button b = (Button) instance.getTextToolItem();
        if (instance.copiedStuff.isEmpty()) {
            b.setText(null);
            b.setVisible(false);
        } else {
            final List<GWTJahiaNode> copiedNodes = instance.copiedStuff.get(0);
            if (copiedNodes.size() > 1) {
                b.setText(copiedNodes.size() + " "+Messages.get("label.items", " Items"));
            } else {
                b.setText(Messages.get("label.clipboard","Clipboard")+": "+copiedNodes.get(0).getDisplayName());
            }
            b.setVisible(true);
        }
    }


    @Override public Component createNewToolItem() {
        Button b = new Button();
        b.setVisible(false);
        return b;
    }
}
