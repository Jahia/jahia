package org.jahia.tools.contentgenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.jahia.tools.contentgenerator.bo.PageBO;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;

public class PageService {
	private static final Logger logger = Logger.getLogger(PageService.class.getName());
	
	private static PageService instance;
	
	public PageService() {
		
	}
	
	public static PageService getInstance() {
		if (instance == null) {
			instance = new PageService();
		}
		return instance;
	}
	
	/**
	 * Main method, create top pages and for each calls the sub pages
	 * generation. Each time a top page and its sub pages have been generated,
	 * they are sent to the writer to avoid out of memory error with a big
	 * number of pages.
	 * 
	 * @param export
	 *            Export BO contains all the parameters chose by the user to
	 *            configure his/her content generation
	 * @param articles
	 *            List of articles selected from database
	 * @throws IOException
	 *             Error while writing generated XML to the output file
	 */
	public void createTopPages(ExportBO export, List<ArticleBO> articles) throws IOException {
		ArticleService articleService = ArticleService.getInstance();
		
		logger.info("Creating top pages");
		PageBO pageTopLevel = null;
		ArticleBO articleEn = null;
		ArticleBO articleFr = null;

		articleEn = articleService.getArticle(articles);
		articleFr = articleService.getArticle(articles);
		PageBO homePage = createNewPage(export, articleEn, articleFr, export.getNbSubLevels() + 1, null);

		OutputService outService = new OutputService();
		outService.initOutputFile(export.getOutputFile());
		outService.appendStringToFile(export.getOutputFile(), export.toString());
		outService.appendStringToFile(export.getOutputFile(), homePage.getHeader());

		for (int i = 1; i <= export.getNbPagesTopLevel().intValue(); i++) {
			articleEn = articleService.getArticle(articles);
			articleFr = articleService.getArticle(articles);
			pageTopLevel = createNewPage(
					export,
					articleEn,
					articleFr,
					export.getNbSubLevels() + 1,
					createSubPages(export, articles, ContentGeneratorService.currentPageIndex, export.getNbSubLevels(),
							export.getMaxArticleIndex()));
			outService.appendPageToFile(export.getOutputFile(), pageTopLevel);

			// path
			if (export.getCreateMap()) {
				logger.info("Pages path are being written to the map file");
				List<PageBO> listeTopPage = new ArrayList<PageBO>();
				listeTopPage.add(pageTopLevel);

				List<String> pagesPath = getPagesPath(listeTopPage, "/" + homePage.getUniqueName());
				outService.appendPathToFile(export.getMapFile(), pagesPath);
			}

			logger.debug("XML code of top level page #" + i + " written in output file");
			logger.info("Top page #" + i + " with subpages created and written to file");
		}
		outService.appendStringToFile(export.getOutputFile(), homePage.getFooter());
	}

	/**
	 * Recursive method that will generate all sub pages of a page, and call
	 * itself as much as necessary to reach the number of levels requested
	 * 
	 * @param export
	 *            Export BO contains all the parameters chose by the user to
	 *            configure his/her content generation
	 * @param articles
	 *            List of articles selected from database
	 * @param articleIndex
	 *            Current cursor in the articles table, to avoid random choice
	 *            as long as some articles have not been selected yet
	 * @param level
	 *            Current level in the tree decrease each time, top level ==
	 *            number of levels requested by the user)
	 * @param maxArticleIndex
	 *            Index of the last article
	 * @return A list of Page BO, containing their sub pages (if they have some)
	 */
	public List<PageBO> createSubPages(ExportBO export, List<ArticleBO> articles, Integer articleIndex, Integer level,
			Integer maxArticleIndex) {
		ArticleService articleService = ArticleService.getInstance();
		List<PageBO> listePages = new ArrayList<PageBO>();
		ArticleBO articleEn;
		ArticleBO articleFr;

		int nbPagesPerLevel = export.getNbSubPagesPerPage();

		listePages.clear();
		if (level.intValue() > 0) {
			for (int i = 0; i < nbPagesPerLevel; i++) {
				articleEn = articleService.getArticle(articles);
				articleFr = articleService.getArticle(articles);
				PageBO page = createNewPage(
						export,
						articleEn,
						articleFr,
						level,
						createSubPages(export, articles, articleIndex.intValue() + 1, level.intValue() - 1,
								maxArticleIndex));
				listePages.add(page);
			}
		}
		return listePages;
	}

	/**
	 * Create a new page object
	 * 
	 * @param export
	 *            Export BO contains all the parameters chose by the user to
	 *            configure his/her content generation
	 * @param articleEn
	 *            English article (language is on the Jahia side, article object
	 *            can contain different language, as it is from the database)
	 * @param articleFr
	 *            French article (language is on the Jahia side, article object
	 *            can contain different language, as it is from the database)
	 * @param level
	 *            Current level in the tree
	 * @param subPages
	 *            List of sub pages related
	 * @return
	 */
	public PageBO createNewPage(ExportBO export, ArticleBO articleEn, ArticleBO articleFr, int level,
			List<PageBO> subPages) {
		
		boolean addFile = false;
		if (export.getAddFilesToPage().equals(ContentGeneratorCst.VALUE_ALL)) {
			addFile = true;
		} else if (export.getAddFilesToPage().equals(ContentGeneratorCst.VALUE_RANDOM)) {
			Random random  = new Random();			
			addFile = random.nextBoolean();
		}
		
		String fileName = null;
		if (addFile) {
			FileService fileService = FileService.getInstance();
			 fileName = fileService.getFileName(export.getFileNames());
		}
		
		logger.debug("		Creating new page level " + level + " - Page " + ContentGeneratorService.currentPageIndex + " - Article FR "
				+ articleFr.getId() + " - Article EN " + articleEn.getId() + " - file attached "+fileName);
		
		
		PageBO page = new PageBO(ContentGeneratorService.currentPageIndex, formatForXml(articleEn.getTitle()),
				formatForXml(articleEn.getContent()), formatForXml(articleFr.getTitle()),
				formatForXml(articleFr.getContent()), level, subPages, export.getPagesHaveVanity(), export.getSiteKey(), fileName);
		ContentGeneratorService.currentPageIndex = ContentGeneratorService.currentPageIndex + 1;
		return page;
	}
	
	/**
	 * getPagesPathrecursively retrieves absolute paths for each page, from the
	 * top page. If choosen by the user, a map of this path will be generated.
	 * It can be used to run performance tests.
	 * 
	 * @param pages
	 *            list of the top pages
	 * @param path
	 *            this method is recursive, this is the path generated for the
	 *            pages above
	 * @return String containing all the generated paths, one per line
	 */
	public List<String> getPagesPath(List<PageBO> pages, String path) {
		List<String> siteMap = new ArrayList<String>();

		for (Iterator<PageBO> iterator = pages.iterator(); iterator.hasNext();) {
			PageBO page = (PageBO) iterator.next();
			String newPath = path + "/" + page.getUniqueName();
			logger.debug("new path: " + newPath);
			siteMap.add(newPath);
			if (page.getSubPages() != null) {
				siteMap.addAll(getPagesPath(page.getSubPages(), newPath));
			}
		}

		return siteMap;
	}

	/**
	 * Convert & \ < > and ' into they HTML equivalent
	 * 
	 * @param s
	 *            XML string to format
	 * @return formatted XML string
	 */
	public String formatForXml(final String s) {
		String formattedString = StringUtils.replace(s, "&", "&amp;");
		formattedString = StringUtils.replace(formattedString, "\"", " &quot;");
		formattedString = StringUtils.replace(formattedString, "<", "&lt;");
		formattedString = StringUtils.replace(formattedString, ">", "&gt;");
		formattedString = StringUtils.replace(formattedString, "'", "&#39;");
		return formattedString;
	}
}
