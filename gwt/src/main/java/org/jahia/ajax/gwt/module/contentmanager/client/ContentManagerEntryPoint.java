/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
