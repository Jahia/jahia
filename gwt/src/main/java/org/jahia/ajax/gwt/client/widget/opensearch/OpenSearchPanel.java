/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.opensearch;

import org.jahia.ajax.gwt.client.data.opensearch.GWTJahiaOpenSearchEngine;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 26 oct. 2008
 * Time: 08:53:28
 * To change this template use File | Settings | File Templates.
 */
public class OpenSearchPanel extends ContentPanel {

    private TextField<String> searchField;
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

        ToolBar toolbar = new ToolBar();
        toolbar.setHeight(28);

        searchField = new TextField<String>();
        if (this.initialSearchTerms != null){
            searchField.setValue(this.initialSearchTerms);
        }

        toolbar.add(searchField);

        Button searchButton = new Button("search");
        toolbar.add(searchButton);
        searchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                onSearchClick(event);
            }
        });

        toolbar.add(new FillToolItem());

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