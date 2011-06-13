package org.jahia.tools.contentgenerator.junit;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.PageBO;
import org.junit.Test;

public class ContentGeneratorTestCase extends TestCase {
	protected List<ArticleBO> articles;
	protected List<PageBO> pages;
	
	protected ArticleBO articleEn;
	protected ArticleBO articleFr;

	protected static String TITLE_FR = "Titre FR";
	protected static String CONTENT_FR = "Titre FR";
	
	protected static String TITLE_EN = "Title EN";
	protected static String CONTENT_EN = "CONTENT EN";
	
	public void setUp() throws Exception {
		super.setUp();
		createPages();
		createArticles();
		createExport();
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	private void createPages() {
		pages = new ArrayList();

		List subPages = new ArrayList();

		boolean hasVanity = true;
		String siteKey = "mySite";
		int pageID = 111;
		PageBO page111 = new PageBO("page" + pageID, "Title " + pageID, "Content " + pageID, "Titre " + pageID, "Contenu " + pageID, 0, null, hasVanity, siteKey, null);
		pageID=112;
		PageBO page112 = new PageBO("page" + pageID, "Title " + pageID, "Content " + pageID, "Titre " + pageID, "Contenu " + pageID, 0, null, hasVanity, siteKey, null);
		
		subPages.add(page111);
		subPages.add(page112);
		
		pageID=11;
		PageBO page11 = new PageBO("page" + pageID, "Title " + pageID, "Content " + pageID, "Titre " + pageID, "Contenu " + pageID, 0, null, hasVanity, siteKey, null);
		pageID=12;
		PageBO page12 = new PageBO("page" + pageID, "Title " + pageID, "Content " + pageID, "Titre " + pageID, "Contenu " + pageID, 0, null, hasVanity, siteKey, null);
		subPages.add(page11);
		subPages.add(page12);
		
		pageID=1;
		PageBO page1 = new PageBO("page" + pageID, "Title " + pageID, "Content " + pageID, "Titre " + pageID, "Contenu " + pageID, 0, null, hasVanity, siteKey, null);

		pageID=2;
		PageBO page2 = new PageBO("page" + pageID, "Title " + pageID, "Content " + pageID, "Titre " + pageID, "Contenu " + pageID, 0, null, hasVanity, siteKey, null);
		
		pageID=3;
		PageBO page3 = new PageBO("page" + pageID, "Title " + pageID, "Content " + pageID, "Titre " + pageID, "Contenu " + pageID, 0, null, hasVanity, siteKey, null);
		
		pages.add(page1);
		pages.add(page2);
		pages.add(page3);
	}
	
	private void createArticles() {
		/*
		 * Articles
		 */
		articleFr = new ArticleBO(1, TITLE_FR, CONTENT_FR);
		articleEn  = new ArticleBO(2, TITLE_EN, CONTENT_EN);
	}
	
	private void createExport() {
		
	}
	
	@Test
	public void testDummy() {
		assertTrue(Boolean.TRUE);
	}
}
