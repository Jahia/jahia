package org.jahia.ajax.gwt.client.widget.definition;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.form.AutoCompleteComboBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jun 30, 2010
 * Time: 9:50:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class TagsEditor extends ContentPanel {
    private TreeStore<GWTJahiaNode> tagStore;

    public TagsEditor(final GWTJahiaNode node) {

        setLayout(new RowLayout(Style.Orientation.HORIZONTAL));
        setWidth("100%");
        setHeight("100%");
        setBorders(false);
        setHeaderVisible(false);
        TreeLoader<GWTJahiaNode> tagLoader = new BaseTreeLoader<GWTJahiaNode>(new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object o, final AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (node != null) {
                    final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
                    async.getProperties(node.getPath(), new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
                        public void onSuccess(GWTJahiaGetPropertiesResult result) {
                            final GWTJahiaNodeProperty gwtJahiaNodeProperty = result.getProperties().get("j:tags");
                            if (gwtJahiaNodeProperty != null) {
                                final List<GWTJahiaNodePropertyValue> propertyValues = gwtJahiaNodeProperty.getValues();
                                List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(propertyValues.size());
                                for (GWTJahiaNodePropertyValue propertyValue : propertyValues) {
                                    nodes.add(propertyValue.getNode());
                                }
                                listAsyncCallback.onSuccess(nodes);
                            }
                        }
                    });
                }
            }
        });

        tagStore = new TreeStore<GWTJahiaNode>(tagLoader);
        final ContentPanel tagPanel = new ContentPanel();
        tagPanel.setHeaderVisible(false);
        tagPanel.setLayout(new RowLayout(Style.Orientation.VERTICAL));
        tagPanel.add(createTagsPanel(), new RowData(1, -1, new Margins(4)));

        add(tagPanel, new RowData(1, 1, new Margins(4, 0, 4, 0)));
        layout();
    }

    private Component createTagsPanel() {
        ColumnConfig columnConfig;
        columnConfig = new ColumnConfig("name", "Name", 500);
        columnConfig.setFixed(true);
        columnConfig.setRenderer(new TreeGridCellRenderer<GWTJahiaNode>());


        ColumnConfig action = new ColumnConfig("action", "Action", 100);
        action.setAlignment(Style.HorizontalAlignment.RIGHT);
        action.setRenderer(new GridCellRenderer() {
            public Object render(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                 ListStore listStore, Grid grid) {
                Button button = new Button("Remove", new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        final GWTJahiaNode node1 = (GWTJahiaNode) buttonEvent.getButton().getData("associatedNode");
                        tagStore.remove(node1);
                    }
                });
                button.setData("associatedNode", modelData);
                button.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
                return button;
            }
        });

        ContentPanel panel = new ContentPanel();
        panel.setBodyBorder(false);
        panel.setBorders(false);
        panel.setHeaderVisible(false);
        // Add a new tag
        final AutoCompleteComboBox autoCompleteComboBox = new AutoCompleteComboBox(JCRClientUtils.TAG_NODETYPES, 15);
        autoCompleteComboBox.setMaxLength(120);
        autoCompleteComboBox.setWidth(200);
        autoCompleteComboBox.setName("tagName");

        //panel.add(name, data);
        Button addTag = new Button(Messages.get("label.add", "Add"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
                async.getTagNode(autoCompleteComboBox.getRawValue(), true, new BaseAsyncCallback<GWTJahiaNode>() {
                    /**
                     * On success
                     * @param result
                     */
                    public void onSuccess(GWTJahiaNode result) {
                        if (tagStore.findModel(result) == null) {
                            tagStore.add(result, false);
                        }
                    }


                });

            }
        });

        ButtonBar bar = new ButtonBar();
        bar.add(new FillToolItem());
        bar.add(new Text(Messages.get("label_add_tag", "Add Tag") + ":"));
        bar.add(autoCompleteComboBox);
        bar.add(addTag);
        panel.setTopComponent(bar);
        // Sub grid

        TreeGrid<GWTJahiaNode> tagGrid = new TreeGrid<GWTJahiaNode>(tagStore, new ColumnModel(Arrays.asList(
                columnConfig, action)));
        tagGrid.setHeight(360);
        tagGrid.setIconProvider(ContentModelIconProvider.getInstance());
        tagGrid.setAutoExpandColumn("name");
        tagGrid.getTreeView().setRowHeight(25);
        tagGrid.getTreeView().setForceFit(true);
        panel.add(tagGrid);

        return panel;
    }
    public TreeStore<GWTJahiaNode> getTagStore() {
        return tagStore;
    }

}
