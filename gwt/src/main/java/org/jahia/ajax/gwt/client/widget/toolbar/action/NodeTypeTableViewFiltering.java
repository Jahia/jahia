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

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.AbstractView;
import org.jahia.ajax.gwt.client.widget.content.ContentViews;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Class for filtering the table view of a manager/picker based on its nodetype
 * User: rincevent
 */
public class NodeTypeTableViewFiltering extends BaseActionItem {
    private static final long serialVersionUID = 6115660301140902069L;
    protected transient ComboBox<ModelData> mainComponent;
    protected transient AbstractView tableView;

    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        initMainComponent();
    }

    @Override
    public void handleNewLinkerSelection() {
        super.handleNewLinkerSelection();
        if (tableView == null) {
            if (linker instanceof ManagerLinker) {
                ManagerLinker managerLinker = (ManagerLinker) linker;
                TopRightComponent topRightComponent = managerLinker.getTopRightObject();
                if (topRightComponent instanceof ContentViews) {
                    ContentViews contentViews = (ContentViews) topRightComponent;
                    tableView = contentViews.getCurrentView();
                    mainComponent.setStore(tableView.getTypeStore());
                    if (mainComponent.getSelection() == null || (mainComponent.getSelection() != null && mainComponent.getSelection().size() == 0)) {
                        mainComponent.setSelection(Arrays.asList(tableView.getTypeStore().getAt(0)));
                    }
                    tableView.getStore().addFilter(new StoreFilter<GWTJahiaNode>() {
                        public boolean select(Store<GWTJahiaNode> gwtJahiaNodeStore, GWTJahiaNode parent,
                                              GWTJahiaNode item, String property) {
                            ModelData value = mainComponent.getValue();
                            if (value != null) {
                                Object o = value.get(GWTJahiaNode.PRIMARY_TYPE_LABEL);
                                if (!o.equals(Messages.get("label.all", "All")) &&
                                    !o.equals(item.get(GWTJahiaNode.PRIMARY_TYPE_LABEL))) {
                                    return false;
                                }
                            }
                            return true;
                        }
                    });
                    mainComponent.recalculate();
                }
            }
        } else {
            if (!tableView.getStore().isFiltered()) {
                tableView.getStore().applyFilters("");
            }
        }
    }

    private void initMainComponent() {
        mainComponent = new ComboBox<ModelData>();
        mainComponent.setDisplayField(GWTJahiaNode.PRIMARY_TYPE_LABEL);
        mainComponent.setTypeAhead(true);
        mainComponent.setTriggerAction(ComboBox.TriggerAction.ALL);
        mainComponent.setForceSelection(false);
        mainComponent.setEditable(false);
        mainComponent.addSelectionChangedListener(new SelectionChangedListener<ModelData>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<ModelData> event) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(Linker.REFRESH_MAIN, true);
                linker.refresh(data);
            }
        });
        setEnabled(true);
    }


    @Override
    public Component getCustomItem() {
        return mainComponent;
    }


    @Override
    public void setEnabled(boolean enabled) {
        mainComponent.setEnabled(enabled);
    }
}
