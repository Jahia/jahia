package org.jahia.services.categories;

import junit.framework.TestCase;
import org.jahia.exceptions.JahiaException;

import java.util.List;
import java.util.Iterator;
import java.util.Locale;

import javax.jcr.Node;

/**
 * Unit test for category service.
 *
 * @author loom
 *         Date: Aug 11, 2009
 *         Time: 3:24:43 PM
 */
public class CategoryTest extends TestCase {

    public void testCategoriesRoot() throws Exception {
        Node rootCategory = Category.getCategoriesRoot(); // root category is created at service start so it should already exist at this point.
        assertNotNull(rootCategory);
    }

    public void testCreateRootCategory() throws Exception {
        Category newRootCategory = Category.createCategory("firstRoot", null);
        assertNotNull(newRootCategory);        
        deleteCategoryWithChildren(newRootCategory);        
    }
    
    public void testCreateCategory() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        assertNotNull(rootCategory);        
        Category newCategory = Category.createCategory("firstChild", rootCategory);        
        List<Category> rootChilds = rootCategory.getChildCategories();
        assertTrue(rootChilds.size() == 1);
        deleteCategoryWithChildren(newCategory);
    }    

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

    public void testBuildCategoryTree() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        Category newCategory = Category.createCategory("rootChild", rootCategory);
        buildCategoryTree(newCategory, 4, 3);
        deleteCategoryWithChildren(rootCategory);
    }

    public void testCategoryPath() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        Category newCategory = Category.createCategory("rootChild", rootCategory);
        String path = newCategory.getCategoryPath();
        Category categoryByPath = Category.getLastCategoryNode(path);
        assertNotNull(categoryByPath);
        deleteCategoryWithChildren(rootCategory);
    }

    public void testCategoryChilds() throws Exception {
        Category rootCategory = Category.createCategory("firstRoot", null);
        Category newCategory = Category.createCategory("rootChild", rootCategory);
        List<Category> childObjectKeys = rootCategory.getChildCategories();
        assertEquals(childObjectKeys.size(), 1);
        assertEquals(childObjectKeys.get(0).getKey(), "rootChild");
        deleteCategoryWithChildren(rootCategory);
    }

    private void buildCategoryTree(Category parentCategory, int depth, int nbChildren) throws Exception {
        if (depth == 0) {
            return;
        }
        for (int i=0; i < nbChildren; i++) {
            Category newCategory = Category.createCategory(parentCategory.getKey() + "_child_" + Integer.toString(i), parentCategory);
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

    protected void tearDown() throws Exception {
        for (Category rootCategory : Category.getRootCategories(null)) {
            deleteCategoryWithChildren(rootCategory);
        }
    }

}
