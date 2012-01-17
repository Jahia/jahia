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

package org.jahia.services.categories;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRSessionFactory;
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
