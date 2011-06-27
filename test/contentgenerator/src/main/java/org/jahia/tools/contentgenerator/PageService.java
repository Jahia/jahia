package org.jahia.tools.contentgenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.jahia.tools.contentgenerator.bo.PageBO;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;

public class PageService {
	private static final Logger logger = Logger.getLogger(PageService.class.getName());

	String sep;
    private Random random = new Random();

    public PageService() {
		sep = System.getProperty("file.separator");
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

        Map<String,ArticleBO> articlesMap = new HashMap<String, ArticleBO>();
        for (String language : export.getSiteLanguages()) {
            articlesMap.put(language,articleService.getArticle(articles));
        }

		String rootPageName = export.getRootPageName();

		PageBO rootPage = createNewPage(export, rootPageName, articlesMap, export.getNbSubLevels() + 1, null);

		OutputService outService = new OutputService();
        outService.initOutputFile(export.getOutputFile());
		outService.appendStringToFile(export.getOutputFile(), export.toString());
		outService.appendStringToFile(export.getOutputFile(), rootPage.getHeader());

		for (int i = 1; i <= export.getNbPagesTopLevel().intValue(); i++) {
            for (String language : export.getSiteLanguages()) {
                articlesMap.put(language,articleService.getArticle(articles));
            }
			pageTopLevel = createNewPage(export, null, articlesMap, export.getNbSubLevels() + 1,
					createSubPages(export, articles, export.getNbSubLevels(), export.getMaxArticleIndex()));
			outService.appendPageToFile(export.getOutputFile(), pageTopLevel);

			// path
			if (export.getCreateMap()) {
				logger.info("Pages path are being written to the map file");
				List<PageBO> listeTopPage = new ArrayList<PageBO>();
				listeTopPage.add(pageTopLevel);

				List<String> pagesPath = getPagesPath(listeTopPage, ContentGeneratorCst.PAGE_PATH_SEPARATOR + rootPage.getUniqueName());
				outService.appendPathToFile(export.getMapFile(), pagesPath);
			}

			logger.debug("XML code of top level page #" + i + " written in output file");
			logger.info("Top page #" + i + " with subpages created and written to file");
		}
		outService.appendStringToFile(export.getOutputFile(), rootPage.getFooter());
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
	 * @param level
	 *            Current level in the tree decrease each time, top level ==
	 *            number of levels requested by the user)
	 * @param maxArticleIndex
	 *            Index of the last article
	 * @return A list of Page BO, containing their sub pages (if they have some)
	 */
	public List<PageBO> createSubPages(ExportBO export, List<ArticleBO> articles, Integer level, Integer maxArticleIndex) {
		ArticleService articleService = ArticleService.getInstance();
		List<PageBO> listePages = new ArrayList<PageBO>();
		Map<String,ArticleBO> articlesMap;

		int nbPagesPerLevel = export.getNbSubPagesPerPage();

		listePages.clear();
		if (level.intValue() > 0) {
			for (int i = 0; i < nbPagesPerLevel; i++) {
                articlesMap = new HashMap<String, ArticleBO>();
                for (String language : export.getSiteLanguages()) {
                    articlesMap.put(language,articleService.getArticle(articles));
                }
				PageBO page = createNewPage(export, null, articlesMap, level,
						createSubPages(export, articles, level.intValue() - 1, maxArticleIndex));
				listePages.add(page);
			}
		}
		return listePages;
	}

	/**
	 * Create a new page object
	 * 
	 *
     * @param export
     *            Export BO contains all the parameters chose by the user to
     *            configure his/her content generation
     * @param pageName
     *            Used only for root page, or to specify a page name If null,
     *            creates a unique page name from concatenation of page name
     *            prefix and unique ID
     * @param articlesMap
     *@param level
     *            Current level in the tree
     * @param subPages
*            List of sub pages related   @return
	 */
	public PageBO createNewPage(ExportBO export, String pageName, Map<String, ArticleBO> articlesMap, int level,
                                List<PageBO> subPages) {

		boolean addFile = false;
		if (export.getAddFilesToPage().equals(ContentGeneratorCst.VALUE_ALL)) {
			addFile = true;
		} else if (export.getAddFilesToPage().equals(ContentGeneratorCst.VALUE_RANDOM)) {
			Random random = new Random();
			addFile = random.nextBoolean();
		}

		String fileName = null;
		if (addFile) {
			FileService fileService = new FileService();
			fileName = fileService.getFileName(export.getFileNames());
		}

		logger.debug("		Creating new page level " + level + " - Page " + ContentGeneratorService.currentPageIndex
				+ " - Articles " + articlesMap + " - file attached "
				+ fileName);

		if (pageName == null) {
			pageName = ContentGeneratorCst.PAGE_NAME_PREFIX + ContentGeneratorService.currentPageIndex;
		}

        HashMap<String, List<String>> acls = new HashMap<String, List<String>>();
        if (random.nextFloat() < export.getGroupAclRatio()) {
            acls.put("g:group"+random.nextInt(export.getNumberOfGroups()), Arrays.asList("editor"));
        }
        if (random.nextFloat() < export.getUsersAclRatio()) {
            acls.put("u:user"+random.nextInt(export.getNumberOfUsers()), Arrays.asList("editor"));
        }

        PageBO page = new PageBO(pageName, articlesMap, level, subPages,
				export.getPagesHaveVanity(), export.getSiteKey(), fileName, export.getNumberOfBigTextPerPage(), acls);
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

		if (path == null) {
			path = "";
		}
		for (Iterator<PageBO> iterator = pages.iterator(); iterator.hasNext();) {
			PageBO page = iterator.next();
			String newPath = path + ContentGeneratorCst.PAGE_PATH_SEPARATOR + page.getUniqueName();
			logger.debug("new path: " + newPath);
			siteMap.add(newPath);
			if (page.getSubPages() != null) {
				siteMap.addAll(getPagesPath(page.getSubPages(), newPath));
			}
		}

		return siteMap;
	}

}
