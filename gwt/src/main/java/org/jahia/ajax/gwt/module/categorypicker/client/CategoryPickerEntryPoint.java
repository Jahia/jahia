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
package org.jahia.ajax.gwt.module.categorypicker.client;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.JahiaGWT;
import org.jahia.ajax.gwt.client.util.category.CategoryHelper;
import org.jahia.ajax.gwt.client.widget.category.CategoriesPickerPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.core.client.EntryPoint;
import com.allen_sauer.gwt.log.client.Log;

/**
 * User: ktlili
 * Date: 3 oct. 2008
 * Time: 12:47:42
 */
public class CategoryPickerEntryPoint implements EntryPoint {
    public static String ID = "categories_picker";
    public static String ROOT_KEY = "rootKey";
    public static String READ_ONLY = "readonly";
    public static String MULTIPLE = "multiple";

    private static RootPanel jahiaTypePanel;


    public void onModuleLoad() {
        JahiaGWT.init();
        Log.debug("load module");
        // init panel
        jahiaTypePanel = RootPanel.get(ID);
        if (jahiaTypePanel != null) {
            jahiaTypePanel.add(new CategoriesPickerPanel(CategoryHelper.getSelectedCategoriesFromHTML(),getReadOnly(),getRootKey(),CategoryHelper.getCategoryLocale(),CategoryHelper.getAutoSelectParent(), isMultipleProperty()));
        } else {
            Log.debug("Categories selector not found");
        }
    }

    /**
     * Get root key
     *
     * @return
     */
    public static String getRootKey() {
        if (jahiaTypePanel.getElement() != null) {
            return jahiaTypePanel.getElement().getAttribute(ROOT_KEY);
        }
        return null;
    }

    /**
     * Get multiple property
     *
     * @return
     */
    public static boolean isMultipleProperty() {
        if (jahiaTypePanel.getElement() != null) {
            String prop = jahiaTypePanel.getElement().getAttribute(MULTIPLE) ;
            if (prop != null && prop.equalsIgnoreCase("false")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get readonly
     *
     * @return
     */
    public static boolean getReadOnly() {
        if (jahiaTypePanel.getElement() != null) {
            String value = jahiaTypePanel.getElement().getAttribute(READ_ONLY);
            try {
                return Boolean.parseBoolean(value);
            } catch (Exception e) {
                Log.error("readonly value[" + value + "] is not a boolean.");
            }
        }
        return false;
    }

    public static String getResource(String key) {
        return Messages.getResource(key);
    }
}
