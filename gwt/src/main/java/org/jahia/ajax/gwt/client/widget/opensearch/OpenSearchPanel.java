/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.opensearch;

import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngine;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.google.gwt.user.client.Element;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 26 oct. 2008
 * Time: 08:53:28
 * To change this template use File | Settings | File Templates.
 */
public class OpenSearchPanel extends ContentPanel {

    private ToolBar toolbar;
    private TextField searchField;
    private OpenSearchWidgetItem openSearchWidgetItem;
    private String initialSearchTerms;

    /**
     *
     * @param openSearchWidgetItem
     * @param initialSearchTerms
     */
    public OpenSearchPanel(OpenSearchWidgetItem openSearchWidgetItem, String initialSearchTerms) {
        super(new FitLayout());
        this.openSearchWidgetItem = openSearchWidgetItem;
        this.initialSearchTerms = initialSearchTerms;
    }

    /**
     *
     * @param layout
     * @param openSearchWidgetItem
     * @param initialSearchTerms
     */
    public OpenSearchPanel(Layout layout, OpenSearchWidgetItem openSearchWidgetItem, String initialSearchTerms) {
        super(layout);
        this.openSearchWidgetItem = openSearchWidgetItem;
        this.initialSearchTerms = initialSearchTerms;
    }

    /**
     *
     * @param searchEngine
     * @param rssFeedMode
     */
    public OpenSearchPanel(GWTJahiaOpenSearchEngine searchEngine, boolean rssFeedMode) {
        this(searchEngine,null,rssFeedMode);
    }

    public OpenSearchPanel(GWTJahiaOpenSearchEngine searchEngine, String searchTerms, boolean rssFeedMode) {
        super(new FitLayout());
        openSearchWidgetItem = new OpenSearchWidgetItem(searchEngine);
        openSearchWidgetItem.setRssFeedMode(rssFeedMode);
        this.initialSearchTerms = searchTerms;
    }

    public OpenSearchWidgetItem getOpenSearchWidgetItem() {
        return openSearchWidgetItem;
    }

    @Override
    protected void onRender(Element element, int i) {
        initWidget();
        super.onRender(element, i);
    }

    private void initWidget(){
        setSize(500, 300);
        setHeading(this.openSearchWidgetItem.getName());
        setLayout(new FitLayout());

        toolbar = new ToolBar() ;
        toolbar.setHeight(28);

        searchField = new TextField();
        if (this.initialSearchTerms != null){
            searchField.setValue(this.initialSearchTerms);
        }

        ToolItem toolItem = new AdapterToolItem(searchField);
        toolbar.add(toolItem);

        TextToolItem searchButton = new TextToolItem("search");
        toolbar.add(searchButton);
        searchButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                onSearchClick(event);
            }
        });

        toolItem = new FillToolItem();
        toolbar.add(toolItem);

        setTopComponent(toolbar);
        add(openSearchWidgetItem);

        executeInitialSearch();
        /*
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                executeInitialSearch();
            };
        });
        */
    }

    private void executeInitialSearch(){
        if (searchField.getValue() != null && !"".endsWith((String)searchField.getValue())){
            openSearchWidgetItem.search((String)searchField.getValue());
            //this.layout();
        }
    }

    private void onSearchClick(ComponentEvent ce){
        this.openSearchWidgetItem.search(searchField.getValue().toString());
    }

}