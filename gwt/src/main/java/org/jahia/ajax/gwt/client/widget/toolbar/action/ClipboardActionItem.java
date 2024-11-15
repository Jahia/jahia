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

    private transient List<GWTJahiaNode> copiedStuff = new ArrayList<GWTJahiaNode>();

    protected ClipboardActionItem() {
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        setInstance(this);
    }

    private static void setInstance(ClipboardActionItem i) {
        instance = i;
    }

    public static void setCopied(List<GWTJahiaNode> copiedNodes) {
        if (instance == null) {
            instance = new ClipboardActionItem();
        }

        instance.copiedStuff = copiedNodes;

        refreshView();
    }

    private static void refreshView() {
        if (instance == null) {
            instance = new ClipboardActionItem();
        }
        if (instance.linker != null ) {
            Button b = (Button) instance.getTextToolItem();
            if (instance.copiedStuff.isEmpty()) {
                b.setEnabled(false);
                b.setMenu(null);
            } else {
                final List<GWTJahiaNode> copiedNodes = instance.copiedStuff;
                final Menu menu = new Menu();
                menu.addStyleName("clipboard-info-menu");
                b.setMenu(menu);

                MenuItem m = new MenuItem();
                m.addStyleName("clipboard-info-menu-item");
                if (copiedNodes.size() > 1) {
                    m.setText(copiedNodes.size() + " " + Messages.get("label.items", " Items"));
                } else {
                    m.setText(copiedNodes.get(0).getDisplayName());
                }
                m.setEnabled(false);
                menu.add(m);

                b.setEnabled(true);
            }
        }
    }


    @Override
    public Component createNewToolItem() {
        Button b = (Button) super.createNewToolItem();
        b.setEnabled(false);
        b.setText(Messages.get("label.clipboard", "Clipboard"));
        return b;
    }
}
