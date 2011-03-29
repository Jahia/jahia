package org.jahia.tools.contentgenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.jahia.tools.contentgenerator.bo.PageBO;
import org.jahia.tools.contentgenerator.bo.SiteBO;
import org.springframework.util.StringUtils;

public class XmlService {
	private static final Logger logger = Logger.getLogger(XmlService.class.getName());

	private static int currentPageIndex = 0;

	public XmlService() {

	}

	private PageBO createNewPage(ArticleBO articleEn, ArticleBO articleFr, int level, List<PageBO> subPages) {
		logger.debug("		Creating new page level " + level + " - Page " + currentPageIndex + " - Article FR " + articleFr.getId() + " - Article EN "+articleEn.getId());
		PageBO page = new PageBO(currentPageIndex, formatForXml(articleEn.getTitle()),
				formatForXml(articleEn.getContent()), formatForXml(articleFr.getTitle()),
				formatForXml(articleFr.getContent()), level, subPages);
		currentPageIndex = currentPageIndex + 1;
		return page;
	}

	private ArticleBO getArticle(List<ArticleBO> articles, Integer maxArticleIndex) {
		Random generator = new Random();
		int index = generator.nextInt(maxArticleIndex);
		return (ArticleBO) articles.get(index);
	}

	private List<PageBO> createSubPages(List<ArticleBO> articles, Integer articleIndex, Integer nbPagesPerLevel,
			Integer level, Integer maxArticleIndex) {
		List<PageBO> listePages = new ArrayList<PageBO>();
		ArticleBO articleEn;
		ArticleBO articleFr;
		listePages.clear();
		if (level.intValue() > 0) {
			for (int i = 0; i < nbPagesPerLevel; i++) {
				articleEn = getArticle(articles, maxArticleIndex);
				articleFr = getArticle(articles, maxArticleIndex);
				PageBO page = createNewPage(
						articleEn,
						articleFr,
						level,
						createSubPages(articles, articleIndex.intValue() + 1, nbPagesPerLevel, level.intValue() - 1,
								maxArticleIndex));
				listePages.add(page);
			}
		}
		return listePages;
	}

	private String formatForXml(final String s) {
		String formattedString = StringUtils.replace(s, "&", "&amp;");
		formattedString = StringUtils.replace(formattedString, "\"", " &quot;");
		formattedString = StringUtils.replace(formattedString, "<", "&lt;");
		formattedString = StringUtils.replace(formattedString, ">", "&gt;");
		formattedString = StringUtils.replace(formattedString, "'", "&#39;");
		return formattedString;
	}

	public void createTopPages(ExportBO export, List<ArticleBO> articles) throws IOException {
		logger.info("Creating top pages");
		SiteBO newSite = new SiteBO(null);
		OutputService outService = new OutputService();
		outService.initOutputFile(export.getOutputFile());
		outService.appendStringToFile(export.getOutputFile(), newSite.getHeader());

		PageBO pageTopLevel = null;
		ArticleBO articleEn = null;
		ArticleBO articleFr = null;
		for (int i = 1; i <= export.getNbPagesTopLevel().intValue(); i++) {
			articleEn = getArticle(articles, export.getMaxArticleIndex());
			articleFr = getArticle(articles, export.getMaxArticleIndex());
			pageTopLevel = createNewPage(
					articleEn,
					articleFr,
					export.getNbSubLevels() + 1,
					createSubPages(articles, currentPageIndex, export.getNbSubPagesPerPage(), export.getNbSubLevels(),
							export.getMaxArticleIndex()));
			outService.appendPageToFile(export.getOutputFile(), pageTopLevel);
			logger.debug("XML code of top level page #" + i + " written in output file");
			logger.info("Top page #" + i + " with subpages created and written to file");
		}
		outService.appendStringToFile(export.getOutputFile(), newSite.getFooter());
	}
}
