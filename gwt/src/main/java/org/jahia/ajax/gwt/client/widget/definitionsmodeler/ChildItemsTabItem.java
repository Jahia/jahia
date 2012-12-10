package org.jahia.ajax.gwt.client.widget.definitionsmodeler;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;

public class ChildItemsTabItem extends EditEngineTabItem {
    private static final long serialVersionUID = 1L;

    private List<String> columnsConfig;

    protected transient ListStore<GWTJahiaNode> store;
    protected transient PropertiesEditor propertiesEditor;

    protected transient Map<GWTJahiaNode,PropertiesEditor> propertiesEditors;

    private String type;

    public ChildItemsTabItem() {
    }


    @Override
    public void init(final NodeHolder engine,final AsyncTabItem tab, String language) {
        if (tab.isProcessed()) {
            return;
        }
        tab.setLayout(new BorderLayout());

        store = new ListStore<GWTJahiaNode>();
        propertiesEditor = null;
        propertiesEditors = new HashMap<GWTJahiaNode, PropertiesEditor>();

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        List<String> keys = new ArrayList<String>();
        for(String columnConfig : columnsConfig) {
            String[] config = columnConfig.split(",");
            keys.add(config[0]);
            ColumnConfig colConfig = new ColumnConfig();
            colConfig.setId(config[0]);
            if (!"*".equals(config[1])) {
                colConfig.setWidth(new Integer(config[1]));
            } else {
                colConfig.setWidth(100);
            }
            colConfig.setHeader(Messages.get(config[2],config[2]));
            columns.add(colConfig);
        }

        JahiaContentManagementService.App.getInstance().lsLoad(engine.getNode(), Arrays.asList(type),null,
                null, keys, false, -1,0, false, null, null, false, new BaseAsyncCallback<PagingLoadResult<GWTJahiaNode>>() {
            public void onSuccess(PagingLoadResult<GWTJahiaNode> result) {
                for (GWTJahiaNode itemDefinition : result.getData()) {
                    engine.getNode().add(itemDefinition);
                    store.add(itemDefinition);
                }
            }
        });


        Grid<GWTJahiaNode> grid = new Grid<GWTJahiaNode>(store,new ColumnModel(columns));
        grid.setHeight(200);
        tab.add(grid, new BorderLayoutData(Style.LayoutRegion.NORTH, 200));
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                final GWTJahiaNode item = se.getSelectedItem();
                JahiaContentManagementService.App.getInstance().getProperties(item.getPath(),engine.getDefaultLanguageCode(), new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
                    public void onSuccess(GWTJahiaGetPropertiesResult result) {
                        if (propertiesEditor != null) {
                            tab.remove(propertiesEditor);
                        }
                        if (propertiesEditors.containsKey(item)) {
                            propertiesEditor = propertiesEditors.get(item);
                        } else {
                            propertiesEditor = new PropertiesEditor(result.getNodeTypes(), result.getProperties(), null);
                            propertiesEditors.put(item, propertiesEditor);
                            propertiesEditor.renderNewFormPanel();
                        }
                        tab.add(propertiesEditor, new BorderLayoutData(Style.LayoutRegion.CENTER));
                        tab.layout();
                    }
                });
            }
        });
        tab.layout();
        tab.setProcessed(true);

    }

    public void setProcessed(boolean processed) {
        if (!processed) {
            store = null;
        }

        super.setProcessed(processed);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, GWTJahiaNodeACL acl) {
        if (store != null) {
            for (GWTJahiaNode itemDefinition : store.getModels()) {
                if (propertiesEditors.containsKey(itemDefinition)) {
                    PropertiesEditor pe = propertiesEditors.get(itemDefinition);
                    itemDefinition.set("nodeProperties", pe.getProperties(false,true,true));
                    itemDefinition.set("nodeLangCodeProperties", new HashMap<String, List<GWTJahiaNodeProperty>>());
                } else {
                    itemDefinition.set("nodeProperties", new ArrayList<GWTJahiaNodeProperty>());
                    itemDefinition.set("nodeLangCodeProperties", new HashMap<String, List<GWTJahiaNodeProperty>>());
                }
            }
            node.set(GWTJahiaNode.INCLUDE_CHILDREN, Boolean.TRUE);
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setColumnsConfig(List<String>columnsConfig) {
        this.columnsConfig = columnsConfig;
    }
}
