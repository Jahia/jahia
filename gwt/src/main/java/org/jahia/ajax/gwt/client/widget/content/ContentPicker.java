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
import org.jahia.ajax.gwt.client.widget.tripanel.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.List;

import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.Style;

/**
 * File and folder picker control.
 *
 * @author rfelden
 *         Date: 27 ao�t 2008
 *         Time: 17:55:07
 */
public class ContentPicker extends TriPanelBrowserLayout {

    public ContentPicker(final String rootPath, final List<GWTJahiaNode> selectedNodes, String types, String filters, String mimeTypes, String conf, boolean multiple, boolean allowThumbs, String callback) {
        super();
        //setWidth("714px");
        setHeight("700px");

        ManagerConfiguration config;
        if (conf == null || conf.length() == 0) {
            config = ManagerConfigurationFactory.getFilePickerConfiguration(linker);
        } else {
            config = ManagerConfigurationFactory.getConfiguration(conf, linker);
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
        BottomRightComponent bottomComponents;
        if (conf.equalsIgnoreCase(ManagerConfigurationFactory.PAGEPICKER)) {
            bottomComponents = new PickedPageView(conf, selectedNodes, multiple, config);
        } else {
            bottomComponents = new PickedContentView(conf, selectedNodes, multiple, config);
        }
        TopRightComponent contentPicker = new ContentPickerBrowser(conf, rootPath, selectedNodes, config, callback, multiple, allowThumbs);

        MyStatusBar statusBar = new FilterStatusBar(config.getFilters(), config.getMimeTypes(), config.getNodeTypes());

        // setup widgets in layout

        if (conf.equalsIgnoreCase(ManagerConfigurationFactory.PAGEPICKER)) {
            setCenterData(new BorderLayoutData(Style.LayoutRegion.SOUTH, 375));
            initWidgets(null,bottomComponents.getComponent(),contentPicker.getComponent(),null,statusBar);
        } else {
            initWidgets(null,contentPicker.getComponent(),bottomComponents.getComponent(),null,statusBar);
        }

        // linker initializations
        linker.registerComponents(null, contentPicker, bottomComponents, null, null);
        contentPicker.initContextMenu();
        linker.handleNewSelection();
    }

    public List<GWTJahiaNode> getSelectedNodes() {
        return ((ContentPickerBrowser) linker.getTopRightObject()).getSelectedNodes();
    }


}
