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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

import java.io.Serializable;
import java.util.*;

/**
 * User: toto
 * Date: Jan 6, 2010
 * Time: 6:31:49 PM
 */
public abstract class EditEngineTabItem implements Serializable {
    protected GWTEngineTab gwtEngineTab;

    protected boolean handleCreate = true;
    private List<String> showForTypes = new ArrayList<String>();
    private List<String> hideForTypes = new ArrayList<String>();

    protected EditEngineTabItem() {
    }

    public AsyncTabItem create(GWTEngineTab engineTab, NodeHolder engine) {
        this.gwtEngineTab = engineTab;

        AsyncTabItem tab = new AsyncTabItem(gwtEngineTab.getTitle()) {
            @Override public void setProcessed(boolean processed) {
                EditEngineTabItem.this.setProcessed(processed);
                super.setProcessed(processed);
            }
        };

        tab.setLayout(new FitLayout());
        tab.setStyleName("x-panel-mc");
        tab.setData("item", this);
        return tab;
    }

    /**
     * Create the tab item
     */
    public abstract void init(NodeHolder engine, AsyncTabItem tab, String language);

    public void onLanguageChange(String language) {
    }

    public void setProcessed(boolean processed) {
    }

    public boolean isHandleMultipleSelection() {
        return false;
    }

    public boolean isHandleCreate() {
        return handleCreate;
    }

    public void setHandleCreate(boolean handleCreate) {
        this.handleCreate = handleCreate;
    }

    public List<String> getShowForTypes() {
        return showForTypes;
    }

    public void setShowForTypes(List<String> showForTypes) {
        this.showForTypes = showForTypes;
    }

    public List<String> getHideForTypes() {
        return hideForTypes;
    }

    public void setHideForTypes(List<String> hideForTypes) {
        this.hideForTypes = hideForTypes;
    }

    public GWTEngineTab getGwtEngineTab() {
        return gwtEngineTab;
    }

    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, GWTJahiaNodeACL acl) {

    }
}
