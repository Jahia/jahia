package org.jahia.tools.contentgenerator;

import java.util.List;
import java.util.Random;

import org.jahia.tools.contentgenerator.bo.ArticleBO;

public class ArticleService {
	private static ArticleService instance;
	
	private ArticleService() {
		
	}
	
	public static ArticleService getInstance() {
		if (instance == null) {
			instance = new ArticleService();
		}
		return instance;
	}
	
	/**
	 * Chooses an article in the list of available articles. As long as not used
	 * yet articles remain, chooses the next one. When all the articles have
	 * been used, randomly picks another one.
	 * 
	 * @param articles
	 *            All articles BO available
	 * @param maxArticleIndex
	 * @return
	 */
	public ArticleBO getArticle(List<ArticleBO> articles) {
		Random generator = new Random();
		int maxIndex = articles.size() - 1;
		int index;
		if (ContentGeneratorService.currentPageIndex <= maxIndex) {
			index = ContentGeneratorService.currentPageIndex;
		} else {
			index = generator.nextInt(maxIndex);
		}
		return articles.get(index);
	}
}
