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
import org.jahia.ajax.gwt.client.widget.content.TableView;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.Arrays;


/**
 * Class for filtering the table view of a manager/picker based on its nodetype
 * User: rincevent
 * Date: 9/12/11
 * Time: 2:14 PM
 * To change this template use File | Settings | File Templates.
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
                if (linker instanceof EditLinker) {
                    ((EditLinker) linker).getMainModule().refresh(Linker.REFRESH_MAIN);
                } else if (linker instanceof ManagerLinker) {
                    ((ManagerLinker) linker).refresh(Linker.REFRESH_MAIN);
                }
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
