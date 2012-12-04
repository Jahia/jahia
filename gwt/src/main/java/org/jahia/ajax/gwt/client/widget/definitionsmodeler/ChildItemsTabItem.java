package org.jahia.ajax.gwt.client.widget.definitionsmodeler;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChildItemsTabItem extends EditEngineTabItem {
    private static final long serialVersionUID = 1L;

    private boolean displayNodes;
    private List<GWTColumn> columns;

    protected transient ListStore<ModelData> store;

    public ChildItemsTabItem() {
    }


    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String language) {
        store = new ListStore<ModelData>();

        JahiaContentManagementService.App.getInstance().getNodeType(engine.getNode().getName(), new BaseAsyncCallback<GWTJahiaNodeType>() {
            @Override
            public void onSuccess(GWTJahiaNodeType result) {
                for (GWTJahiaItemDefinition itemDefinition : result.getItems()) {
                    if (itemDefinition.isNode() == displayNodes) {
                        BaseModelData data = new BaseModelData();
                        data.set("name", itemDefinition.getName());
                        data.set("isMandatory", Boolean.valueOf(itemDefinition.isMandatory()));
                        data.set("isInternationalized", Boolean.valueOf(itemDefinition.isInternationalized()));
                        data.set("isProtected", Boolean.valueOf(itemDefinition.isProtected()));
                        data.set("isHidden", Boolean.valueOf(itemDefinition.isHidden()));
                        data.set("definition", itemDefinition);

                        if (!itemDefinition.isNode()) {
                            GWTJahiaPropertyDefinition propertyDefinition = (GWTJahiaPropertyDefinition) itemDefinition;
                            data.set("type", propertyDefinition.getRequiredType());
                        } else {
                            GWTJahiaNodeDefinition nodeDefinition = (GWTJahiaNodeDefinition) itemDefinition;

                        }
                        store.add(data);
                    }
                }
            }
        });

        NodeColumnConfigList config = new NodeColumnConfigList(columns);
        Grid<ModelData> grid = new Grid<ModelData>(store,new ColumnModel(config));

        tab.add(grid);

    }

    public void setProcessed(boolean processed) {
        if (!processed) {
            store = null;
        }

        super.setProcessed(processed);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, GWTJahiaNodeACL acl) {
        super.doSave(node, changedProperties, changedI18NProperties, addedTypes, removedTypes, acl);
    }

    public void setDisplayNodes(boolean displayNodes) {
        this.displayNodes = displayNodes;
    }

    public void setColumns(List<GWTColumn> columns) {
        this.columns = columns;
    }
}
