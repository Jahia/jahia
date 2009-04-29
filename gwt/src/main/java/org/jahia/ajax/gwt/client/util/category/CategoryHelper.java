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
package org.jahia.ajax.gwt.client.util.category;

import com.google.gwt.core.client.JsArray;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ktlili
 * Date: 6 nov. 2008
 * Time: 10:48:10
 */
public class CategoryHelper {
    public static List<GWTJahiaCategoryNode> getSelectedCategoriesFromHTML() {
        List<GWTJahiaCategoryNode> selectedCategories = new ArrayList<GWTJahiaCategoryNode>();
        JsArray<CategoryJavaScriptObject> categoryOverlayTypeJsArray = getCategoryOverlayTypes();

        if (categoryOverlayTypeJsArray != null) {
            for (int i = 0; i < categoryOverlayTypeJsArray.length(); i++) {
                CategoryJavaScriptObject categoryJavaScriptObject = categoryOverlayTypeJsArray.get(i);
                GWTJahiaCategoryNode categoryNode = new GWTJahiaCategoryNode();
                categoryNode.setCategoryId(categoryJavaScriptObject.getId());
                categoryNode.setName(categoryJavaScriptObject.getName());
                categoryNode.setKey(categoryJavaScriptObject.getKey());
                categoryNode.setPath(categoryJavaScriptObject.getPath());
                selectedCategories.add(categoryNode);
            }
        }
        return selectedCategories;
    }
    public static native String getCategoryLocale() /*-{
        return $wnd.sLocale;
    }-*/;
    public static native String getAutoSelectParent() /*-{
        return $wnd.sAutoSelectParent;
    }-*/;
    private static native JsArray<CategoryJavaScriptObject> getCategoryOverlayTypes() /*-{
    // Get a reference to the first customer in the JSON array from earlier
    return $wnd.sCategories;
  }-*/;

}
