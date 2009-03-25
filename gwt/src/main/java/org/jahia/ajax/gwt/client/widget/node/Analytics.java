package org.jahia.ajax.gwt.client.widget.node;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.layout.*;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.ListStore;

import com.extjs.gxt.ui.client.util.Margins;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.allen_sauer.gwt.log.client.Log;


import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.*;


import java.util.*;

import org.jahia.ajax.gwt.client.service.analytics.GWTAnalyticsService;

/**
 * Created by IntelliJ IDEA.
 * Date: 2 mars 2009
 * Time: 08:36:24
 *
 * @author Ibrahim El Ghandour
 */
public class Analytics extends LayoutContainer {
    private TabPanel tabs;
    private ToolBar toolbarr;
    private ContentPanel predefinedQueryPanel;
    private TabItem tabItemLastActivities;
    private TabItem tabItemMostActiveUsers;
    private TabItem tabItemLeastActiveUsers;
    private TabItem tabItemPredefinedQuery;
    private TabItem tabItemErrors;
    private ListStore<BaseModelData> storeLastNactivities = new ListStore<BaseModelData>();
    private ListStore<BaseModelData> storeLastMostRequested = new ListStore<BaseModelData>();
    private ListStore<BaseModelData> storeMostActiveUsers = new ListStore<BaseModelData>();
    private ListStore<BaseModelData> storeLeastActiveUsers = new ListStore<BaseModelData>();
    private ListStore<BaseModelData> storeResult = new ListStore<BaseModelData>();
    private ListStore<BaseModelData> storeSystemErrors = new ListStore<BaseModelData>();


    public Analytics() {
        super();
        setLayout(new FitLayout());

        ContentPanel panel = new ContentPanel();
        panel.setHeading("Analytics");
        panel.setLayout(new RowLayout(Style.Orientation.VERTICAL));
        panel.setHeight(600);
        /*  get the last 20 activities */
        getLastNactivities(20);
        /*  get the most 5 active users */
        getMostNactiveUsers(5);
        /*  get the least 5 active users */
        //getLeastNactiveUsers(5);
        /*  get the last 5 system errors*/
        getLastNSystemErrors(5);

        toolbarr = getToolbar();
        tabs = getTabs();

        predefinedQueryPanel = getQueriesPanel();

        panel.add(toolbarr, new RowData(1, -1, new Margins(4)));
        panel.add(tabs, new RowData(1, 1, new Margins(0, 4, 0, 4)));

        add(panel);
    }

    private void getLastNSystemErrors(int N) {
        GWTAnalyticsService.App.getInstance().getLastNSystemErrors(N, new AsyncCallback<Map<String, String>>() {

            public void onSuccess(Map<String, String> result) {

                storeSystemErrors.removeAll();

                int size = Integer.parseInt(result.get("size"));
                BaseModelData bm11 = new BaseModelData();
                for (int i = 1; i <= size; i++) {
                    bm11.set("time", result.get("time" + i));
                    bm11.set("code", (result.get("code" + i)));
                    String msg = result.get("msg" + i);
                    Log.info("------------------");
                    Log.info(msg);
                    Log.info(msg.split(",")[0].split("=")[1]);
                    Log.info(msg.split(",")[1].split("=")[1]);
                    Log.info("------------------");
                    bm11.set("msgUsr", msg.split(",")[0].split("=")[1]);
                    bm11.set("msgSys", (msg.split(",")[1].split("=")[1]).split(":")[1]);
                    bm11.set("source",result.get("src" + i));
                    storeSystemErrors.add(bm11);
                    bm11 = new BaseModelData();
                }
            }

            public void onFailure(Throwable throwable) {
                Info.display("", "Error occured on the server side");
                //To change body of implemented methods use File | Settings | File Templates.
            }
        }
        );
    }

    public void getLastNactivities(int N) {
        GWTAnalyticsService.App.getInstance().getLastNActivities(N, new AsyncCallback<Map<String, String>>() {

            public void onSuccess(Map<String, String> result) {

                storeLastNactivities.removeAll();

                int size = Integer.parseInt(result.get("size"));
                BaseModelData bm11 = new BaseModelData();
                for (int i = 1; i <= size; i++) {
                    bm11.set("id", result.get("id" + i));
                    bm11.set("time", result.get("time" + i));
                    bm11.set("user", (result.get("user" + i)));
                    bm11.set("objectid", (result.get("objectid" + i)));
                    bm11.set("type", (result.get("type" + i)));
                    bm11.set("operation", (result.get("operation" + i)));
                    bm11.set("uuid", (result.get("uuid" + i)));
                    bm11.set("sid", (result.get("siteid" + i)));
                    bm11.set("pid", (result.get("pid" + i)));
                    bm11.set("hits", result.get("hits" + i));
                    storeLastNactivities.add(bm11);
                    bm11 = new BaseModelData();
                }
            }

            public void onFailure(Throwable throwable) {
                Info.display("", "Error occured on the server side");
            }
        }
        );
    }

    private void getLeastNactiveUsers(int N) {
        GWTAnalyticsService.App.getInstance().getLeastNactiveUsers(N, new AsyncCallback<Map<String, String>>() {

            public void onSuccess(Map<String, String> result) {
                storeLeastActiveUsers.removeAll();
                BaseModelData bm11;
                for (Iterator it = result.keySet().iterator(); it.hasNext();) {
                    String name = (String) it.next();
                    Log.info(name + " : " + result.get(name));
                    bm11 = new BaseModelData();
                    bm11.set("user", name);
                    bm11.set("operations", result.get(name));
                    //bm11.set("lastaccess", result.get(name));
                    storeLeastActiveUsers.add(bm11);
                }
            }

            public void onFailure(Throwable throwable) {
                Info.display("", "Error occured on the server side");
            }
        }
        );
    }

    private void getMostNactiveUsers(int N) {
        GWTAnalyticsService.App.getInstance().getMostNactiveUsers(N, new AsyncCallback<Map<String, String>>() {

            public void onSuccess(Map<String, String> result) {
                storeMostActiveUsers.removeAll();
                BaseModelData bm11;
                for (Iterator it = result.keySet().iterator(); it.hasNext();) {
                    String name = (String) it.next();
                    Log.info(name + " : " + result.get(name));
                    bm11 = new BaseModelData();
                    bm11.set("user", name);
                    bm11.set("operations", result.get(name));
                    //bm11.set("lastaccess", result.get(name));
                    storeMostActiveUsers.add(bm11);
                }
            }

            public void onFailure(Throwable throwable) {
                Info.display("", "Error occured on the server side");
            }
        }
        );
    }

    private ContentPanel getQueriesPanel() {
        ContentPanel hp = new ContentPanel();
        hp.setLayout(new RowLayout(Style.Orientation.VERTICAL));

        FormData formData = new FormData("100%");
        FormPanel panel = new FormPanel();
        panel.setFrame(true);
        panel.setIconStyle("icon-form");
        panel.setCollapsible(true);
        panel.setHeading("FormPanel");
        panel.setSize(470, -1);
        panel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        panel.setLayout(new FlowLayout());

        LayoutContainer main = new LayoutContainer();
        main.setLayout(new ColumnLayout());

        LayoutContainer left = new LayoutContainer();

        FormLayout layout = new FormLayout();
        layout.setLabelAlign(FormPanel.LabelAlign.TOP);
        left.setLayout(layout);

        final TextField<String>[] user = new TextField[1];
        user[0] = new TextField();
        user[0].setFieldLabel("Name");
        user[0].setAllowBlank(false);
        left.add(user[0], formData);

        final TextField<String>[] operation = new TextField[1];
        operation[0] = new TextField();
        operation[0].setFieldLabel("operation");
        left.add(operation[0], formData);

        final TextField<String>[] objectType = new TextField[1];
        objectType[0] = new TextField();
        objectType[0].setFieldLabel("Object type");
        left.add(objectType[0], formData);


        LayoutContainer right = new LayoutContainer();

        layout = new FormLayout();
        layout.setLabelAlign(FormPanel.LabelAlign.TOP);
        right.setLayout(layout);

        final NumberField[] objectId = new NumberField[1];
        objectId[0] = new NumberField();
        objectId[0].setAllowNegative(false);
        objectId[0].setFieldLabel("Object id");
        right.add(objectId[0], formData);

        final TextField<String>[] uuid = new TextField[1];
        uuid[0] = new TextField();
        uuid[0].setFieldLabel("UUID");
        right.add(uuid[0], formData);

        final NumberField[] siteId = new NumberField[1];
        siteId[0] = new NumberField();
        siteId[0].setAllowNegative(false);
        siteId[0].setFieldLabel("Site id");
        right.add(siteId[0], formData);

        final NumberField[] pageId = new NumberField[1];
        pageId[0] = new NumberField();
        pageId[0].setAllowNegative(false);
        pageId[0].setFieldLabel("Page id");
        right.add(pageId[0], formData);


        main.add(left, new ColumnData(.5));
        main.add(right, new ColumnData(.5));

        panel.add(main);


        LayoutContainer area = new LayoutContainer();
        area.setStyleAttribute("padding", "0 10px 5px 10px");

        layout = new FormLayout();
        layout.setLabelAlign(FormPanel.LabelAlign.TOP);
        layout.setPadding(0);
        area.setLayout(layout);

        final SelectionListener executeActionListener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {
                String name = user[0].getValue();
                if (name == null) {
                    name = "";
                }
                String op = operation[0].getValue();
                if (op == null) {
                    op = "";
                }
                String type = objectType[0].getValue();
                if (type == null) {
                    type = "";
                }
                String uu = uuid[0].getValue();
                if (uu == null) {
                    uu = "";
                }
                int oid = -1;
                int sid = -1;
                int pid = -1;
                if (objectId[0].getValue() != null) {
                    oid = objectId[0].getValue().intValue();
                }
                if (siteId[0].getValue() != null) {
                    sid = siteId[0].getValue().intValue();
                }
                if (pageId[0].getValue() != null) {
                    pid = pageId[0].getValue().intValue();
                }
                executeQuery(name, op, type, oid, uu, pid, sid, 20);

            }
        };
        Button executeQuerybutton = new Button("Execute");
        executeQuerybutton.addSelectionListener(executeActionListener);
        panel.addButton(executeQuerybutton);

        final SelectionListener resetActionListener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {

                Info.display("r", "reset fields");


            }
        };
        Button resetQuerybutton = new Button("Reset");
        resetQuerybutton.addSelectionListener(resetActionListener);
        panel.addButton(resetQuerybutton);

        hp.add(panel, new RowData(1, -1, new Margins(4)));

        hp.add(getGridResult(), new RowData(1, 1, new Margins(0, 4, 0, 4)));

        return hp;


    }

    private void executeQuery(String name, String op, String type, int oid, String uu, int pid, int sid, int N) {
        GWTAnalyticsService.App.getInstance().executeQuery(name, op, type, oid, uu, pid, sid, N, new AsyncCallback<Map<String, String>>() {
            public void onSuccess(Map<String, String> result) {
                storeResult.removeAll();
                int size = Integer.parseInt(result.get("size"));
                BaseModelData bm11 = new BaseModelData();
                for (int i = 1; i <= size; i++) {
                    bm11.set("id", result.get("id" + i));
                    bm11.set("time", result.get("time" + i));
                    bm11.set("user", (result.get("user" + i)));
                    bm11.set("objectid", (result.get("objectid" + i)));
                    bm11.set("type", (result.get("type" + i)));
                    bm11.set("operation", (result.get("operation" + i)));
                    bm11.set("uuid", (result.get("uuid" + i)));
                    bm11.set("sid", (result.get("siteid" + i)));
                    bm11.set("pid", (result.get("pid" + i)));
                    storeResult.add(bm11);
                    bm11 = new BaseModelData();
                }

                Info.display("", "your query has been successfully executed");
            }

            public void onFailure(Throwable throwable) {
                Info.display("Error", "execution failure");
            }
        }
        );
    }

    private Grid getGridResult() {
        List<ColumnConfig> configsResult = new ArrayList<ColumnConfig>();
        ColumnConfig column = new ColumnConfig("time", "Time", 150);
        column.setAlignment(Style.HorizontalAlignment.LEFT);
        configsResult.add(column);

        column = new ColumnConfig("user", "User", 75);
        configsResult.add(column);
        column = new ColumnConfig("operation", "Operation", 75);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsResult.add(column);
        column = new ColumnConfig("type", "Type", 75);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsResult.add(column);
        column = new ColumnConfig("objectid", "Object Id", 75);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsResult.add(column);

        column = new ColumnConfig("hits", "Hits", 75);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsResult.add(column);

        column = new ColumnConfig("pid", "Page id", 75);
        //column.setHidden(true);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsResult.add(column);

        column = new ColumnConfig("uuid", "UUID", 75);
        column.setHidden(true);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsResult.add(column);

        column = new ColumnConfig("sid", "Site id", 75);
        //column.setHidden(true);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsResult.add(column);
         /**/


        Grid<BaseModelData> gridResult = new Grid<BaseModelData>(storeResult, new ColumnModel(configsResult));
        gridResult.setBorders(false);
        //gridResult.setAutoExpandColumn("time");
        gridResult.setBorders(true);
        gridResult.setHeight(600);

        return gridResult;
    }


    private String getMaxMin(String s) {
        String[] objects = (s.split("::")[1]).split(":");
        String maxObj = "";
        String currentObj = "";
        String currentOp = "";
        int max = 0;
        Log.info("--> " + s);
        // todo if not page and loaded
        for (int i = 0; i < objects.length; i++) {
            currentObj = objects[i].split("_")[0];
            if (!currentObj.equalsIgnoreCase("page")) {
                if (Integer.parseInt(objects[i].split("_")[1]) > max) {
                    max = Integer.parseInt(objects[i].split("_")[1]);
                    maxObj = currentObj;
                }
            }
        }
        return maxObj + "#" + getMin(s, max);  //To change body of created methods use File | Settings | File Templates.
    }

    private String getMin(String s, int max) {
        String[] objects = (s.split("::")[1]).split(":");
        String minObj = "";
        int min = max;
        for (int i = 0; i < objects.length; i++) {
            if (Integer.parseInt(objects[i].split("_")[1]) < min) {
                min = Integer.parseInt(objects[i].split("_")[1]);
                minObj = objects[i].split("_")[0];
            }
        }
        return minObj;  //To change body of created methods use File | Settings | File Templates.
    }

    private ToolBar getToolbar() {
        ToolBar toolbar = new ToolBar();
        Button pdfbutton = new Button("PDF");
        AdapterToolItem atipdfbutton = new AdapterToolItem(pdfbutton);
        toolbar.add(atipdfbutton);
        toolbar.add(new SeparatorToolItem());

        Button htmlbutton = new Button("HTML");
        AdapterToolItem atihtmlbutton = new AdapterToolItem(htmlbutton);
        toolbar.add(atihtmlbutton);
        toolbar.add(new SeparatorToolItem());

        final SelectionListener flushActionListener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {
                GWTAnalyticsService.App.getInstance().flushDatabase(new AsyncCallback<Void>() {
                    public void onFailure(Throwable throwable) {
                        //To change body of implemented methods use File | Settings | File Templates.
                        Info.display("", "Error when flushing the database");
                    }

                    public void onSuccess(Void aVoid) {
                        Info.display("", "Database successfully flushed");
                    }
                });
            }
        };

        Button flushbutton = new Button("Flush database");
        flushbutton.addSelectionListener(flushActionListener);
        AdapterToolItem atiflushbutton = new AdapterToolItem(flushbutton);
        toolbar.add(atiflushbutton);
        toolbar.add(new SeparatorToolItem());

        Button flushOldestbutton = new Button("Flush oldest");
        AdapterToolItem atiflushOldestbutton = new AdapterToolItem(flushOldestbutton);
        toolbar.add(atiflushOldestbutton);
        toolbar.add(new SeparatorToolItem());

        LabelToolItem labelNbOfLinesToDisplay = new LabelToolItem("Lines to display");
        AdapterToolItem atiNblabel = new AdapterToolItem(labelNbOfLinesToDisplay);
        toolbar.add(atiNblabel);
        final SimpleComboBox<Integer>[] nbrOfLinesToDisplay = new SimpleComboBox[1];
        nbrOfLinesToDisplay[0] = new SimpleComboBox<Integer>();
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 1; i <= 100; i++) {
            list.add(i);
        }
        nbrOfLinesToDisplay[0].add(list);
        nbrOfLinesToDisplay[0].setSimpleValue(20);
        nbrOfLinesToDisplay[0].addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<SimpleComboValue> se) {
                if (tabItemLastActivities.isVisible()) {
                    getLastNactivities(Integer.parseInt(nbrOfLinesToDisplay[0].getSimpleValue().toString()));
                } else if (tabItemMostActiveUsers.isVisible()) {
                    getMostNactiveUsers(Integer.parseInt(nbrOfLinesToDisplay[0].getSimpleValue().toString()));
                    //getLeastNactiveUsers(Integer.parseInt(nbrOfLinesToDisplay[0].getSimpleValue().toString()));
                } else if (tabItemPredefinedQuery.isVisible()) {
                    //executeQuery();
                } else if (tabItemErrors.isVisible()) {
                    getLastNSystemErrors(Integer.parseInt(nbrOfLinesToDisplay[0].getSimpleValue().toString()));
                }
                /* else if (tabItemLeastActiveUsers.isVisible()) {
                    getMostNactiveUsers(Integer.parseInt(nbrOfLinesToDisplay[0].getSimpleValue().toString()));
                    getLeastNactiveUsers(Integer.parseInt(nbrOfLinesToDisplay[0].getSimpleValue().toString()));
                }*/

            }

        });
        AdapterToolItem atiNbOfEntries = new AdapterToolItem(nbrOfLinesToDisplay[0]);
        toolbar.add(atiNbOfEntries);
        return toolbar;  //To change body of created methods use File | Settings | File Templates.
    }


    public TabPanel getTabs() {
        TabPanel tabPanel = new TabPanel();
        tabItemLastActivities = new TabItem("Last activities");
        tabItemLastActivities.setLayout(new FitLayout());
        tabItemLastActivities.add(getGridLastActivities());//, new RowData(.5, 1));
        tabPanel.add(tabItemLastActivities);

        tabItemErrors = new TabItem("Errors");
        tabItemErrors.setLayout(new FitLayout());
        tabItemErrors.add(getGridErrors(), new RowData(.5, 1));
        tabPanel.add(tabItemErrors);

        tabItemMostActiveUsers = new TabItem("Total activities per user");
        tabItemMostActiveUsers.setLayout(new FitLayout());
        tabItemMostActiveUsers.add(getGridMostNactiveUsers(), new RowData(.5, 1));
        tabPanel.add(tabItemMostActiveUsers);

        /*tabItemLeastActiveUsers = new TabItem("Least active users");
        tabItemLeastActiveUsers.setLayout(new FitLayout());
        tabItemLeastActiveUsers.add(getGridLeastNactiveUsers(), new RowData(.5, 1));
        tabPanel.add(tabItemLeastActiveUsers);*/

        tabItemPredefinedQuery = new TabItem("Advanced");
        tabItemPredefinedQuery.setLayout(new FitLayout());
        tabItemPredefinedQuery.add(getQueriesPanel());
        tabPanel.add(tabItemPredefinedQuery);

/*
        TabItem tabItemMostRequested = new TabItem("Most requested");
        tabItemMostRequested.setLayout(new FitLayout());
        tabItemMostRequested.add(getGridMostRequested(), new RowData(.5, 1));
        tabPanel.add(tabItemMostRequested);

        TabItem tabItemLeastRequested = new TabItem("Least requested");
        tabItemLeastRequested.setLayout(new FitLayout());
        tabItemLeastRequested.add(getGridLeastRequested(), new RowData(.5, 1));
        tabPanel.add(tabItemLeastRequested);*/


        return tabPanel;
    }

    private Grid getGridErrors() {
        List<ColumnConfig> config = new ArrayList<ColumnConfig>();
        ColumnConfig column = new ColumnConfig("time", "Time", 75);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        config.add(column);
        column = new ColumnConfig("code", "Code", 75);
        config.add(column);

        column = new ColumnConfig("msgUsr", "User message", 85);
        config.add(column);

        column = new ColumnConfig("msgSys", "System message", 250);
        config.add(column);

        column = new ColumnConfig("source", "Source", 250);
        config.add(column);

        Grid<BaseModelData> grid = new Grid<BaseModelData>(storeSystemErrors, new ColumnModel(config));
        grid.setBorders(false);
        grid.setAutoExpandColumn("msgSys");
        grid.setBorders(true);

        return grid;
    }


    private Grid getGridLeastNactiveUsers() {
        List<ColumnConfig> config = new ArrayList<ColumnConfig>();
        ColumnConfig column = new ColumnConfig("user", "User", 75);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        config.add(column);
        column = new ColumnConfig("operations", "Operations", 75);
        config.add(column);

        column = new ColumnConfig("lastaccess", "Last access", 75);
        column.setHidden(true);
        config.add(column);

        Grid<BaseModelData> grid = new Grid<BaseModelData>(storeLeastActiveUsers, new ColumnModel(config));
        grid.setBorders(false);
        grid.setAutoExpandColumn("lastaccess");
        grid.setBorders(true);

        return grid;
    }

    private Grid getGridMostNactiveUsers() {
        List<ColumnConfig> config = new ArrayList<ColumnConfig>();
        ColumnConfig column = new ColumnConfig("user", "User", 75);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        config.add(column);
        column = new ColumnConfig("operations", "Operations", 75);
        config.add(column);

        column = new ColumnConfig("lastaccess", "Last access", 75);
        column.setHidden(true);
        config.add(column);

        Grid<BaseModelData> grid = new Grid<BaseModelData>(storeMostActiveUsers, new ColumnModel(config));
        grid.setBorders(false);
        grid.setAutoExpandColumn("lastaccess");
        grid.setBorders(true);

        return grid;
    }

    private Grid getGridLeastRequested() {
        return getGridMostRequested();  //To change body of created methods use File | Settings | File Templates.
    }

    private Grid getGridMostRequested() {
        List<ColumnConfig> configsMostRequested = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("time", "Time", 100);
        column.setAlignment(Style.HorizontalAlignment.LEFT);
        configsMostRequested.add(column);

        column = new ColumnConfig();
        column.setId("user");
        column.setHeader("User");
        column.setWidth(200);
        configsMostRequested.add(column);

        column = new ColumnConfig();
        column.setId("operation");
        column.setHeader("Operation");
        column.setAlignment(Style.HorizontalAlignment.RIGHT);
        column.setWidth(75);
        configsMostRequested.add(column);

        column = new ColumnConfig();
        column.setId("type");
        column.setHeader("Type");
        column.setWidth(100);
        configsMostRequested.add(column);

        column = new ColumnConfig();
        column.setId("objectid");
        column.setHeader("Object Id");
        column.setWidth(200);
        configsMostRequested.add(column);

        column = new ColumnConfig();
        column.setId("id");
        column.setHeader("Id");
        column.setWidth(200);
        configsMostRequested.add(column);

        Grid<BaseModelData> gridLastTenActivities = new Grid<BaseModelData>(storeLastMostRequested, new ColumnModel(configsMostRequested));
        gridLastTenActivities.setBorders(false);
        gridLastTenActivities.setAutoExpandColumn("time");
        gridLastTenActivities.setBorders(true);
        return gridLastTenActivities;
    }

    private Grid getGridLastActivities() {

        List<ColumnConfig> configsLastTenActivities = new ArrayList<ColumnConfig>();
        ColumnConfig column = new ColumnConfig("time", "Time", 150);

        column.setId("time");
        column.setHeader("Time");
        column.setAlignment(Style.HorizontalAlignment.LEFT);
        configsLastTenActivities.add(column);
        column = new ColumnConfig("user", "User", 75);
        configsLastTenActivities.add(column);
        column = new ColumnConfig("operation", "Operation", 75);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsLastTenActivities.add(column);
        column = new ColumnConfig("type", "Type", 75);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsLastTenActivities.add(column);
        column = new ColumnConfig("objectid", "Object Id", 75);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsLastTenActivities.add(column);

        column = new ColumnConfig("hits", "Hits", 75);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsLastTenActivities.add(column);


        column = new ColumnConfig("pid", "Page id", 75);
        //column.setHidden(true);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsLastTenActivities.add(column);

        column = new ColumnConfig("uuid", "UUID", 75);
        column.setHidden(true);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsLastTenActivities.add(column);

        column = new ColumnConfig("sid", "Site id", 75);
        //column.setHidden(true);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        configsLastTenActivities.add(column);

        Grid<BaseModelData> gridLastTenActivities = new Grid<BaseModelData>(storeLastNactivities, new ColumnModel(configsLastTenActivities));
        gridLastTenActivities.setBorders(false);
        //gridLastTenActivities.setAutoExpandColumn("time");
        gridLastTenActivities.setBorders(true);

        return gridLastTenActivities;
    }

    //private String extractToHTML


}
