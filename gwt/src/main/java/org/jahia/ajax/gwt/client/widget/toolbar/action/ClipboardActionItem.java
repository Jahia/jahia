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

    public static void removeCopied(List<GWTJahiaNode> copiedNodes) {
        instance.copiedStuff.remove(copiedNodes);
        refreshView();
    }

    public static void setCopied(List<GWTJahiaNode> copiedNodes) {
        if (instance == null) instance = new ClipboardActionItem();

        // todo handle history, keeps old items
        instance.copiedStuff.clear();

        instance.copiedStuff.add(0, copiedNodes);
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
