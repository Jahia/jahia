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
import org.jahia.ajax.gwt.client.widget.content.ContentViews;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.content.TableView;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;


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
    protected transient TableView tableView;

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
                    tableView = contentViews.getTableView();
                    mainComponent.setStore(tableView.getTypeStore());
                    tableView.getStore().addFilter(new StoreFilter<GWTJahiaNode>() {
                        public boolean select(Store<GWTJahiaNode> gwtJahiaNodeStore, GWTJahiaNode parent, GWTJahiaNode item, String property) {
                            if (mainComponent.getValue() != null && !mainComponent.getValue().get(GWTJahiaNode.PRIMARY_TYPE_LABEL).equals(Messages.get("label.all", "All"))) {
                                if (!mainComponent.getValue().get(GWTJahiaNode.PRIMARY_TYPE_LABEL).equals(item.get(GWTJahiaNode.PRIMARY_TYPE_LABEL))) {
                                    return false;
                                }
                            }
                            return true;
                        }
                    });
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
