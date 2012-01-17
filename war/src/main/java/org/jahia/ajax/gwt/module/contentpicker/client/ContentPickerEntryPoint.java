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

package org.jahia.ajax.gwt.module.contentpicker.client;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerViewport;
import org.jahia.ajax.gwt.client.widget.content.util.ContentHelper;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;

/**
 * File picker to embed in container edition engine or anywhere else you want,
 * provided there is correct initialization.
 *
 * @author rfelden
 *         Date: 27 ao�t 2008
 *         Time: 17:59:59
 */
public class ContentPickerEntryPoint extends CommonEntryPoint {


    /**
     * On module load
     */
    public void onModuleLoad() {
        /* todo The following two lines are a hack to get development mode to work on Mac OS X, should be removed once this
           problem is fixed.
         */
        @SuppressWarnings("unused")
        Layout junk = new AnchorLayout();
        final RootPanel panel = RootPanel.get("contentpicker");
        if (panel != null) {
            final String jahiaContextPath = DOM.getElementAttribute(panel.getElement(), "jahiaContextPath");
            final String jahiaServletPath = DOM.getElementAttribute(panel.getElement(), "jahiaServletPath");
            final String filesServletPath = DOM.getElementAttribute(panel.getElement(), "filesServletPath");
            final String selectionLabel = DOM.getElementAttribute(panel.getElement(), "selectionLabel");
            final Map<String, String> selectorOptions = new HashMap<String, String>();
            final List<GWTJahiaNode> selectedNodes = ContentHelper.getSelectedContentNodesFromHTML();
            final String filtersString = DOM.getElementAttribute(panel.getElement(), "filters");
            final List<String> filters = filtersString.length() > 0 ? Arrays.asList(filtersString.split(",")) : null;
            final String mimeTypesString = DOM.getElementAttribute(panel.getElement(), "mimeTypes");
            final List<String> mimeTypes = mimeTypesString.length() > 0 ? Arrays.asList(mimeTypesString.split(",")) : null;
            final String conf = DOM.getElementAttribute(panel.getElement(), "config");
            final boolean multiple = Boolean.parseBoolean(DOM.getElementAttribute(panel.getElement(), "multiple"));
            final String callback = DOM.getElementAttribute(panel.getElement(), "callback");


            JahiaContentManagementService.App.getInstance().getManagerConfiguration(conf, new BaseAsyncCallback<GWTManagerConfiguration>() {
                public void onSuccess(GWTManagerConfiguration config) {
                    PermissionsUtils.loadPermissions(config.getPermissions());
                    panel.add(new ContentPickerViewport(jahiaContextPath, jahiaServletPath, filesServletPath,
                            selectionLabel,
                            selectorOptions, selectedNodes, filters, mimeTypes, config, multiple, callback));
                }

                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Error while loading user permission", throwable);
                }
            });
        }
    }

}
