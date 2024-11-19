/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

    /**
     * @deprecated {@link #getIDInType()} should be used instead.
     */
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
