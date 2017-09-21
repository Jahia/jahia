/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaChannel;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.SearchField;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author rincevent
 */
@SuppressWarnings("serial")
public class CustomizedPreviewActionItem extends BaseActionItem {
    private transient SearchField userSearchField;
    private transient String lastUserSearchValue;
    private transient Grid<GWTJahiaNode> userGrid;
    private transient String defaultWindowOptions = "resizable=yes,scrollbars=yes";

    @Override
    public void onComponentSelection() {
        final Window window = new Window();
        window.addStyleName("customized-preview");
        window.setSize(500, 430);
        window.setPlain(true);
        window.setModal(true);
        window.setBlinkModal(true);
        window.setBorders(false);
        window.setHeadingHtml(Messages.get("label.preview.window.title", "Customized preview"));
        window.setLayout(new FitLayout());
        window.setButtonAlign(Style.HorizontalAlignment.CENTER);
        window.setBodyBorder(false);

        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaNode>> proxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object pageLoaderConfig, AsyncCallback<PagingLoadResult<GWTJahiaNode>> callback) {
                if (userSearchField != null) {
                    String newSearch = userSearchField.getText().trim().replace("'", "''");

                    String query = "select * from [jnt:user] as u where (isdescendantnode(u,'/users/') or isdescendantnode(u,'/sites/" + JahiaGWTParameters.getSiteKey().replace("'", "''") + "/users/'))";
                    if (newSearch.length() > 0) {
                        query += " and (CONTAINS(u.*,'*" + newSearch + "*') OR LOWER(u.[j:nodename]) LIKE '*" + newSearch.toLowerCase() + "*') ";
                    }
                    query += " ORDER BY u.[j:nodename]";

                    // reset offset to 0 if the search value has changed
                    int offset = lastUserSearchValue != null && lastUserSearchValue.equals(newSearch) ? ((PagingLoadConfig) pageLoaderConfig).getOffset() : 0;

                    final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

                    service.searchSQL(query, ((PagingLoadConfig) pageLoaderConfig).getLimit(), offset, null, GWTJahiaNode.DEFAULT_USER_FIELDS, false, callback);

                    // remember last searched value
                    lastUserSearchValue = newSearch;
                }
            }
        };
        final BasePagingLoader<PagingLoadResult<GWTJahiaNode>> loader = new BasePagingLoader<PagingLoadResult<GWTJahiaNode>>(
                proxy);
        userSearchField = new SearchField(Messages.get("label.search", "Search: "), false) {

            @Override
            public void onFieldValidation(String value) {
                loader.load();
            }

            @Override
            public void onSaveButtonClicked(String value) {

            }
        };
        userSearchField.setWidth(490);
        loader.setLimit(10);
        loader.load();
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(userSearchField);

        ListStore<GWTJahiaNode> store = new ListStore<GWTJahiaNode>(loader);

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("displayName", Messages.get("label.username", "User name"), 150));
        columns.add(new ColumnConfig("j:lastName", Messages.get("label.lastName", "Last name"), 130));
        columns.add(new ColumnConfig("j:firstName", Messages.get("label.firstName", "First name"), 130));
//        columns.add(new ColumnConfig("siteName", "Site name", 80));
        columns.add(new ColumnConfig("providerKey", Messages.get("column.provider.label", "Provider"), 75));
//        columns.add(new ColumnConfig("email", "Email", 100));

        ColumnModel cm = new ColumnModel(columns);

        final PagingToolBar toolBar = new PagingToolBar(10);
        toolBar.bind(loader);
        toolBar.setBorders(false);
        toolBar.setWidth(480);
        userGrid = new Grid<GWTJahiaNode>(store, cm);
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
        Label label = new Label("&nbsp;&nbsp;&nbsp;&nbsp;" + Messages.get("label.preview.window.date", "Pick a date for the preview") + "&nbsp;&nbsp;&nbsp;");
        label.setWidth(200);
        label.setLabelFor("previewDate");
        final Date date = new Date();
        final CalendarField calendarField = new CalendarField("yyyy-MM-dd HH:mm", true, false, "previewDate", false, date);
        panel.add(label);
        panel.add(calendarField);
        verticalPanel.add(panel);
        window.add(verticalPanel);

        // Channel selector
        panel = new HorizontalPanel();
        panel.setTableHeight("30px");
        panel.setVerticalAlign(Style.VerticalAlignment.BOTTOM);
        label = new Label("&nbsp;&nbsp;&nbsp;&nbsp;" + Messages.get("label.preview.window.channel", "Channel") + "&nbsp;&nbsp;&nbsp;");
        label.setLabelFor("previewChannel");

        // we will setup the right elements now because we will need to reference them in the event listener
        final Label orientationLabel = new Label("&nbsp;&nbsp;&nbsp;&nbsp;" + Messages.get("label.preview.window.channelOrientation", "Orientation") + "&nbsp;&nbsp;&nbsp;");
        orientationLabel.setLabelFor("previewChannelOrientation");
        orientationLabel.hide();
        final ListStore<GWTJahiaBasicDataBean> orientations = new ListStore<GWTJahiaBasicDataBean>();
        final ComboBox<GWTJahiaBasicDataBean> orientationCombo = new ComboBox<GWTJahiaBasicDataBean>();
        orientationCombo.setDisplayField("displayName");
        orientationCombo.setName("previewChannelOrientation");
        orientationCombo.setStore(orientations);
        orientationCombo.setTypeAhead(true);
        orientationCombo.setTriggerAction(TriggerAction.ALL);
        orientationCombo.hide();

        // now we can setup the channel selection combo box
        final ListStore<GWTJahiaChannel> states = new ListStore<GWTJahiaChannel>();
        final ComboBox<GWTJahiaChannel> combo = new ComboBox<GWTJahiaChannel>();
        JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
        contentService.getChannels(new BaseAsyncCallback<List<GWTJahiaChannel>>() {

            public void onSuccess(List<GWTJahiaChannel> result) {
                states.add(result);
                combo.setStore(states);
            }
        });
        combo.setDisplayField("display");
        combo.setName("previewChannel");
        combo.setStore(states);
        combo.setTypeAhead(true);
        combo.setTriggerAction(TriggerAction.ALL);
        combo.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaChannel>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaChannel> event) {
                GWTJahiaChannel selectedChannel = event.getSelectedItem();
                Map<String, String> capabilities = selectedChannel.getCapabilities();
                if (capabilities != null && capabilities.containsKey("variants")) {
                    String[] variants = capabilities.get("variants").split(",");
                    String[] displayNames = null;
                    if (capabilities.containsKey("variants-displayNames")) {
                        displayNames = capabilities.get("variants-displayNames").split(",");
                    }
                    ListStore<GWTJahiaBasicDataBean> orientations = orientationCombo.getStore();
                    orientations.removeAll();
                    for (int i = 0; i < variants.length; i++) {
                        String displayName = (displayNames == null ? variants[i] : displayNames[i]);
                        orientations.add(new GWTJahiaBasicDataBean(variants[i], displayName));
                    }
                    orientationCombo.setValue(orientations.getAt(0));
                    orientationLabel.show();
                    orientationCombo.show();
                } else {
                    orientationLabel.hide();
                    orientationCombo.hide();
                }
            }
        });
        panel.add(label);
        panel.add(combo);
        panel.add(orientationLabel);
        panel.add(orientationCombo);
        verticalPanel.add(panel);
        window.add(verticalPanel);

        Button ok = new Button(Messages.get("label.preview.window.confirm", "Show customized preview"));
        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                final GWTJahiaNode node = linker.getSelectionContext().getMainNode();
                if (node != null) {
                    String path = node.getPath();
                    String locale = JahiaGWTParameters.getLanguage();
                    JahiaContentManagementService.App.getInstance().getNodeURL(null, path, null, null, "default",
                            locale, false, new BaseAsyncCallback<String>() {

                        @Override
                        public void onSuccess(String url) {
                            GWTJahiaNode selectedItem = userGrid.getSelectionModel().getSelectedItem();
                            String alias = null;
                            List<String> urlParameters = new ArrayList<String>();
                            if (selectedItem != null) {
                                alias = "alias=" + selectedItem.getName();
                                urlParameters.add(alias);
                            }
                            String previewDate = null;
                            if (calendarField.getValue().after(date)) {
                                previewDate = "prevdate=" + calendarField.getValue().getTime();
                                urlParameters.add(previewDate);
                            }
                            GWTJahiaChannel channel = combo.getValue();
                            String windowFeatures = null;
                            if ((channel != null) && (!"default".equals(channel.getValue()))) {
                                urlParameters.add("channel=" + channel.getValue());
                                Map<String, String> capabilities = channel.getCapabilities();
                                if (capabilities != null && capabilities.containsKey("variants")) {
                                    int variantIndex = 0;
                                    String[] variants = capabilities.get("variants").split(",");
                                    String variant = orientationCombo.getValue().getValue();
                                    urlParameters.add("variant=" + variant);
                                    for (int i = 0; i < variants.length; i++) {
                                        if (variants[i].equals(variant)) {
                                            variantIndex = i;
                                            break;
                                        }
                                    }
                                    int[] imageSize = channel.getVariantDecoratorImageSize(variantIndex);
                                    windowFeatures = "resizable=no,status=no,menubar=no,toolbar=no,width=" + imageSize[0] +
                                            ",height=" + imageSize[1];
                                }
                            }
                            StringBuilder urlParams = new StringBuilder();
                            for (int i = 0; i < urlParameters.size(); i++) {
                                if (i == 0) {
                                    urlParams.append("?");
                                }
                                urlParams.append(urlParameters.get(i));
                                if (i < urlParameters.size()-1) {
                                    urlParams.append("&");
                                }
                            }
                            String url1 = url + urlParams.toString();

                            openWindow(url1, "customizedpreview", windowFeatures != null ? windowFeatures : defaultWindowOptions);
                        }

                    });
                }
            }
        });
        window.setBottomComponent(ok);
        window.show();
    }

    public static native void openWindow(String url, String name, String features) /*-{
        if (typeof(previewWindow) != "undefined") {
            previewWindow.close();
        }
        previewWindow = $wnd.open(url, name, features);
    }-*/;

    public void setDefaultWindowOptions(String defaultWindowOptions) {
        this.defaultWindowOptions = defaultWindowOptions;
    }

}
