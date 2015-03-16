/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.module.contentmanager.client;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.content.ContentManagerViewPort;
import org.jahia.ajax.gwt.client.widget.content.ContentManager;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerViewport;
import org.jahia.ajax.gwt.client.widget.content.util.ContentHelper;

import java.util.*;

/**
 * @author rfelden
 */
public class ContentManagerEntryPoint extends CommonEntryPoint {

    public void onModuleLoad() {
        super.onModuleLoad();
        checkSession();
        if (RootPanel.get("contentmanager") != null) {
            final RootPanel panel = RootPanel.get("contentmanager");
            final boolean embedded = Boolean.valueOf(DOM.getElementAttribute(panel.getElement(), "embedded"));
            final String filtersString = DOM.getElementAttribute(panel.getElement(), "filters");
            final List<String> filters = filtersString.length() > 0 ? Arrays.asList(filtersString.split(",")) : null;
            final String mimeTypesString = DOM.getElementAttribute(panel.getElement(), "mimeTypes");
            final List<String> mimeTypes = mimeTypesString.length() > 0 ? Arrays.asList(mimeTypesString.split(",")) : null;
            final String config = DOM.getElementAttribute(panel.getElement(), "config");
            final String rootPath = DOM.getElementAttribute(panel.getElement(), "rootPath");
            final String paths = DOM.getElementAttribute(panel.getElement(), "selectedPaths");
            final List<String> selectedPaths = new ArrayList<String>();
            if (paths != null && paths.length() > 0) {
                for (String path : paths.split(",")) {
                    selectedPaths.add(path.trim());
                }
            }

            JahiaContentManagementService.App.getInstance().getManagerConfiguration(config, rootPath, new BaseAsyncCallback<GWTManagerConfiguration>() {
                public void onSuccess(GWTManagerConfiguration config) {
                    PermissionsUtils.loadPermissions(config.getPermissions());
                    if (embedded) {
                        panel.add(new ContentManager(filters, mimeTypes, selectedPaths, config, 250));
                    } else {
                        panel.add(new ContentManagerViewPort(filters, mimeTypes, selectedPaths, config));
                    }
                }

                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Error while loading user permission", throwable);
                }
            });


        } else if (RootPanel.get("contentpicker") != null) {
            final  RootPanel panel = RootPanel.get("contentpicker");
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


            JahiaContentManagementService.App.getInstance().getManagerConfiguration(conf, null, new BaseAsyncCallback<GWTManagerConfiguration>() {
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
