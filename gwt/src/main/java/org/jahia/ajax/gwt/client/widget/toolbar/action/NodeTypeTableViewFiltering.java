/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
        mainComponent.addStyleName(getGwtToolbarItem().getClassName());
        mainComponent.addStyleName("action-bar-menu-item");
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
