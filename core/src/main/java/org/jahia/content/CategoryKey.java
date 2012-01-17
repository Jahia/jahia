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