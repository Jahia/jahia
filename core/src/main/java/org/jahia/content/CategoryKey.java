/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

    /**
     * Protected constructor to use this class also as a factory by calling
     * the getChildInstance method
     */
    protected CategoryKey() {}

    public CategoryKey(String categoryID) {
        super(CATEGORY_TYPE, categoryID);
    }

    public CategoryKey(String categoryID, String objectKey) {
        super(CATEGORY_TYPE, categoryID, objectKey);
    }

    public int getCategoryID() {
        return getIdInType();
    }

    public ObjectKey getChildInstance(String IDInType, String objectKey) {
        return new CategoryKey(IDInType, objectKey);
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