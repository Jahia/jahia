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
package org.jahia.ajax.gwt.client.widget.content;

import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;
import org.jahia.ajax.gwt.client.widget.language.LanguageSwitcher;
import org.jahia.ajax.gwt.client.widget.language.LanguageSelectedListener;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItemItf;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ContentActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarLayoutContainer;
import org.jahia.ajax.gwt.client.widget.toolbar.handler.ManagerSelectionHandler;
import org.jahia.ajax.gwt.client.util.content.actions.*;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.service.JahiaServiceAsync;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 14:17:12
 */
public class ContentToolbar extends TopBar {

    private LayoutContainer m_component;

    private ManagerConfiguration configuration ;

    public ContentToolbar(ManagerConfiguration config) {
        configuration = config ;
        m_component = new LayoutContainer(new RowLayout());
        createUi();
    }

    private void createDynamicUi() {
      m_component = new ActionToolbarLayoutContainer(){
            @Override
            public void afterToolbarLoading() {
               createUi();
            }
        };
        ((ActionToolbarLayoutContainer)m_component).initWithLinker(getLinker());
    }

    private void createUi() {
        //m_component = new LayoutContainer(new RowLayout());

        ToolBar menus = new ToolBar();
        ToolBar shortcuts = new ToolBar();
        //menus.setHeight(21);

        // refresh item not bound to any configuration
        ContentActionItem refresh = new ContentActionItem(Messages.getResource("fm_refresh"), "fm-refresh") {
            public void onSelection() {
                getLinker().refreshTable();
            }
        };

        // toolbar createion
        for (ActionItemItf item: configuration.getItems()) {
            Component b = item.getTextToolitem();
            if (b != null) {
                shortcuts.add(b);
            } else {
                shortcuts.add(new SeparatorToolItem());
            }

        }
        shortcuts.add(new SeparatorToolItem()) ;
        shortcuts.add(refresh.getTextToolitem()) ;

        // text menu creation
        if (configuration.isEnableTextMenu() && configuration.getGroupedItems().size() > 0) {
            for (ContentActionItemGroup group: configuration.getGroupedItems()) {
                final Menu menu = new Menu() ;
                for (ActionItemItf item: group.getItems()) {
                    menu.add(item.getMenuItem()) ;
                }

                if (Messages.getResource("fm_remoteMenu").equals(group.getGroupLabel())) {
                    JahiaContentManagementService.App.getInstance().getStoredPasswordsProviders(new AsyncCallback<Map<String, String>>() {
                        public void onSuccess(Map<String, String> map) {
                            final String username = map.get(null);
                            for (final String key : map.keySet()) {
                                if (key != null) {

                                    final String loginLabel = Messages.getResource("fm_login") + " " + key;
                                    final String logoutLabel = Messages.getResource("fm_logout") + " " + key;

                                    final MenuItem item = new MenuItem(loginLabel);
                                    if (map.get(key) != null) {
                                        item.setText(logoutLabel);
                                    } else {
                                        item.setText(loginLabel);
                                    }

                                    item.addSelectionListener(new SelectionListener<MenuEvent>() {
                                        public void componentSelected(MenuEvent event) {
                                            if (item.getText().equals(loginLabel)) {
                                                new PasswordPrompt(getLinker(), username, key, item, logoutLabel).show();
                                            } else {
                                                JahiaContentManagementService.App.getInstance().storePasswordForProvider(key, null, null, new AsyncCallback() {
                                                    public void onSuccess(Object o) {
                                                        item.setText(loginLabel);
                                                        getLinker().refreshAll();
                                                    }

                                                    public void onFailure(Throwable throwable) {
                                                        Log.error(Messages.getResource("fm_fail"), throwable);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                    menu.add(item);
                                }
                            }
                        }

                        public void onFailure(Throwable throwable) {
                            Log.error("error", throwable);
                        }
                    });

                }


                Button mMenu = new Button(group.getGroupLabel()) ;
                mMenu.setMenu(menu);
                menus.add(mMenu) ;
            }

            // add the views menu (not part of the config)
            MenuItem list = new MenuItem(Messages.getResource("fm_list"), new SelectionListener<MenuEvent>() {
                public void componentSelected(MenuEvent event) {
                    setListView();
                }
            });
            list.setIconStyle("fm-tableview");
            MenuItem thumbs = new MenuItem(Messages.getResource("fm_thumbs"), new SelectionListener<MenuEvent>() {
                public void componentSelected(MenuEvent event) {
                    setThumbView();
                }
            });
            thumbs.setIconStyle("fm-iconview");
            MenuItem detailedThumbs = new MenuItem(Messages.getResource("fm_icons_detailed"), new SelectionListener<MenuEvent>() {
                public void componentSelected(MenuEvent event) {
                    setDetailedThumbView();
                }
            });
            detailedThumbs.setIconStyle("fm-iconview-detailed");
            MenuItem templates = new MenuItem("Template view", new SelectionListener<MenuEvent>() {
                public void componentSelected(MenuEvent event) {
                    setTemplateView();
                }
            });
            templates.setIconStyle("fm-templateview");
            Menu menu = new Menu() ;
            menu.add(refresh.getMenuItem()) ;
            menu.add(new SeparatorMenuItem()) ;
            menu.add(list) ;
            menu.add(thumbs) ;
            Button mMenu = new Button(Messages.getResource("fm_viewMenu")) ;
            menu.add(templates) ;
            mMenu.setMenu(menu);
            menus.add(mMenu) ;

            LanguageSwitcher languageSwitcher = new LanguageSwitcher(true, true, false, false, JahiaGWTParameters.getLanguage(), false, new LanguageSelectedListener() {
                private final JahiaServiceAsync jahiaServiceAsync = JahiaService.App.getInstance();

                public void onLanguageSelected(String languageSelected) {
                    jahiaServiceAsync.changeLocaleForAllPagesAndEngines(languageSelected, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                        }

                        public void onSuccess(Object o) { // TODO chained rpc calls are ugly... well, not so bad after all
                            Window.Location.reload();
                        }
                    });
                }
            });
            Button item = new Button("loading...") ;
            item.setEnabled(false);
            languageSwitcher.init(item);

            menus.add(item);


            m_component.add(menus) ;
        }

        m_component.add(shortcuts);
    }

    // override to handle view switching
   /* protected void switchView(Button switchView) {
    }*/

    protected void setListView() {
    }

    protected void setThumbView() {
    }

    protected void setDetailedThumbView(){
    }

    protected void setTemplateView(){
    }

    public void handleNewSelection(Object leftTreeSelection, Object topTableSelectionEl) {
        List<GWTJahiaNode> topTableSelection = (List<GWTJahiaNode>) topTableSelectionEl;

        boolean isTreeSelection = leftTreeSelection != null ;
        boolean isParentWriteable = (isTreeSelection) ? (((GWTJahiaNode) leftTreeSelection).isWriteable() && !((GWTJahiaNode) leftTreeSelection).isLocked()) : false;
        boolean isWritable = false;
        boolean isDeleteable = false;
        boolean isLockable = false;
        boolean isLocked = false;
        boolean isSingleFile = false;
        boolean isSingleFolder = false;
        boolean isPasteAllowed = isTreeSelection ? CopyPasteEngine.getInstance().canCopyTo((GWTJahiaNode) leftTreeSelection) : false ;
        boolean isZip = false ;
        boolean isImage = false ;
        boolean isTableSelection = false ;
        boolean isMount = false ;
        if (topTableSelection != null && topTableSelection.size() > 0) {
            if (leftTreeSelection != null) {
                isTreeSelection = true ;
            }
            if (!isTreeSelection) {
                GWTJahiaNode parent = (GWTJahiaNode) topTableSelection.get(0).getParent() ;
                if (parent != null) {
                    isParentWriteable = parent.isWriteable();
                }
            }
            isTableSelection = true ;
            isWritable = true;
            isDeleteable = true;
            isLockable = true;
            isLocked = true;
            for (GWTJahiaNode gwtJahiaNode : topTableSelection) {
                isWritable &= gwtJahiaNode.isWriteable() && !gwtJahiaNode.isLocked();
                isDeleteable &= gwtJahiaNode.isDeleteable() && !gwtJahiaNode.isLocked();
                isLockable &= gwtJahiaNode.isLockable();
                isLocked &= gwtJahiaNode.isLocked();
            }
            if (topTableSelection.size() == 1) {
                isSingleFile = topTableSelection.get(0).isFile();
                isSingleFolder = !isSingleFile;
            }
            if (isSingleFolder) {
                isMount = topTableSelection.get(0).getInheritedNodeTypes().contains("jnt:mountPoint")  || topTableSelection.get(0).getNodeTypes().contains("jnt:mountPoint");
            }
            if (!isTreeSelection) {
                if (isSingleFolder) {
                    isPasteAllowed = CopyPasteEngine.getInstance().canCopyTo(topTableSelection.get(0)) ;
                } else {
                    isPasteAllowed = CopyPasteEngine.getInstance().canCopyTo((GWTJahiaNode) topTableSelection.get(0).getParent()) ;
                }
            }
            int extIndex = topTableSelection.get(0).getName().lastIndexOf(".") ;
            if (extIndex > 0 && topTableSelection.get(0).getName().substring(extIndex).equalsIgnoreCase(".zip")) {
                isZip = true ;
            }
            isImage = topTableSelection.get(0).getNodeTypes().contains("jmix:image") ;
        }
        
        for (ContentActionItemGroup group: configuration.getGroupedItems()) {
            for (ActionItemItf item: group.getItems()) {
                if (item instanceof ManagerSelectionHandler) {
                    ((ManagerSelectionHandler)item).enableOnConditions(isTreeSelection, isTableSelection, isWritable, isDeleteable, isParentWriteable, isSingleFile, isSingleFolder, isPasteAllowed, isLockable, isLocked, isZip, isImage, isMount);
                }
            }
        }
        
        //m_component.enableOnConditions(isTreeSelection, isTableSelection, isWritable, isDeleteable, isParentWriteable, isSingleFile, isSingleFolder, isPasteAllowed, isLockable, isLocked, isZip, isImage, isMount);

    }

    public Component getComponent() {
        return m_component;
    }

}
