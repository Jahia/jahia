package org.jahia.tools.contentgenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;

public class ContentGeneratorService {

	public static ContentGeneratorService instance;
	
	private static final Logger logger = Logger.getLogger(ContentGeneratorService.class.getName());

	public static int currentPageIndex = 0;
	
	public static int currentFileIndex = 0;
	
	private ContentGeneratorService() {
		
	}
	
	public static ContentGeneratorService getInstance() {
		if (instance == null) {
			instance = new ContentGeneratorService();
		}
		return instance;
	}
	
	/**
	 * 
	 * @param export
	 */
	public void generatePages(ExportBO export) throws MojoExecutionException {
        if (!ContentGeneratorCst.VALUE_NONE.equals(export.getAddFilesToPage()) && export.getFileNames().isEmpty()) {
            throw new MojoExecutionException(
                    "Directory containing files to include is empty, use jahia-cg:generate-files first");
        }

		logger.info("Jahia content generator starts");
		logger.info(export.getTotalPages() + " pages will be created");

		List<ArticleBO> articles = DatabaseService.getInstance().selectArticles(export, export.getTotalPages());

		PageService pageService = new PageService();
		
		try {
			pageService.createTopPages(export, articles);
			logger.info("XML import file available here: " + export.getOutputFile().getAbsolutePath());
			if (export.getCreateMap()) {
				logger.info("Paths list available here: " + export.getMapFile().getAbsolutePath());
			}

			logger.info("Completed, " + export.getTotalPages() + " pages created");
			logger.info("Please remember that your export contains articles randomly picked from Wikipedia data, and the content generator is not responsible for the articles content. Thank you!");

		} catch (IOException e) {
			logger.error("Error while writing to output file: ", e);
		}
	}

	/**
	 * Generates text files that can be used as attachments, with random content
	 * from the articles database
	 * 
	 * @param export
	 * @param articles
	 * @throws MojoExecutionException
	 */
	public void generateFiles(ExportBO export) throws MojoExecutionException {
		logger.info("Jahia files generator starts");

		Integer numberOfFilesToGenerate = export.getNumberOfFilesToGenerate();
		if (numberOfFilesToGenerate == null) {
			throw new MojoExecutionException("numberOfFilesToGenerate parameter is null");
		}

		List<ArticleBO> articles = DatabaseService.getInstance().selectArticles(export, export.getNumberOfFilesToGenerate());
		int indexArticle = 0;
		File outputFile;

		for (int i = 0; i < numberOfFilesToGenerate; i++) {
			if (indexArticle == articles.size()) {
				indexArticle = 0;
			}

			String newFileName = "file." + i + ".txt";
			String newFilePath = export.getFilesDirectory() + System.getProperty("file.separator") + newFileName;
			outputFile = new File(newFilePath);

			try {
				FileUtils.writeStringToFile(outputFile, articles.get(indexArticle).getContent());
			} catch (IOException e) {
				throw new MojoExecutionException("Can't create new file: " + e.getMessage());
			}

			indexArticle++;
		}
	}
	
	/**
	 * Calculates the number of pages needed, used to know how much articles we
	 * will need
	 * 
	 * @param nbPagesTopLevel
	 * @param nbLevels
	 * @param nbPagesPerLevel
	 * @return number of pages needed
	 */
	public Integer getTotalNumberOfPagesNeeded(Integer nbPagesTopLevel, Integer nbLevels, Integer nbPagesPerLevel) {
		Double nbPages = new Double(0);
		for (double d = nbLevels; d > 0; d--) {
			nbPages += Math.pow(nbPagesPerLevel.doubleValue(), d);
		}
		nbPages = nbPages * nbPagesTopLevel + nbPagesTopLevel;

		return new Integer(nbPages.intValue());
	}
}
