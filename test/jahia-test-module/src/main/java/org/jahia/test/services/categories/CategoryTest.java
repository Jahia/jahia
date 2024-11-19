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
package org.jahia.test.services.categories;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.categories.Category;
import org.jahia.services.content.*;
import org.junit.After;
import org.junit.Test;

import javax.jcr.Node;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Unit test for category service.
 *
 * @author loom
 *         Date: Aug 11, 2009
 *         Time: 3:24:43 PM
 */
public class CategoryTest {

    @Test
    public void testCategoriesRoot() throws Exception {
        Node rootCategory = Category.getCategoriesRoot(); // root category is created at service start so it should already exist at this point.
        assertNotNull(rootCategory);
    }

    @Test
    public void testCreateRootCategory() throws Exception {
        Category newRootCategory = Category.createCategory("firstRoot", null);
        assertNotNull(newRootCategory);
        deleteCategoryWithChildren(newRootCategory);
    }

    @Test
    public void testCreateCategory() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        assertNotNull(rootCategory);
        Category newCategory = Category.createCategory("firstChild", rootCategory);
        List<Category> rootChilds = rootCategory.getChildCategories();
        assertTrue(rootChilds.size() == 1);
        deleteCategoryWithChildren(newCategory);
    }

    @Test
    public void testCategoryKeyUnicity() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        Category.createCategory("firstChild", rootCategory);
        List<Category> rootChilds = rootCategory.getChildCategories();
        assertTrue(rootChilds.size() == 1);
        boolean duplicateDetected = false;
        try {
            Category.createCategory("firstChild", rootCategory);
        } catch (JahiaException je) {
            // this is expected, and normal.
            duplicateDetected = true;
        }
        assertTrue(duplicateDetected);

        deleteCategoryWithChildren(rootCategory);

    }

    @Test
    public void testUpdateCategoryTitle() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        Category newCategory = Category.createCategory("firstChild", rootCategory);
        List<Category> rootChilds = rootCategory.getChildCategories();
        assertTrue(rootChilds.size() == 1);

        newCategory.setTitle(Locale.ENGLISH, "english_title");
        newCategory.setTitle(Locale.FRENCH, "french_title");

        assertEquals(newCategory.getTitle(Locale.ENGLISH), "english_title");
        assertEquals(newCategory.getTitle(Locale.FRENCH), "french_title");

        newCategory.removeTitle(Locale.ENGLISH);
        newCategory.removeTitle(Locale.FRENCH);

        assertNull(newCategory.getTitle(Locale.ENGLISH));
        assertNull(newCategory.getTitle(Locale.FRENCH));

        // now let's test with null titles

        newCategory.setTitle(Locale.ENGLISH, null);
        assertNull(newCategory.getTitle(Locale.ENGLISH));
        newCategory.removeTitle(Locale.ENGLISH);

        deleteCategoryWithChildren(rootCategory);
    }

    @Test
    public void testCategoryProperties() throws Exception {
        // commented for now as category properties are no longer supported
        // but there will be category relationships, which could be tested
        // in a similar way

        /*Category rootCategory = Category.createCategory("firstRoot", null);
        Category newCategory = Category.createCategory("firstChild", rootCategory);
        List<Category> rootChilds = rootCategory.getChildCategories();
        assertTrue(rootChilds.size() == 1);

        newCategory.setProperty("NormalProperty", "normal");

        assertEquals(newCategory.getProperty("NormalProperty"), "normal");

        newCategory.removeProperty("NormalProperty");

        assertNull(newCategory.getProperty("NormalProperty"));

        boolean threwNPE = false;
        try {
            newCategory.setProperty("NullProperty", null);
        } catch (NullPointerException npe) {
            threwNPE = true;
        }
        assertTrue(threwNPE);
        assertNull(newCategory.getProperty("NullProperty"));
        newCategory.removeProperty("NullProperty");

        deleteCategoryWithChildren(newCategory);*/

    }

    @Test
    public void testCategoryDelete() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        Category.createCategory("firstChild", rootCategory);
        List<Category> rootChilds = rootCategory.getChildCategories();
        assertTrue(rootChilds.size() == 1);
//        int aclID = newCategory.getAclID();
        deleteCategoryWithChildren(rootCategory);
/*        boolean aclWasDeleted = false;
        try {
            JahiaBaseACL categoryACL = new JahiaBaseACL(aclID);
        } catch (ACLNotFoundException anfe) {
            aclWasDeleted = true;
        }
        assertTrue(aclWasDeleted);*/
    }

    @Test
    public void testBuildCategoryTree() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        Category newCategory = Category.createCategory("rootChild", rootCategory);
        buildCategoryTree(newCategory, 4, 3);
        int sizeOfTree = 0;
        for (int i = 1; i <= 4; i++) {
            sizeOfTree += Math.pow(3, i);
        }
        final List<Category> childCategories = newCategory.getChildCategories(true);
        assertTrue(childCategories.size() == sizeOfTree);
        deleteCategoryWithChildren(rootCategory);
    }

    @Test
    public void testCategoryPath() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        Category newCategory = Category.createCategory("rootChild", rootCategory);
        String path = newCategory.getCategoryPath();
        Category categoryByPath = Category.getLastCategoryNode(path);
        assertNotNull(categoryByPath);
        deleteCategoryWithChildren(rootCategory);
    }

    @Test
    public void testCategoryChilds() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        Category.createCategory("rootChild", rootCategory);
        List<Category> childObjectKeys = rootCategory.getChildCategories();
        assertEquals(childObjectKeys.size(), 1);
        assertEquals(childObjectKeys.get(0).getKey(), "rootChild");
        deleteCategoryWithChildren(rootCategory);
    }

    @Test
    public void testCategoryRename() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        Category.createCategory("rootChild", rootCategory);

        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        sessionFactory.closeAllSessions();
        JCRSessionWrapper englishEditSession = sessionFactory.getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, Locale.ENGLISH);
        JCRSessionWrapper englishLiveSession = sessionFactory.getCurrentSystemSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, Locale.ENGLISH);

        JCRNodeWrapper categoryToRename = englishEditSession.getNode("/sites/systemsite/categories/firstRoot/rootChild");
        categoryToRename.rename("rootChildRenamed");
        englishEditSession.save();

        assertTrue(englishLiveSession.nodeExists("/sites/systemsite/categories/firstRoot/rootChildRenamed"));
        assertFalse(englishLiveSession.nodeExists("/sites/systemsite/categories/firstRoot/rootChild"));

        deleteCategoryWithChildren(rootCategory);
    }

    private void buildCategoryTree(Category parentCategory, int depth, int nbChildren) throws Exception {
        if (depth == 0) {
            return;
        }
        for (int i = 0; i < nbChildren; i++) {
            Category newCategory = Category.createCategory(parentCategory.getKey() + "_child_" + Integer.toString(i),
                    parentCategory);
            buildCategoryTree(newCategory, depth - 1, nbChildren);
        }
    }

    private void deleteCategoryWithChildren(Category currentCategory) throws Exception {
        List<Category> childCategories = currentCategory.getChildCategories();
        Iterator<Category> childCategoriesIterator = childCategories.iterator();
        while (childCategoriesIterator.hasNext()) {
            deleteCategoryWithChildren(childCategoriesIterator.next());
        }
        currentCategory.delete();
    }

    @After
    public void tearDown() throws Exception {
        for (Category rootCategory : Category.getRootCategories(null)) {
            deleteCategoryWithChildren(rootCategory);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

}
