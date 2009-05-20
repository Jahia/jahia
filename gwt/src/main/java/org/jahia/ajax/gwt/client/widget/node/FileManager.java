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
package org.jahia.ajax.gwt.client.widget.node;

import org.jahia.ajax.gwt.client.widget.tripanel.*;

import org.jahia.ajax.gwt.client.widget.node.FileToolbar;
import org.jahia.ajax.gwt.client.widget.node.FileStatusBar;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfigurationFactory;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 19 juin 2008 - 15:22:43
 */
public class FileManager extends TriPanelBrowserViewport {

    public FileManager(String types, String filters, String mimeTypes, String conf) {
        // superclass constructor (define linker)
        super();

        ManagerConfiguration config ;
        if (conf != null && conf.length() > 0) {
            config = ManagerConfigurationFactory.getConfiguration(conf, linker) ;
        } else {
            config = ManagerConfigurationFactory.getFileManagerConfiguration(linker) ;
        }

        if (types != null && types.length() > 0) {
            config.setNodeTypes(types);
        }
        if (mimeTypes != null && mimeTypes.length() > 0) {
            config.setMimeTypes(mimeTypes);
        }
        if (filters != null && filters.length() > 0) {
            config.setFilters(filters);
        }

        // construction of the UI components
        LeftComponent tree = new FolderTree(config);
        final FilesView filesViews = new FilesView(config);
        BottomRightComponent tabs = new FileDetails(config);
        TopBar toolbar = new FileToolbar(config) {
            protected void setListView() {
                filesViews.switchToListView();
            }

            protected void setThumbView() {
                filesViews.switchToThumbView();
            }

            protected void setDetailedThumbView() {
                filesViews.switchToDetailedThumbView();
            }
        };
        BottomBar statusBar = new FileStatusBar();

        // setup widgets in layout
        initWidgets(tree.getComponent(),
                filesViews.getComponent(),
                tabs.getComponent(),
                toolbar.getComponent(),
                statusBar.getComponent());

        // linker initializations
        linker.registerComponents(tree, filesViews, tabs, toolbar, statusBar);
        filesViews.initContextMenu();
        linker.handleNewSelection();
    }
}
