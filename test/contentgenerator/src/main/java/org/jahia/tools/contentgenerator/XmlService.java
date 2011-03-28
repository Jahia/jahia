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

	private PageBO createNewPage(ArticleBO article, int level, List<PageBO> subPages) {
		logger.debug("		Creating new page level " + level + " - Page " + currentPageIndex);
		PageBO page = new PageBO(currentPageIndex, formatForXml(article.getTitle()), formatForXml(article.getContent()), level, subPages);
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
		listePages.clear();
		ArticleBO article = getArticle(articles, maxArticleIndex);
		if (level.intValue() > 0) {
			for (int i = 0; i < nbPagesPerLevel; i++) {
				PageBO page = createNewPage(
						article,
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

		SiteBO newSite = new SiteBO(null);
		OutputService outService = new OutputService();
		outService.initOutputFile(export.getOutputFile());
		outService.appendStringToFile(export.getOutputFile(), newSite.getHeader());

		PageBO pageTopLevel = null;
		ArticleBO article = null;
		for (int i = 1; i <= export.getNbPagesTopLevel().intValue(); i++) {
			article = getArticle(articles, export.getMaxArticleIndex());
			pageTopLevel = createNewPage(
					article,
					export.getNbSubLevels() + 1,
					createSubPages(articles, currentPageIndex, export.getNbSubPagesPerPage(), export.getNbSubLevels(),
							export.getMaxArticleIndex()));
			logger.debug("XML code of top level page #" + i + " written in output file");
			outService.appendPageToFile(export.getOutputFile(), pageTopLevel);

			outService.appendStringToFile(export.getOutputFile(), newSite.getFooter());
		}
	}
}
