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
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.button.Button;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * Action item that represents a separator.
 *
 * @author rfelden
 */
public class SeparatorActionItem implements ActionItem {

    private static final long serialVersionUID = -888436606272080323L;

    public void setEnabled(boolean enabled) {
        // do nothing
    }

    public void setVisible(boolean visible) {
        // do nothing
    }

    public Component getCustomItem() {
        return new Html("<span class=\"xtb-sep\">&nbsp;</span>");
    }

    public Button getTextToolItem() {
        return null;
    }

    public Item getMenuItem() {
        return new SeparatorMenuItem();
    }

    public Item getContextMenuItem() {
        return new SeparatorMenuItem();
    }

    public GWTJahiaToolbarItem getGwtToolbarItem() {
        return null;
    }

    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
    }

    public void handleNewLinkerSelection() {

    }

    public void onComponentSelection() {

    }

    public void setTextToolitem(Component textToolitem) {

    }

    public void setMenuItem(MenuItem menuItem) {

    }

    public void setContextMenuItem(MenuItem contextMenuItem) {

    }

    public void handleNewMainNodeLoaded(GWTJahiaNode node) {

    }
}
