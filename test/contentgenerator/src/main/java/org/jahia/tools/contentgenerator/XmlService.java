package org.jahia.tools.contentgenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.PageBO;
import org.jahia.tools.contentgenerator.bo.SiteBO;
import org.jfree.util.Log;

public class XmlService {

	private File outputFile;

	// write into file and flush the string every...
	private static final int RECORDS_GROUP_SIZE = 2500;
	private static final Logger logger = Logger.getLogger(XmlService.class.getName());

	private static int currentArticleIndex = 0;
	private static int currentPageIndex = 0;

	public XmlService() {

	}

	private void writePagesToFile(File f, List listePages, int cpt) throws IOException {
		appendPagesToFile(this.outputFile, listePages);
		logger.info(cpt + " pages written into temp file.");
		logger.debug("Temp file: " + this.outputFile.getAbsolutePath());
		// listePages.clear();
	}

	private void appendPagesToFile(File f, List listePages) throws IOException {
		FileWriter fwriter = new FileWriter(f, true);
		BufferedWriter fOut = new BufferedWriter(fwriter);
		for (Iterator iterator = listePages.iterator(); iterator.hasNext();) {
			PageBO page = (PageBO) iterator.next();
			fOut.write(page.toString());
		}
		fOut.close();
	}

	private void createTempPagesFile(final String fileName) throws IOException {
		String outputFileDir = ContentGenerator.getProperty(ContentGeneratorCst.OUTPUT_DIR_PROPERTY);
		String pathSeparator = System.getProperty("file.separator");

		String outputFileName = outputFileDir + pathSeparator + fileName;
		this.outputFile = new File(outputFileName);
		FileUtils.writeStringToFile(outputFile, "");
	}

	private PageBO createNewPage(ArticleBO article, int level, List<PageBO> subPages) {
		logger.debug("		Creating new page level " + level + " - Page " + currentPageIndex);
		PageBO page = new PageBO(currentPageIndex, article.getTitle(), article.getContent(), level, subPages);
		currentPageIndex = currentPageIndex + 1;
		return page;
	}

	/**
	 * @deprecated
	 * 
	 * @param fileName
	 * @param articles
	 * @throws IOException
	 */
	// public void createNewSiteDocument(String fileName, List articles) throws
	// IOException {
	// // creer fichier temp
	// createTempPagesFile(fileName);
	//
	// // boucle
	// List listePages = new ArrayList();
	// int cpt = 0;
	// for (Iterator iterator = articles.iterator(); iterator.hasNext();) {
	// cpt++;
	// logger.debug("Record #" + cpt);
	// ArticleBO article = (ArticleBO) iterator.next();
	// PageBO newPage = createNewPage(article);
	// listePages.add(newPage);
	//
	//
	// if (cpt % RECORDS_GROUP_SIZE == 0) {
	// writePagesToFile(this.outputFile, listePages, cpt);
	// }
	// }
	// writePagesToFile(this.outputFile, listePages, cpt);
	//
	// SiteBO newSite = new SiteBO(listePages);
	//
	// FileUtils.writeStringToFile(this.outputFile, newSite.toString());
	// logger.info("XML import file available here: " +
	// this.outputFile.getAbsolutePath());
	// }

	/**
	 * articles
	 * 
	 * @param fileName
	 * @param articles
	 * @throws IOException
	 */
	public void createNewSiteDocument(String fileName, List<ArticleBO> articles, Integer nbPagesTopLevel,
			Integer nbSubLevels, Integer nbPagesPerLevel) throws IOException {

		// creer fichier temp
		createTempPagesFile(fileName);

		// int articleIndex = 0;
		// ArticleBO article = null;
		// PageBO page = null;
		// List<PageBO> pagesTopLevel = new ArrayList<PageBO>();
		// // @TODO: this page wound't mind to be refactored a bit... like
		// recursivity
		// for (int indexPagesTopLevel = 1; indexPagesTopLevel <=
		// nbPagesTopLevel.intValue(); indexPagesTopLevel++) {
		// logger.debug("Top level - page #"+indexPagesTopLevel);
		// List<PageBO> subPagesTopLevel = new ArrayList<PageBO>();
		// for (int indexSubLevel = 1; indexSubLevel <= nbSubLevels.intValue();
		// indexSubLevel++) {
		// logger.debug("	|- Sub level - page #"+indexSubLevel);
		// List<PageBO> subPagesLevel = new ArrayList<PageBO>();
		// for (int indexPagesPerLevel = 1; indexPagesPerLevel <=
		// nbPagesPerLevel.intValue(); indexPagesPerLevel++) {
		// logger.debug("		|- Page #"+indexPagesPerLevel);
		// article = getArticle(articles, articleIndex);
		// articleIndex=articleIndex+1;
		// page = createNewPage(article, indexPagesPerLevel, null);
		// subPagesLevel.add(page);
		// }
		// article = getArticle(articles, articleIndex);
		// articleIndex=articleIndex+1;
		// page = createNewPage(article, indexSubLevel, subPagesLevel);
		// subPagesTopLevel.add(page);
		// }
		//
		// article = getArticle(articles, articleIndex);
		// articleIndex=articleIndex+1;
		// page = createNewPage(article, indexPagesTopLevel, subPagesTopLevel);
		// pagesTopLevel.add(page);
		// }

		List<PageBO> pagesTopLevel = new ArrayList<PageBO>();
		PageBO pageTopLevel = null;
		ArticleBO article = null;
		for (int i = 0; i < nbPagesTopLevel.intValue(); i++) {
			article = getNextArticle(articles, currentPageIndex);
			pageTopLevel = createNewPage(article, nbSubLevels + 1,
					createSubPages(articles, currentPageIndex, nbPagesPerLevel, nbSubLevels));
			pagesTopLevel.add(pageTopLevel);
		}
		SiteBO newSite = new SiteBO(pagesTopLevel);

		FileUtils.writeStringToFile(this.outputFile, newSite.toString());
		logger.info("XML import file available here: " + this.outputFile.getAbsolutePath());
	}

	private ArticleBO getNextArticle(List<ArticleBO> articles, int articleIndex) {
		return (ArticleBO) articles.get(articleIndex);
	}

	private List<PageBO> createSubPages(List<ArticleBO> articles, Integer articleIndex, Integer nbPagesPerLevel,
			Integer level) {
		List<PageBO> listePages = new ArrayList<PageBO>();
		listePages.clear();
		ArticleBO article = getNextArticle(articles, currentPageIndex);
		if (level.intValue() > 0) {
			for (int i = 0; i < nbPagesPerLevel; i++) {
				PageBO page = createNewPage(article, level,
						createSubPages(articles, articleIndex.intValue() + 1, nbPagesPerLevel, level.intValue() - 1));
				listePages.add(page);
			}
		}
		return listePages;
	}
}
