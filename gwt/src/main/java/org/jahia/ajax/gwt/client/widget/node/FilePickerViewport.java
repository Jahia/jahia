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

import org.jahia.ajax.gwt.client.widget.node.FileToolbar;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.widget.node.FilePickerContainer;
import org.jahia.ajax.gwt.client.widget.tripanel.TriPanelBrowserViewport;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

/**
 * File and folder picker control.
 * @author rfelden
 * Date: 27 ao�t 2008
 * Time: 17:55:07
 */
public class FilePickerViewport extends TriPanelBrowserViewport {

    public FilePickerViewport(final String rootPath, final String startPath, String types, String filters, String mimeTypes, String conf, boolean allowThumbs, String callback) {
        super() ;
        //setWidth("714px");

        ManagerConfiguration config  ;
        if (conf == null || conf.length() == 0) {
            config = ManagerConfigurationFactory.getFilePickerConfiguration(linker) ;
        } else {
            config = ManagerConfigurationFactory.getConfiguration(conf, linker) ;
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
        TopBar toolbar = new FileToolbar(config) ;
        TopRightComponent filepicker = new FilePickerContainer(rootPath, startPath, config, callback, allowThumbs) ;

        // setup widgets in layout
        initWidgets(null,
                    filepicker.getComponent(),
                    null,
                    toolbar.getComponent(),
                    null);

        // linker initializations
        linker.registerComponents(null, filepicker, null, toolbar, null) ;
        filepicker.initContextMenu();
        linker.handleNewSelection();
    }

}