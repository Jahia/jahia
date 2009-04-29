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
package org.jahia.ajax.gwt.module.categorymanager.client;

import org.jahia.ajax.gwt.client.util.JahiaGWT;
import org.jahia.ajax.gwt.client.widget.category.CategoriesManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * User: ktlili
 * Date: 15 sept. 2008
 * Time: 15:23:49
 */
public class CategoryManagerEntryPoint implements EntryPoint {
    private static String ID = "categories_manager";
    private static String IMPORT_ACTION = "importAction";
    private static String EXPORT_URL = "exportUrl";
    private static String UPDATE_ACTION = "updateAction";
    private static String ROOT_KEY = "rootKey";

    public void onModuleLoad() {
        JahiaGWT.init();
        // init panel
        RootPanel jahiaTypePanel = RootPanel.get(ID);
        if (jahiaTypePanel != null) {
            String importActionUrl = jahiaTypePanel.getElement().getAttribute(IMPORT_ACTION);
            String exportActionUrl = jahiaTypePanel.getElement().getAttribute(EXPORT_URL);
            String updateActionUrl = jahiaTypePanel.getElement().getAttribute(UPDATE_ACTION);
            String rootKey = jahiaTypePanel.getElement().getAttribute(ROOT_KEY);
            jahiaTypePanel.add(new CategoriesManager(rootKey, importActionUrl, exportActionUrl, updateActionUrl));
        }
    }

}
