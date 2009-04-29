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
package org.jahia.content;

import java.util.Set;
import java.util.HashSet;

/**
 * <p>Title: A ObjectKey class for categories</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class CategoryKey extends ObjectKey {

    private static final long serialVersionUID = 5874862723244322929L;
    
    public static final String CATEGORY_TYPE = "Category";

    static {
        ObjectKey.registerType(CATEGORY_TYPE, CategoryKey.class);
    }

    /**
     * Protected constructor to use this class also as a factory by calling
     * the getChildInstance method
     */
    protected CategoryKey() {}

    public CategoryKey(int categoryID) {
        super(CATEGORY_TYPE, Integer.toString(categoryID));
    }

    public CategoryKey(int categoryID, String objectKey) {
        super(CATEGORY_TYPE, Integer.toString(categoryID), objectKey);
    }

    public int getCategoryID() {
        return getIdInType();
    }

    /**
     * @deprecated This method should not be called directly, but rather it
     * should be replace by a call to the constructor with the proper IDInType.
     * This has been deprecated because the new getChildInstance() is much
     * faster
     * @param IDInType the IDInType
     * @return the ObjectKey corresponding to the ID for this class type
     */
    public static ObjectKey getChildInstance(String IDInType) {
        return new CategoryKey(Integer.parseInt(IDInType));
    }

    public ObjectKey getChildInstance(String IDInType, String objectKey) {
        return new CategoryKey(Integer.parseInt(IDInType), objectKey);
    }

    /**
     * Return a set of categories for the given array of String keys
     * @param keys
     * @return
     */
    public static Set<CategoryKey> getCategories(String[] keys) {
        Set<CategoryKey> categories = new HashSet<CategoryKey>();
        for ( int i=0; i<keys.length; i++ ){
            try {
                CategoryKey catKey = (CategoryKey)CategoryKey.getInstance(keys[i]);
                categories.add(catKey);
            } catch ( Exception t ){
            }
        }
        return categories;
    }
}