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
	
	private static List<PageBO> pages;
	
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
		ExportBO export = new ExportBO();
		
		//	pageService.createNewPage(export, articleEn, articleFr, level, subPages)
	}
	
	@Ignore
	@Test
	public void testGetPagesPath() {
		
	}
	
	@Ignore
	@Test
	public void testFormatForXml() {
		
	}
}
