package org.jahia.tools.contentgenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;
import org.jahia.tools.contentgenerator.properties.DatabaseProperties;

public class ContentGeneratorService {

	private static final Logger logger = Logger.getLogger(ContentGenerator.class.getName());

	public void execute(Properties properties) {
		long start = System.currentTimeMillis();
		logger.info("Jahia content generator starts");

		ExportBO export = initExport(properties);
		if (checkParameters(export)) {
			logger.info(export.getTotalPages() + " pages will be created");

			List<ArticleBO> articles = DatabaseService.getInstance().getArticles(export);

			generateExport(export, articles);

			long stop = System.currentTimeMillis();
			long duration = stop - start;
			logger.info("Completed, " + export.getTotalPages() + " pages created in ~" + duration / 1000 + "s");
			logger.info("Please remember that your export contains articles randomly picked from Wikipedia data, and the content generator is not responsible for the articles content. Thank you!");
		} else {
			logger.error("Wrong parameter(s), fail.");
		}
	}

	/**
	 * Get properties and initialize ExportBO
	 * 
	 * @TODO: manage null for each property
	 * @TODO: check values
	 * @param properties
	 * @return a new export BO containing all the parameters
	 */
	private ExportBO initExport(Properties properties) {
		ExportBO export = new ExportBO();
		Integer nbPagesTopLevel;
		Integer nbSubPagesPerPage;
		Integer nbSubLevels;

		File outputFile;

		Boolean createMap;
		File mapFile = null;
		Boolean pagesHaveVanity = null;
		String siteKey = null;
		String addFilesToPage = null;
		String filesDirectory = null;

		if (properties == null) {
			logger.info("Properties not found, default properties used.");
			nbPagesTopLevel = ContentGeneratorCst.NB_PAGES_TOP_LEVEL_DEFAULT;
			nbSubPagesPerPage = ContentGeneratorCst.NB_SUBPAGES_PER_PAGE_DEFAULT;
			nbSubLevels = ContentGeneratorCst.NB_SUB_LEVELS_DEFAULT;
			/** @FIXM: use default variable */
			outputFile = new File("output.xml");

			createMap = Boolean.FALSE;
			pagesHaveVanity = ContentGeneratorCst.HAS_VANITY_DEFAULT;
			siteKey = ContentGeneratorCst.SITE_KEY_DEFAULT;
			addFilesToPage = ContentGeneratorCst.VALUE_NONE;
		} else {
			nbPagesTopLevel = Integer.valueOf(properties.getProperty(ContentGeneratorCst.NB_PAGES_TOP_LEVEL));
			nbSubPagesPerPage = Integer.valueOf(properties.getProperty(ContentGeneratorCst.NB_SUBPAGES_PER_PAGE));
			nbSubLevels = Integer.valueOf(properties.getProperty(ContentGeneratorCst.NB_SUB_LEVELS));

			String outputDir = properties.getProperty(ContentGeneratorCst.OUTPUT_DIR_PROPERTY);
			String filename = properties.getProperty(ContentGeneratorCst.OUTPUT_FILE_PROPERTY);
			String pathSeparator = System.getProperty("file.separator");
			String outputFilePath = outputDir + pathSeparator + filename;
			outputFile = new File(outputFilePath);

			createMap = new Boolean(properties.getProperty(ContentGeneratorCst.CREATE_MAP_YN_PROPERTY));
			if (createMap) {
				mapFile = new File(outputDir + pathSeparator + "jahiaGeneratedExport_map.csv");
			}
			String s = properties.getProperty(ContentGeneratorCst.PAGES_HAVE_VANITY_PROPERTY);
			pagesHaveVanity = new Boolean(s);
			siteKey = properties.getProperty(ContentGeneratorCst.SITE_KEY_PROPERTY);

			addFilesToPage = properties.getProperty(ContentGeneratorCst.ADD_FILE_PROPERTY);

			filesDirectory = properties.getProperty(ContentGeneratorCst.FILE_POOL_PROPERTY);
			if (!new File(filesDirectory).exists()) {
				logger.error("Directory containing files to attach does not exist or is not readable: "
						+ filesDirectory);
			}
		}

		export.setNbPagesTopLevel(nbPagesTopLevel);
		export.setNbSubLevels(nbSubLevels);
		export.setNbSubPagesPerPage(nbSubPagesPerPage);
		export.setOutputFile(outputFile);
		export.setCreateMap(createMap);
		export.setMapFile(mapFile);
		export.setPagesHaveVanity(pagesHaveVanity);
		export.setSiteKey(siteKey);
		export.setAddFilesToPage(addFilesToPage);
		export.setFilesDirectory(filesDirectory);
		
		if (export.getAddFilesToPage() != null && !export.getAddFilesToPage().equals(ContentGeneratorCst.VALUE_NONE)) {
			export.setFileNames(getFileNamesAvailable(export.getFilesDirectory()));
		}

		Integer totalPages = getTotalNumberOfPagesNeeded(nbPagesTopLevel, nbSubLevels, nbSubPagesPerPage);
		export.setTotalPages(totalPages);

		/**
		 * Database
		 */
		DatabaseProperties.HOSTNAME = properties.getProperty(ContentGeneratorCst.DB_HOST_PROPERTY);
		DatabaseProperties.DATABASE = properties.getProperty(ContentGeneratorCst.DB_DATABASE_PROPERTY);
		DatabaseProperties.USER = properties.getProperty(ContentGeneratorCst.DB_LOGIN_PROPERTY);
		DatabaseProperties.PASSWORD = properties.getProperty(ContentGeneratorCst.DB_PASSWORD_PROPERTY);
		DatabaseProperties.TABLE = properties.getProperty(ContentGeneratorCst.DB_TABLE_PROPERTY);

		return export;
	}

	private void generateExport(ExportBO export, List<ArticleBO> articles) {
		XmlService xmlManager = new XmlService();
		try {
			xmlManager.createTopPages(export, articles);
			logger.info("XML import file available here: " + export.getOutputFile().getAbsolutePath());
			if (export.getCreateMap()) {
				logger.info("Paths list available here: " + export.getMapFile().getAbsolutePath());
			}

		} catch (IOException e) {
			logger.error("Error while writing to output file: ", e);
		}
	}

	private static boolean checkParameters(ExportBO export) {
		boolean parametersValidity = true;
		if (export.getTotalPages().compareTo(ContentGeneratorCst.MAX_TOTAL_PAGES) > 0) {
			logger.error("You asked to generate " + export.getTotalPages() + " pages, the maximum allowed is "
					+ ContentGeneratorCst.MAX_TOTAL_PAGES);
			parametersValidity = false;
		}
		return parametersValidity;
	}

	private Integer getTotalNumberOfPagesNeeded(Integer nbPagesTopLevel, Integer nbLevels, Integer nbPagesPerLevel) {
		Double nbPages = new Double(0);
		for (double d = nbLevels; d > 0; d--) {
			nbPages += Math.pow(nbPagesPerLevel.doubleValue(), d);
		}
		nbPages = nbPages * nbPagesTopLevel + nbPagesTopLevel;

		return new Integer(nbPages.intValue());
	}

	/**
	 * Returns a list of the files that can be used as attachements
	 * @param filesDirectoryPath
	 *            directory containing the files that will be uploaded into the
	 *            Jahia repository and can be used as attachements
	 * @return list of file names
	 */
	private List<String> getFileNamesAvailable(String filesDirectoryPath) {
		List<String> fileNames = new ArrayList<String>();
		File filesDir = new File(filesDirectoryPath);
		File[] files = filesDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				logger.debug("\"" + files[i].getName() + "\" added to the list of available files.");
				fileNames.add(files[i].getName());
			}
		}
		return fileNames;
	}
}
