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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.GWTJahiaContentHistoryEntry;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A GXT panel to display a content object's history
 * User: loom
 * Date: Oct 5, 2010
 * Time: 5:47:40 PM
 */
public class HistoryPanel extends LayoutContainer {

    private GWTJahiaNode node;
    private FormPanel detailsPanel;

    private List<GWTJahiaContentHistoryEntry> selectedItems = null;
    private PagingToolBar pagingToolBar;
    public static final String SECONDS_PRECISION_DATETIME_FORMAT = "dd.MM.yyyy HH:mm:ss";


    public HistoryPanel(GWTJahiaNode node) {
        super(new BorderLayout());
        this.node = node;
        init();
    }

    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
    }

    private void init() {
        setBorders(false);
        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaContentHistoryEntry>> proxy = new RpcProxy<PagingLoadResult<GWTJahiaContentHistoryEntry>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<PagingLoadResult<GWTJahiaContentHistoryEntry>> callback) {
                if (loadConfig == null) {
                    service.getContentHistory(node.getUUID(), 0, Integer.MAX_VALUE, callback);
                } else if (loadConfig instanceof BasePagingLoadConfig) {
                    BasePagingLoadConfig pagingLoadConfig = (BasePagingLoadConfig) loadConfig;
                    int limit = pagingLoadConfig.getLimit();
                    int offset = pagingLoadConfig.getOffset();
                    Style.SortDir sortDir = pagingLoadConfig.getSortDir();
                    String sortField = pagingLoadConfig.getSortField();
                    service.getContentHistory(node.getUUID(), offset, limit, callback);
                } else {
                    callback.onSuccess(new BasePagingLoadResult<GWTJahiaContentHistoryEntry>(new ArrayList<GWTJahiaContentHistoryEntry>()));
                }
            }
        };

        // tree loader
        final PagingLoader<BasePagingLoadResult<ModelData>> loader = new BasePagingLoader<BasePagingLoadResult<ModelData>>(proxy);
        loader.setRemoteSort(true);

        // trees store
        final GroupingStore<GWTJahiaContentHistoryEntry> store = new GroupingStore<GWTJahiaContentHistoryEntry>(loader);
        store.groupBy("status");

        pagingToolBar = new PagingToolBar(50);
        PagingToolBar.PagingToolBarMessages pagingMessages = pagingToolBar.getMessages();
        pagingMessages.setEmptyMsg(pagingMessages.getEmptyMsg() + ". " + Messages.get("label.historyMayBeDelayed", "History may be delayed."));
        if (pagingMessages.getDisplayMsg() != null) {
            pagingMessages.setDisplayMsg(pagingMessages.getDisplayMsg() + ". " + Messages.get("label.historyMayBeDelayed", "History may be delayed."));
        }
        pagingToolBar.bind(loader);

        List<ColumnConfig> config = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("date", Messages.get("label.date", "Date"), 125);
        column.setDateTimeFormat(DateTimeFormat.getFormat(SECONDS_PRECISION_DATETIME_FORMAT));
        column.setSortable(false);
        config.add(column);

        column = new ColumnConfig("action", Messages.get("label.action", "Action"), 90);
        column.setSortable(false);
        config.add(column);

        column = new ColumnConfig("userKey", Messages.get("label.user", "User"), 90);
        column.setSortable(false);
        config.add(column);

        column = new ColumnConfig("propertyName", Messages.get("label.property", "Property"), 90);
        column.setSortable(false);
        config.add(column);

        column = new ColumnConfig("languageCode", Messages.get("label.language", "Language"), 70);
        column.setSortable(false);
        config.add(column);

        column = new ColumnConfig("message", Messages.get("label.message", "Message"), 300);
        column.setSortable(false);
        column.setRenderer(new GridCellRenderer<GWTJahiaContentHistoryEntry>() {

            public Object render(GWTJahiaContentHistoryEntry gwtJahiaContentHistoryEntry, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaContentHistoryEntry> gwtJahiaContentHistoryEntryListStore, Grid<GWTJahiaContentHistoryEntry> gwtJahiaContentHistoryEntryGrid) {
                String message = buildMessage(gwtJahiaContentHistoryEntry);
                return new Label(message);
            }
        });
        config.add(column);

        final ColumnModel cm = new ColumnModel(config);

        final Grid<GWTJahiaContentHistoryEntry> grid = new Grid<GWTJahiaContentHistoryEntry>(store, cm);
        grid.setBorders(true);
        grid.setAutoExpandColumn("message");
        grid.setTrackMouseOver(false);
        grid.setStateId("historyPagingGrid");
        grid.setStateful(true);
        grid.addListener(Events.Attach, new Listener<GridEvent<GWTJahiaContentHistoryEntry>>() {
            public void handleEvent(GridEvent<GWTJahiaContentHistoryEntry> be) {
                PagingLoadConfig config = new BasePagingLoadConfig();
                config.setOffset(0);
                config.setLimit(50);

                Map<String, Object> state = grid.getState();
                if (state.containsKey("offset")) {
                    int offset = (Integer) state.get("offset");
                    int limit = (Integer) state.get("limit");
                    config.setOffset(offset);
                    config.setLimit(limit);
                }
                if (state.containsKey("sortField")) {
                    config.setSortField((String) state.get("sortField"));
                    config.setSortDir(Style.SortDir.valueOf((String) state.get("sortDir")));
                }
                loader.load(config);
            }
        });
        grid.setLoadMask(true);
        grid.setBorders(true);
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaContentHistoryEntry>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaContentHistoryEntry> gwtJahiaJobDetailSelectionChangedEvent) {
                selectedItems = gwtJahiaJobDetailSelectionChangedEvent.getSelection();
                updateDetails();
            }
        });

        ContentPanel listPanel = new ContentPanel();
        listPanel.setFrame(true);
        listPanel.setCollapsible(false);
        listPanel.setAnimCollapse(false);
        // panel.setIcon(Resources.ICONS.table());
        // panel.setHeading("");
        listPanel.setHeaderVisible(false);
        listPanel.setLayout(new FitLayout());
        listPanel.add(grid);
        listPanel.setSize(600, 350);
        listPanel.setBottomComponent(pagingToolBar);
        grid.getAriaSupport().setLabelledBy(listPanel.getId());
        add(listPanel);

        BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.CENTER);
        add(listPanel, centerData);

        FormPanel detailPanel = new FormPanel();
        detailPanel.setBorders(true);
        detailPanel.setBodyBorder(true);
        detailPanel.setHeaderVisible(true);
        detailPanel.setHeading(Messages.get("label.detailed", "Details"));
        detailPanel.setScrollMode(Style.Scroll.AUTOY);
        detailPanel.setLabelWidth(100);
        detailsPanel = detailPanel;

        BorderLayoutData southData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 200);
        southData.setSplit(true);
        southData.setCollapsible(true);
        add(detailPanel, southData);

    }

    private String buildMessage(GWTJahiaContentHistoryEntry gwtJahiaContentHistoryEntry) {
        String message = gwtJahiaContentHistoryEntry.getMessage();
        if ("published".equals(gwtJahiaContentHistoryEntry.getAction())) {
            String[] messageParts = message.split(";;");
            if (messageParts.length == 3) {
                message = Messages.getWithArgs("label.publishMessageWithComments", "Published from {0} to {1} with comments \"{2}\"", messageParts);
            } else if (messageParts.length == 2) {
                message = Messages.getWithArgs("label.publishMessage", "Published from {0} to {1}", messageParts);
            }
        }
        return message;
    }

    public void addDetail(String labelKey, String labelDefaultValue, Object value) {
        if (value != null) {
            TextField textField = new TextField();
            textField.setFieldLabel(Messages.get(labelKey, labelDefaultValue));
            textField.setReadOnly(true);
            if (value instanceof String) {
                textField.setValue(value);
            } else if (value instanceof Date) {
                textField.setValue(org.jahia.ajax.gwt.client.util.Formatter.getFormattedDate((Date) value, SECONDS_PRECISION_DATETIME_FORMAT));
            } else {
                textField.setValue(value.toString());
            }
            detailsPanel.add(textField, new FormData("98%"));
        }
    }

    public void addTimeDetail(String labelKey, String labelDefaultValue, Object value) {
        if (value == null) {
            addDetail(labelKey, labelDefaultValue, value);
        } else if (value instanceof Long) {
            Date date = new Date((Long) value);
            addDetail(labelKey, labelDefaultValue, date);
        } else {
            addDetail(labelKey, labelDefaultValue, value);
        }
    }

    public void updateDetails() {

        if (detailsPanel == null) {
            // maybe we clicked before it was created properly ?
            return;
        }

        if (selectedItems == null || selectedItems.size() == 0) {
            return;
        }

        detailsPanel.removeAll();
        if (selectedItems.size() == 1) {
            GWTJahiaContentHistoryEntry historyEntry = selectedItems.get(0);

            addDetail("label.user", "User key", historyEntry.getUserKey());
            addTimeDetail("label.date", "Date", historyEntry.getDate());
            addDetail("label.property", "Property", historyEntry.getPropertyName());
            addDetail("label.language", "Language", historyEntry.getLanguageCode());
            addDetail("label.path", "Path", historyEntry.getPath());
            addDetail("label.action", "Action", historyEntry.getAction());
            addDetail("label.message", "Message", buildMessage(historyEntry));
        } else {
            int nbHistoryEntries = 0;

            for (GWTJahiaContentHistoryEntry historyEntry : selectedItems) {
                nbHistoryEntries++;
            }
            detailsPanel.add(new HTML("<b>" + Messages.get("label.selectedHistoryEntryCount", "Number of selected history entries") + " :</b> " + nbHistoryEntries));
        }
        detailsPanel.layout();

    }

}
