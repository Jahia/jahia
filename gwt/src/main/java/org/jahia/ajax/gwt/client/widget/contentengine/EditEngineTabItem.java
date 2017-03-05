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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
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
        
        tab.setId("JahiaGxtEditEnginePanel-"+engineTab.getId());
        
        
        return tab;
    }

    /**
     * Create the tab item
     */
    public abstract void init(NodeHolder engine, AsyncTabItem tab, String language);

    public void onLanguageChange(String language, TabItem tabItem) {
    }

    /**
     * Set the processed state of the tab.
     * @param processed processed state
     */
    public void setProcessed(boolean processed) {
        // do nothing
    }

    public boolean isHandleMultipleSelection() {
        return false;
    }

    /**
     * @return if true, the tab will appear both in the create and edit engine, if false only appear in edit engine
     */
    public boolean isHandleCreate() {
        return handleCreate;
    }

    /**
     * @param handleCreate (default true) if true, the tab will appear both in the create and edit engine, if false only appear in edit engine
     */
    public void setHandleCreate(boolean handleCreate) {
        this.handleCreate = handleCreate;
    }

    /**
     * @return if true, the tab allow to order child nodes (default false)
     */
    public boolean isOrderableTab() {
        return false;
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

    public void doValidate(List<EngineValidation.ValidateResult> validateResult, NodeHolder engine, TabItem tab, String selectedLanguage, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, TabPanel tabs) {

    }

    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {

    }
}
