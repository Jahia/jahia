package org.jahia.ajax.gwt.client.widget.trash;


import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Trash board - display all deleted nodes in a grid, allows to undelete and preview information about each of them
 */
public class TrashboardEngine extends LayoutContainer {
    private final Linker linker;
    private EngineContainer container;
    private Grid<GWTJahiaNode> grid;
    private ButtonBar bar;

    public TrashboardEngine(Linker linker, EngineContainer container) {
        super(new FitLayout());
        this.linker = linker;
        this.container = container;
        init();

        container.setEngine(this, Messages.get("label.trashboard", "Trash Board"), bar, this.linker);
    }

    private void init() {
        setLayout(new FitLayout());

        // data proxy
        RpcProxy<List<GWTJahiaNode>> proxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaNode>> callback) {
                List<String> l = new ArrayList<String>(GWTJahiaNode.DEFAULT_FIELDS);
                l.add("j:deletionUser");
                l.add("j:deletionDate");
                l.add("j:deletionMessage");
                JahiaContentManagementService.App.getInstance().searchSQL("select * from [jmix:markedForDeletionRoot]",-1, null, null, null,
                        l, true, callback);
            }
        };

        // tree loader
        final ListLoader<GWTJahiaNode> loader = new BaseListLoader(proxy);
        final ListStore<GWTJahiaNode> deletedNodes = new ListStore<GWTJahiaNode>(loader);

        ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        ColumnConfig  column = new ColumnConfig("icon", "", 40);
        column.setResizable(false);
        column.setSortable(false);
        column.setMenuDisabled(true);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        column.setRenderer(NodeColumnConfigList.ICON_RENDERER);
        columns.add(column);

        column = new ColumnConfig("displayName", Messages.get("label.name", "Name"), 100);
        columns.add(column);
        column = new ColumnConfig("j:deletionDate", Messages.get("label.deletionDate", "Deletion date"), 150);
        columns.add(column);
        column = new ColumnConfig("j:deletionUser", Messages.get("label.deletionUser", "User"), 150);
        columns.add(column);

        column = new ColumnConfig("undelete", Messages.get("label.undelete", "Undelete"), 100);
        column.setRenderer(new GridCellRenderer() {
            public Object render(final ModelData modelData, String property, ColumnData columnData, int rowIndex, int colIndex,
                                 ListStore listStore, Grid grid) {
                GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) modelData;
                Button button = new Button(Messages.get("label.undelete", "Undelete"), new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                       MessageBox.confirm(
                               Messages.get("label.information", "Information"),
                               Messages.getWithArgs(
                                                "message.undelete.confirm",
                                                "Do you really want to undelete the selected resource {0}?",
                                                new String[] { ((GWTJahiaNode)modelData).getDisplayName() }),
                               new Listener<MessageBoxEvent>() {
                                   public void handleEvent(MessageBoxEvent be) {
                                       if (be.getButtonClicked().getText().equalsIgnoreCase(Dialog.YES)) {
                                           JahiaContentManagementService.App.getInstance().undeletePaths(Arrays.asList(((GWTJahiaNode)modelData).getPath()), new BaseAsyncCallback() {
                                               @Override
                                               public void onApplicationFailure(Throwable throwable) {
                                                   Log.error(throwable.getMessage(), throwable);
                                                   MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
                                               }

                                               public void onSuccess(Object result) {
                                                   deletedNodes.remove((GWTJahiaNode) modelData);
                                               }
                                           });
                                       }
                                   }
                               });
                    }
                });
                button.setIcon(StandardIconsProvider.STANDARD_ICONS.restore());
                return button;
            }
        });
        columns.add(column);

        column = new ColumnConfig("infos", "", 100);
        column.setRenderer(new GridCellRenderer() {
            public Object render(final ModelData modelData, String property, ColumnData columnData, int rowIndex, int colIndex,
                                 ListStore listStore, Grid grid) {
                GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) modelData;
                Button button = new Button(Messages.get("label.information", "Information"), new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        PreviewWindow w = new PreviewWindow(linker, (GWTJahiaNode) modelData);
                        w.show();
                    }
                });
                button.setIcon(StandardIconsProvider.STANDARD_ICONS.information());
                return button;
            }
        });
        columns.add(column);

        ColumnModel cm = new ColumnModel(columns);

        grid = new Grid<GWTJahiaNode>(deletedNodes, cm);
        grid.setAutoExpandColumn("displayName");
        grid.setAutoExpandMax(1000);
        add(grid);

        bar = new ButtonBar();
        bar.setAlignment(Style.HorizontalAlignment.CENTER);

        Button cancel = new Button(Messages.get("label.close", "Close"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                container.closeEngine();
            }
        });
        bar.add(cancel);

        loader.load();
    }

}
