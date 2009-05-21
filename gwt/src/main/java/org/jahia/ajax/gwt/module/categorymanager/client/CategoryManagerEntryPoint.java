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
