package org.jahia.tools.contentgenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.jahia.tools.contentgenerator.bo.PageBO;
import org.jfree.util.Log;
import org.springframework.util.StringUtils;

public class XmlService {
	private static final Logger logger = Logger.getLogger(XmlService.class.getName());

	private static int currentPageIndex = 0;

	public XmlService() {

	}

	public void createTopPages(ExportBO export, List<ArticleBO> articles) throws IOException {
		logger.info("Creating top pages");
		PageBO pageTopLevel = null;
		ArticleBO articleEn = null;
		ArticleBO articleFr = null;

		articleEn = getArticle(articles, export.getMaxArticleIndex());
		articleFr = getArticle(articles, export.getMaxArticleIndex());
		PageBO homePage = createNewPage(export, articleEn, articleFr, export.getNbSubLevels() + 1, null);

		OutputService outService = new OutputService();
		outService.initOutputFile(export.getOutputFile());
		outService.appendStringToFile(export.getOutputFile(), export.toString());
		outService.appendStringToFile(export.getOutputFile(), homePage.getHeader());

		for (int i = 1; i <= export.getNbPagesTopLevel().intValue(); i++) {
			articleEn = getArticle(articles, export.getMaxArticleIndex());
			articleFr = getArticle(articles, export.getMaxArticleIndex());
			pageTopLevel = createNewPage(
					export,
					articleEn,
					articleFr,
					export.getNbSubLevels() + 1,
					createSubPages(export, articles, currentPageIndex, export.getNbSubLevels(),
							export.getMaxArticleIndex()));
			outService.appendPageToFile(export.getOutputFile(), pageTopLevel);

			// path
			if (export.getCreateMap()) {
				Log.info("Pages path are being written to the map file");
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

	private List<PageBO> createSubPages(ExportBO export, List<ArticleBO> articles, Integer articleIndex, Integer level,
			Integer maxArticleIndex) {
		List<PageBO> listePages = new ArrayList<PageBO>();
		ArticleBO articleEn;
		ArticleBO articleFr;

		int nbPagesPerLevel = export.getNbSubPagesPerPage();

		listePages.clear();
		if (level.intValue() > 0) {
			for (int i = 0; i < nbPagesPerLevel; i++) {
				articleEn = getArticle(articles, maxArticleIndex);
				articleFr = getArticle(articles, maxArticleIndex);
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

	private PageBO createNewPage(ExportBO export, ArticleBO articleEn, ArticleBO articleFr, int level,
			List<PageBO> subPages) {
		logger.debug("		Creating new page level " + level + " - Page " + currentPageIndex + " - Article FR "
				+ articleFr.getId() + " - Article EN " + articleEn.getId());
		PageBO page = new PageBO(currentPageIndex, formatForXml(articleEn.getTitle()),
				formatForXml(articleEn.getContent()), formatForXml(articleFr.getTitle()),
				formatForXml(articleFr.getContent()), level, subPages, export.getPagesHaveVanity(), export.getSiteKey());
		currentPageIndex = currentPageIndex + 1;
		return page;
	}

	private ArticleBO getArticle(List<ArticleBO> articles, Integer maxArticleIndex) {
		Random generator = new Random();
		int index;
		if (currentPageIndex <= maxArticleIndex) {
			index = currentPageIndex;
		} else {
			index = generator.nextInt(maxArticleIndex);
		}
		return (ArticleBO) articles.get(index);
	}

	private List<String> getPagesPath(List<PageBO> pages, String path) {
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

	private String formatForXml(final String s) {
		String formattedString = StringUtils.replace(s, "&", "&amp;");
		formattedString = StringUtils.replace(formattedString, "\"", " &quot;");
		formattedString = StringUtils.replace(formattedString, "<", "&lt;");
		formattedString = StringUtils.replace(formattedString, ">", "&gt;");
		formattedString = StringUtils.replace(formattedString, "'", "&#39;");
		return formattedString;
	}
}
