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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.UserManagerService;
import org.jahia.ajax.gwt.client.service.UserManagerServiceAsync;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.SearchField;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 8/30/11
 */
public class CustomizedPreviewActionItem extends BaseActionItem {
    private transient SearchField userSearchField;
    private transient Grid<GWTJahiaUser> userGrid;

    @Override
    public void onComponentSelection() {
        final Window window = new Window();
        window.setSize(500, 400);
        window.setPlain(true);
        window.setModal(true);
        window.setBlinkModal(true);
        window.setBorders(false);
        window.setHeading(Messages.get("label.preview.window.title", "Customized preview"));
        window.setLayout(new FitLayout());
        window.setButtonAlign(Style.HorizontalAlignment.CENTER);
        window.setBodyBorder(false);

        final UserManagerServiceAsync service = UserManagerService.App.getInstance();
        // data proxy
        RpcProxy<BasePagingLoadResult<GWTJahiaUser>> proxy = new RpcProxy<BasePagingLoadResult<GWTJahiaUser>>() {
            @Override
            protected void load(Object pageLoaderConfig, AsyncCallback<BasePagingLoadResult<GWTJahiaUser>> callback) {
                if (userSearchField != null) {
                    if (userSearchField.getText().length() == 0) {
                        service.searchUsersInContext("*", ((PagingLoadConfig) pageLoaderConfig).getOffset(),
                                ((PagingLoadConfig) pageLoaderConfig).getLimit(), "currentSite", callback);
                    } else {
                        service.searchUsersInContext("*" + userSearchField.getText() + "*",
                                ((PagingLoadConfig) pageLoaderConfig).getOffset(),
                                ((PagingLoadConfig) pageLoaderConfig).getLimit(), "currentSite", callback);
                    }
                }
            }
        };
        final BasePagingLoader<PagingLoadResult<GWTJahiaUser>> loader = new BasePagingLoader<PagingLoadResult<GWTJahiaUser>>(
                proxy);
        userSearchField = new SearchField(Messages.get("label.search", "Search: "), false) {
            public void onFieldValidation(String value) {
                loader.load();
            }

            public void onSaveButtonClicked(String value) {

            }
        };
        userSearchField.setWidth(490);
        loader.setLimit(10);
        loader.load();
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(userSearchField);

        ListStore<GWTJahiaUser> store = new ListStore<GWTJahiaUser>(loader);

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("display", Messages.get("label.username", "User name"), 150));
        columns.add(new ColumnConfig("j:lastName", Messages.get("org.jahia.admin.lastName.label", "Last name"), 130));
        columns.add(new ColumnConfig("j:firstName", Messages.get("org.jahia.admin.firstName.label", "First name"), 130));
//        columns.add(new ColumnConfig("siteName", "Site name", 80));
        columns.add(new ColumnConfig("provider", Messages.get("column.provider.label", "Provider"), 75));
//        columns.add(new ColumnConfig("email", "Email", 100));

        ColumnModel cm = new ColumnModel(columns);

        final PagingToolBar toolBar = new PagingToolBar(10);
        toolBar.bind(loader);
        toolBar.setBorders(false);
        toolBar.setWidth(480);
        userGrid = new Grid<GWTJahiaUser>(store, cm);
        userGrid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        userGrid.setLoadMask(true);
        userGrid.setBorders(false);
        userGrid.setHeight(250);
        userGrid.setWidth(490);
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(userGrid);
        verticalPanel.add(toolBar);
        window.setTopComponent(panel);

        panel = new HorizontalPanel();
        panel.setTableHeight("30px");
        panel.setVerticalAlign(Style.VerticalAlignment.BOTTOM);
        Label label = new Label("&nbsp;&nbsp;&nbsp;&nbsp;"+Messages.get("label.preview.window.date", "Pick a date for the preview")+"&nbsp;&nbsp;&nbsp;");
        label.setLabelFor("previewDate");
        final Date date = new Date();
        final CalendarField calendarField = new CalendarField("yyyy-MM-dd HH:mm", true, false, "previewDate", false, date);
        panel.add(label);
        panel.add(calendarField);
        verticalPanel.add(panel);
        window.add(verticalPanel);

        Button ok = new Button(Messages.get("label.preview.window.confirm","Show customized preview"));
        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                final GWTJahiaNode node = linker.getSelectionContext().getMainNode();
                if (node != null) {
                    String path = node.getPath();
                    String locale = JahiaGWTParameters.getLanguage();
                    JahiaContentManagementService.App.getInstance().getNodeURL(null, path, null, null, "default",
                            locale, new BaseAsyncCallback<String>() {
                        public void onSuccess(String url) {
                            GWTJahiaUser selectedItem = userGrid.getSelectionModel().getSelectedItem();
                            String alias = null;
                            if(selectedItem!=null) {
                                alias = "alias="+selectedItem.getUsername();
                            }
                            String previewDate= null;
                            if(calendarField.getValue().after(date)) {
                                previewDate = "prevdate="+calendarField.getValue().getTime();
                            }
                            String urlParams="";
                            if(alias!=null && previewDate!=null) {
                                urlParams = "?"+alias+"&"+previewDate;
                            } else if(alias!=null) {
                                urlParams = "?"+alias;
                            } else if (previewDate!=null) {
                                urlParams = "?"+previewDate;
                            }
                            String url1 = url + urlParams;
                            com.google.gwt.user.client.Window.open(url1, "customizedpreview", "");
                        }

                    });
                }
            }
        });
        window.setBottomComponent(ok);
        window.show();
    }
}
