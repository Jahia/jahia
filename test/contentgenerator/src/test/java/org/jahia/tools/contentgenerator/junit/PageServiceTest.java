package org.jahia.tools.contentgenerator.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jahia.tools.contentgenerator.PageService;
import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.PageBO;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PageServiceTest extends ContentGeneratorTestCase {
	private static PageService pageService;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		pageService = new PageService();
	}

	@After
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

	@Test
	public void testCreateNewPage() {
		String pageName = "myPage";
		Integer totalBigText = Integer.valueOf(5);
		super.export_default.setNumberOfBigTextPerPage(totalBigText);
		
        HashMap<String, ArticleBO> map = new HashMap<String, ArticleBO>();
        map.put("en",articleEn);
		PageBO newPage = pageService.createNewPage(super.export_default, pageName, map, 1, null);
		assertEquals(pageName, newPage.getUniqueName());
		String sPage = newPage.toString();
		assertTrue(StringUtils.contains(sPage, "<bigText_1"));
		assertTrue(StringUtils.contains(sPage, "<bigText_2"));
		assertTrue(StringUtils.contains(sPage, "<bigText_3"));
		assertTrue(StringUtils.contains(sPage, "<bigText_4"));
		assertTrue(StringUtils.contains(sPage, "<bigText_5"));
	}
	
	@Test
	public void testCreateNewPageZeroBigText() {
		String pageName = "myPage";
		Integer totalBigText = Integer.valueOf(0);
		super.export_default.setNumberOfBigTextPerPage(totalBigText);

        HashMap<String, ArticleBO> map = new HashMap<String, ArticleBO>();
        map.put("en",articleEn);
        PageBO newPage = pageService.createNewPage(super.export_default, pageName, map, 1, null);
		assertEquals(pageName, newPage.getUniqueName());
		String sPage = newPage.toString();
		assertFalse(StringUtils.contains(sPage, "<bigText"));
	}
	
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
