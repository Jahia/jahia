/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.node;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.util.nodes.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.SearchField;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.List;

/**
 * User: rfelden
 * Date: 16 sept. 2008 - 09:46:42
 */
public class FilesView extends TopRightComponent {

    private FileTable fileTable;
    private ThumbView thumbView;

    private ThumbView detailedThumbView;

    private ContentPanel m_component;
    private TopRightComponent current;
    private SearchField searchField;

    private FileListContextMenu contextMenu;

    private ManagerConfiguration configuration;

    private List<GWTJahiaNode> searchResults = null;

    public FilesView(ManagerConfiguration config) {
        configuration = config;
        fileTable = new FileTable(config);
        thumbView = new ThumbView(config);
        detailedThumbView = new ThumbView(config) {
            @Override
            public native String getTemplate() /*-{
        return ['<tpl for=".">',
                '<div style="padding: 0 0 10px 0;margin-bottom: 12px;border-bottom: 1px solid #D9E2F4;float: left;width: 100%;" class="thumb-wrap" id="{name}">',
                '<div style="width: 140px; float: left; text-align: center;" class="thumb"><img src="{preview}" title="{name}"></div>',
                '<div style="margin-left: 150px; margin-right: 110px;"><p style="font:bold;">{name}</p><br/><p> {description}</p></div></div>',
                '</tpl>',
                '<div class="x-clear"></div>'].join("");
    }-*/;
        };
        m_component = new ContentPanel(new FitLayout());
        m_component.setHeaderVisible(false);
        m_component.setBorders(false);
        m_component.setBodyBorder(false);
        searchField = new SearchField(Messages.getResource("fm_search") + ": ", true) {
            public void onFieldValidation(String value) {
                setSearchContent(value);
            }

            public void onSaveButtonClicked(String value) {
                if (value != null && value.length() > 0) {
                    String name = Window.prompt(Messages.getNotEmptyResource("fm_search_warning_entername","Please enter a name for this search"), JCRClientUtils.cleanUpFilename(value));
                    if (name != null && name.length() > 0) {
                        name = JCRClientUtils.cleanUpFilename(name);
                        final JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance();
                        service.saveSearch(value, name, new AsyncCallback<GWTJahiaNode>() {
                            public void onFailure(Throwable throwable) {
                                Log.error("error", throwable);
                            }

                            public void onSuccess(GWTJahiaNode o) {
                                Log.debug("saved.");
                                saveSearch(o);
                            }
                        });
                    } else {
                        Window.alert(Messages.getNotEmptyResource("fm_search_error_nameinvalid","The entered name is invalid"));
                    }
                }
            }
        };
        m_component.setTopComponent(searchField);

        // set default view
        if (config.getDefaultView() == JCRClientUtils.FILE_TABLE) {
            current = fileTable;
        } else if (config.getDefaultView() == JCRClientUtils.THUMB_VIEW) {
            current = thumbView;
        }else if (config.getDefaultView() == JCRClientUtils.DETAILED_THUMB_VIEW) {
            current = detailedThumbView;
        } else {
            current = fileTable;          
        }
        m_component.add(current.getComponent());

    }

    public void switchToListView() {
        if (current != fileTable) {
            clearTable();
            m_component.removeAll();
            //fileTable = new FileTable(configuration) ;
            current = fileTable;
            current.setContextMenu(contextMenu);
            //current.initWithLinker(getLinker());
            m_component.add(current.getComponent());
            m_component.layout();

            if (searchResults == null) {
                refresh();
            } else {
                current.setProcessedContent(searchResults);
            }
            getLinker().handleNewSelection();
        }
    }

    public void switchToThumbView() {
        if (current != thumbView) {
            clearTable();
            m_component.removeAll();
            current = thumbView;
            current.setContextMenu(contextMenu);
            m_component.add(current.getComponent());
            m_component.layout();

            if (searchResults == null) {
                refresh();
            } else {
                current.setProcessedContent(searchResults);
            }
            getLinker().handleNewSelection();
        }
    }

    public void switchToDetailedThumbView() {
        if (current != detailedThumbView) {
            clearTable();
            m_component.removeAll();
            current = detailedThumbView;
            current.setContextMenu(contextMenu);
            m_component.add(current.getComponent());
            m_component.layout();

            if (searchResults == null) {
                refresh();
            } else {
                current.setProcessedContent(searchResults);
            }
            getLinker().handleNewSelection();
        }
    }

    public void setSearchContent(String text) {
        clearTable();
        if (text != null && text.length() > 0) {
            final JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance();
            if (getLinker() != null) {
                getLinker().loading("searching content...");
            }
            service.search(text, 0, new AsyncCallback<List<GWTJahiaNode>>() {
                public void onFailure(Throwable throwable) {
                    Window.alert("Element list retrieval failed :\n" + throwable.getLocalizedMessage());
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                }

                public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                    if (gwtJahiaNodes != null) {
                        searchResults = gwtJahiaNodes;
                        current.setProcessedContent(gwtJahiaNodes);
                    } else {
                        searchResults = null;
                    }
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                }
            });
        } else {
            refresh();
        }
    }

    public void saveSearch(GWTJahiaNode query) {
        ((FolderTree) getLinker().getLeftObject()).addSavedSearch(query, true);
        searchField.clear();
        searchResults = null;
    }

    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        fileTable.initWithLinker(linker);
        thumbView.initWithLinker(linker);
        detailedThumbView.initWithLinker(linker);
    }

    public void initContextMenu() {
        contextMenu = new FileListContextMenu(getLinker(), configuration);
        current.setContextMenu(contextMenu);
    }

    public void setContent(Object root) {
        if (current != null) {
            searchField.clear();
            current.setContent(root);
            getLinker().getBottomRightObject().fillData(root);
        }
    }

    public void setProcessedContent(Object content) {
        if (current != null) {
            current.setProcessedContent(content);
        }
    }

    public void clearTable() {
        if (current != null) {
            current.clearTable();
        }
    }

    public Object getSelection() {
        if (current != null) {
            return current.getSelection();
        } else {
            return null;
        }
    }

    public void refresh() {
        if (current != null) {
            current.refresh();
        }
    }

    public Component getComponent() {
        return m_component;
    }
}
