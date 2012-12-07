package org.jahia.ajax.gwt.client.widget.definitionsmodeler;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChildItemsTabItem extends EditEngineTabItem {
    private static final long serialVersionUID = 1L;

    private boolean displayNodes;
    private List<String> columnsConfig;

    protected transient ListStore<GWTJahiaItemDefinition> store;

    public ChildItemsTabItem() {
    }


    @Override
    public void init(NodeHolder engine,final AsyncTabItem tab, String language) {
        store = new ListStore<GWTJahiaItemDefinition>();

        JahiaContentManagementService.App.getInstance().getNodeType(engine.getNode().getName(), new BaseAsyncCallback<GWTJahiaNodeType>() {
            @Override
            public void onSuccess(GWTJahiaNodeType result) {
                for (GWTJahiaItemDefinition itemDefinition : result.getItems()) {
                    store.add(itemDefinition);
                }
            }
        });
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        for(String columnConfig : columnsConfig) {
            String[] config = columnConfig.split(",");
            ColumnConfig colConfig = new ColumnConfig();
            colConfig.setId(config[0]);
            if (!"*".equals(config[1])) {
                colConfig.setWidth(new Integer(config[1]));
            } else {
                colConfig.setWidth(100);
            }
            colConfig.setHeader(Messages.get(config[2],config[2]));
            colConfig.setRenderer(new GridCellRenderer() {
                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore store, Grid grid) {
                    GWTJahiaItemDefinition def = (GWTJahiaItemDefinition) model;
                    if ("name".equals(property)) {
                        return def.getName();
                    } else if ("type".equals(property)) {
                        return def.getDeclaringNodeType();
                    } else if ("defaultValue".equals(property)) {
                        if (def instanceof GWTJahiaPropertyDefinition) {
                            GWTJahiaPropertyDefinition prop = (GWTJahiaPropertyDefinition) def;
                            String r = "";
                            for (GWTJahiaNodePropertyValue s : prop.getDefaultValues()) {
                                r += " " + s.getString();
                            }
                            return r;
                        }
                        return "";
                    } else if ("isInternationalized".equals(property)) {
                        return def.isInternationalized();
                    } else if ("isMandatory".equals(property)) {
                        return def.isMandatory();
                    } else if ("isProtected".equals(property)) {
                        return def.isProtected();
                    } else if ("isHidden".equals(property)) {
                        return def.isHidden();
                    }
                    return "property " + property + "not set";
                }
            });
            columns.add(colConfig);
        }
        Grid<GWTJahiaItemDefinition> grid = new Grid<GWTJahiaItemDefinition>(store,new ColumnModel(columns));
        grid.setHeight(200);
        tab.add(grid);
        tab.layout();

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

    public void setColumnsConfig(List<String>columnsConfig) {
        this.columnsConfig = columnsConfig;
    }
}
