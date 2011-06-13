package org.jahia.tools.contentgenerator.junit;

import java.util.List;

import org.jahia.tools.contentgenerator.PageService;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.jahia.tools.contentgenerator.bo.PageBO;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class PageServiceTest extends ContentGeneratorTestCase {
	private static PageService pageService;
	
	@BeforeClass
	public void setUp() throws Exception {
		super.setUp();
		pageService = new PageService();
	}

	@AfterClass
	public void tearDown() {
		// run for one time after all test cases
	}
	
	@Ignore
	@Test
	public void testCreateTopPages() {
		//pageService.createTopPages(export, articles);
	}
	
	@Ignore
	@Test
	public void testCreateSubPages() {
		
	}
	
	@Ignore
	@Test
	public void testCreateNewPage() {
		String pageName = "myPage";
		
		PageBO newPage = pageService.createNewPage(super.export_default, pageName, articleEn, articleFr, 1, null);
		assertEquals(pageName, newPage.getUniqueName());
	}
	
	@Ignore
	@Test
	public void testGetPagesPath() {
		List pagesPath = pageService.getPagesPath(super.pages, null);
		assertEquals(super.total_pages.intValue(), pagesPath.size());
		assertTrue(pagesPath.contains("/page1"));
		assertTrue(pagesPath.contains("/page2"));
		assertTrue(pagesPath.contains("/page3"));
		assertTrue(pagesPath.contains("/page1/page11"));
		assertTrue(pagesPath.contains("/page1/page12"));
		assertTrue(pagesPath.contains("/page1/page11/page111"));
		assertTrue(pagesPath.contains("/page1/page11/page112"));
	}
	
	@Ignore
	@Test
	public void testFormatForXml() {
		
	}
}
