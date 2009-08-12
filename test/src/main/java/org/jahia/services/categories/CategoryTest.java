package org.jahia.services.categories;

import junit.framework.TestCase;
import org.jahia.test.TestHelper;
import org.jahia.exceptions.JahiaException;

import java.util.List;
import java.util.Iterator;
import java.util.Locale;

/**
 * Unit test for category service.
 *
 * @author loom
 *         Date: Aug 11, 2009
 *         Time: 3:24:43 PM
 */
public class CategoryTest extends TestCase {

    public void testRootCategory() throws Exception {
        Category rootCategory = Category.getRootCategory(); // root category is created at service start so it should already exist at this point.
        assertNotNull(rootCategory);
    }

    public void testCreateCategory() throws Exception {
        Category rootCategory = Category.getRootCategory(); // root category is created at service start so it should already exist at this point.
        Category newCategory = Category.createCategory("firstChild", rootCategory);
        List<Category> rootChilds = rootCategory.getChildCategories();
        assertTrue(rootChilds.size() == 1);
        deleteCategoryWithChildren(newCategory);
    }

    public void testCategoryKeyUnicity() throws Exception {
        Category rootCategory = Category.getRootCategory(); // root category is created at service start so it should already exist at this point.
        Category newCategory = Category.createCategory("firstChild", rootCategory);
        List<Category> rootChilds = rootCategory.getChildCategories();
        assertTrue(rootChilds.size() == 1);
        boolean duplicateDetected = false;
        try {
            Category duplicateKeyCategory = Category.createCategory("firstChild", newCategory);
        } catch (JahiaException je) {
            // this is expected, and normal.
            duplicateDetected = true;
        }
        assertTrue(duplicateDetected);
        
        deleteCategoryWithChildren(newCategory);

    }

    public void testUpdateCategoryTitle() throws Exception {
        Category rootCategory = Category.getRootCategory(); // root category is created at service start so it should already exist at this point.
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

        deleteCategoryWithChildren(newCategory);
    }

    public void testCategoryProperties() throws Exception {
        Category rootCategory = Category.getRootCategory(); // root category is created at service start so it should already exist at this point.
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

        deleteCategoryWithChildren(newCategory);
        
    }

    public void testBuildCategoryTree() throws Exception {
        Category rootCategory = Category.getRootCategory(); // root category is created at service start so it should already exist at this point.
        Category newCategory = Category.createCategory("rootChild", rootCategory);
        buildCategoryTree(newCategory, 4, 3);
        deleteCategoryWithChildren(newCategory);
    }

    public void testCategoryPath() throws Exception {
        // todo not yet implemented
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
        if (!currentCategory.getObjectKey().equals(Category.getRootCategory().getObjectKey())) {
            currentCategory.delete();
        }
    }

    protected void tearDown() throws Exception {
        deleteCategoryWithChildren(Category.getRootCategory()); // root category is created at service start so it should already exist at this point.
    }

}
