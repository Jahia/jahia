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

import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.widget.tripanel.TriPanelBrowserViewport;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

/**
 * File and folder picker control.
 * @author rfelden
 * Date: 27 ao�t 2008
 * Time: 17:55:07
 */
public class ContentPickerViewport extends TriPanelBrowserViewport {

    public ContentPickerViewport(final String rootPath, final String startPath, String types, String filters, String mimeTypes, String conf, boolean allowThumbs, String callback) {
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
        TopBar toolbar = new ContentToolbar(config) ;
        TopRightComponent filepicker = new ContentPickerContainer(rootPath, startPath, config, callback, allowThumbs) ;

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