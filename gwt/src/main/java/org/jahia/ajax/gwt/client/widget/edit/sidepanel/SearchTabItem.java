package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.DisplayGridDragSource;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDragSource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Search tab item for the side panel for performing simple queries in the content repository.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 3:14:11 PM
 */
class SearchTabItem extends SidePanelTabItem {
    protected ListStore<GWTJahiaNode> contentStore;
    protected LayoutContainer contentContainer;
    protected DisplayGridDragSource displayGridSource;
    
    SearchTabItem() {
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(l);

        setIcon(ContentModelIconProvider.CONTENT_ICONS.query());

        FormPanel searchForm = new FormPanel();
        searchForm.setHeaderVisible(false);
        searchForm.setBorders(true);
        searchForm.setBodyBorder(false);
        final TextField<String> searchField = new TextField<String>();
        searchField.setFieldLabel(Messages.getResource("fm_search"));
        searchField.addListener(Events.SpecialKey, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                contentContainer.mask("Loading","x-mask-loading");
                search(searchField.getValue(), null,null);                
            }
        });
        Button ok = new Button(Messages.getResource("fm_search"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                contentContainer.mask("Loading","x-mask-loading");
                search(searchField.getValue(), null,null);
            }
        });

        Button drag = new Button(Messages.getResource("em_drag"));
        EditModeDragSource querySource = new EditModeDragSource(drag) {
            @Override
            protected void onDragStart(DNDEvent e) {
                e.setCancelled(false);
                e.getStatus().update(searchField.getValue());
                e.getStatus().setStatus(true);
                e.setData(searchField);
                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.QUERY_SOURCE_TYPE);
                e.getStatus().setData(EditModeDNDListener.SOURCE_QUERY, searchField.getValue());
                super.onDragStart(e);
            }
        };

        searchForm.add(searchField);
        HorizontalPanel h = new HorizontalPanel();
        h.add(ok);
        h.add(drag);
        searchForm.add(h);

        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(1);
        add(searchForm, contentVBoxData);


        contentContainer = new LayoutContainer();
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTO);
        contentContainer.setLayout(new FitLayout());

        contentStore = new ListStore<GWTJahiaNode>();

        List<ColumnConfig> displayColumns = new ArrayList<ColumnConfig>();

        ColumnConfig col = new ColumnConfig("ext", "", 40);
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
                return ContentModelIconProvider.getInstance().getIcon(modelData).getHTML();
            }
        });
        displayColumns.add(col);
        displayColumns.add(new ColumnConfig("displayName", Messages.getResource("fm_info_name"), 280));
        final Grid<GWTJahiaNode> grid = new Grid<GWTJahiaNode>(contentStore, new ColumnModel(displayColumns));

        contentContainer.add(grid);

        contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(4);
        add(contentContainer, contentVBoxData);

        displayGridSource = new DisplayGridDragSource(grid);
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        displayGridSource.addDNDListener(editLinker.getDndListener());
    }

    /**
     * Method used by the search form
     *
     * @param query      the query string
     * @param date       search for items newer than date
     * @param searchRoot search within this path
     */
    private void search(String query, Date date, String searchRoot) {
        JahiaContentManagementService.App.getInstance().search(query, 500, new AsyncCallback<List<GWTJahiaNode>>() {
            public void onFailure(Throwable throwable) {
                contentContainer.unmask();
                contentStore.removeAll();
            }

            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                contentContainer.unmask();
                contentStore.removeAll();

                if (gwtJahiaNodes != null) {
                    contentStore.add(gwtJahiaNodes);
                    contentStore.sort("displayName", Style.SortDir.ASC);
                }
            }
        });
    }

}