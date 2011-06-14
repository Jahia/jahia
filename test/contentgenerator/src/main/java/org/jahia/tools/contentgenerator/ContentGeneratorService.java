package org.jahia.tools.contentgenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu.Separator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.jahia.tools.contentgenerator.bo.SiteBO;
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
	 * Generates an XML files containing the pages tree and that can be imported
	 * into a Jahia website
	 * 
	 * @param export
	 */
	public void generatePages(ExportBO export) throws MojoExecutionException, IOException {
		if (!ContentGeneratorCst.VALUE_NONE.equals(export.getAddFilesToPage()) && export.getFileNames().isEmpty()) {
			throw new MojoExecutionException(
					"Directory containing files to include is empty, use jahia-cg:generate-files first");
		}

		List<ArticleBO> articles = DatabaseService.getInstance().selectArticles(export, export.getTotalPages());

		PageService pageService = new PageService();
		pageService.createTopPages(export, articles);
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

		List<ArticleBO> articles = DatabaseService.getInstance().selectArticles(export,
				export.getNumberOfFilesToGenerate());
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
	 * Generates pages and then creates a ZIP archive containing those pages,
	 * the files needed for attachments and site.properties
	 * 
	 * @param export
	 * @return Absolute path of ZIP file created
	 * @throws MojoExecutionException
	 */
	public String generateSite(ExportBO export) throws MojoExecutionException {
		String zipFilePath = null;
		
		// as we create a full site we will need a home page
		export.setRootPageName(ContentGeneratorCst.ROOT_PAGE_NAME);
		SiteBO site = new SiteBO();
		site.setSiteKey(export.getSiteKey());
		
		try {
			SiteService siteService = new SiteService();

			generatePages(export);

			List<File> filesToZip = new ArrayList<File>();

			// create temporary dir in output dir (siteKey)
			File tempOutputDir = siteService.createSiteDirectory(export.getSiteKey(), new File(export.getOutputDir()));
						
			// create properties file
			File propertiesFile = siteService.createPropertiesFile(export.getSiteKey(), tempOutputDir);
			filesToZip.add(propertiesFile);

			// create tree dirs for files attachments (if files are not at
			// "none")
			File tempXmlFile = null;
			if (!ContentGeneratorCst.VALUE_NONE.equals(export.getAddFilesToPage())) {
				FileService fileService = new FileService();
				File filesDirectory = siteService.createFilesDirectoryTree(export.getSiteKey(), tempOutputDir);
				filesToZip.add(new File(tempOutputDir + "/content"));
				
				// get all files available in the pool dir
				List<File> filesToCopy = fileService.getFilesAvailable(export.getFilesDirectory());

				// if there are more files available than pages created, we copy
				// only the total of files that have been used
				if (filesToCopy.size() > export.getTotalPages()) {
					filesToCopy = filesToCopy.subList(0, export.getTotalPages() - 1);
				}

				fileService.copyFilesForAttachment(filesToCopy, filesDirectory);
				
				// generates XML code for files
				tempXmlFile = new File(export.getOutputDir() + System.getProperty("file.separator") + "jcrFiles.xml");
				fileService.createAndPopulateFilesXmlFile(tempXmlFile, filesToCopy);
			}
			
			// 2 - copy pages => repository.xml
			File repositoryFile = siteService.createAndPopulateRepositoryFile(tempOutputDir, site, export.getOutputFile(), tempXmlFile);
			filesToZip.add(repositoryFile);
			
			OutputService os = new OutputService();
			String zipFileName = export.getSiteKey() + ".zip";
			os.createSiteArchive(zipFileName, tempOutputDir.getAbsolutePath(), filesToZip);
			zipFilePath = tempOutputDir.getParentFile().getAbsolutePath() + System.getProperty("file.separator") + zipFileName;
		} catch (IOException e) {
			throw new MojoExecutionException("Exception while creating the website ZIP archive: " + e);
		}
		return zipFilePath;
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
