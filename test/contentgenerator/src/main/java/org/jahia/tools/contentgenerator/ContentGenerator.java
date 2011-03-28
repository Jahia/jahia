package org.jahia.tools.contentgenerator;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.ArticleBO;

public class ContentGenerator {
	// @TODO: create goal Maven
	// @TODO: default properties
	// @TODO: gerer plusieurs niveaux de pages, configurable
	// @TODO: gerer contenu different pour fr et en
	// @TODO: decouper traitements n / 20,000
	// @TODO: ajouter verification environnement au debut (dossiers etc)
	public static Properties properties;

	private static final Logger logger = Logger.getLogger(ContentGenerator.class.getName());

	public static void main(final String[] args) {
		long start = System.currentTimeMillis();

		logger.info("Jahia content generator starts");
		readPropertiesFile();

		// select content
		// Integer nbrecords = new
		// Integer(getProperty(ContentGeneratorCst.NB_RECORDS_PROPERTY));
		Integer nbPagesTopLevel = new Integer(getProperty(ContentGeneratorCst.NB_PAGES_TOP_LEVEL));
		Integer nbLevels = new Integer(getProperty(ContentGeneratorCst.NB_LEVELS));
		Integer nbPagesPerLevel = new Integer(getProperty(ContentGeneratorCst.NB_PAGES_PER_LEVEL));

		Integer nbRecords = getTotalNumberOfPagesNeeded(nbPagesTopLevel, nbLevels, nbPagesPerLevel);
		if (checkParameters(nbRecords)) {
			logger.info(nbRecords + " are going to be generated");
			List<ArticleBO> articles = DatabaseService.getInstance().getArticles(nbRecords);

			XmlService xmlManager = new XmlService();
			String fileName = "JahiaExport-" + nbRecords + "-pages_" + nbLevels + "-levels.xml";

			try {
				xmlManager.createNewSiteDocument(fileName, articles, nbPagesTopLevel, nbLevels, nbPagesPerLevel);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long stop = System.currentTimeMillis();
			long duration = stop - start;
			logger.info("Completed, " + articles.size() + " pages created in ~" + duration / 1000 + "s");
			logger.info("Please remember that your export contains articles randomly picked from Wikipedia data, and the content generator is not responsible for the articles content. Thank you!");
		} else {
			logger.error("Invalid parameter, process stopped.");
		}
	}

	private static boolean checkParameters(Integer nbRecords) {
		boolean parametersValidity = true;
		Integer maxNbrecords = new Integer(25000);
		if (nbRecords.compareTo(maxNbrecords) > 0) {
		logger.error("You asked to generate " + nbRecords + " pages, the maximum allowed is 25,000.");
			parametersValidity = false;
		}
		return parametersValidity;
	}

	private static void readPropertiesFile() {
		properties = PropertyLoader.loadProperties(ContentGeneratorCst.PROPERTIES_FILENAME + ".properties");
	}

	public static String getProperty(final String propName) {
		return properties.getProperty(propName);
	}

	private static Integer getTotalNumberOfPagesNeeded(Integer nbPagesTopLevel, Integer nbLevels,
			Integer nbPagesPerLevel) {
//		int i = nbPagesTopLevel.intValue() * nbLevels.intValue() * nbPagesPerLevel.intValue();
//		i = i + nbPagesTopLevel.intValue() * nbPagesPerLevel.intValue();
//		i = i + nbPagesTopLevel.intValue();
//		Double d = Math.pow(nbPagesPerLevel.doubleValue(), nbLevels.doubleValue());
//		d = d + (nbPagesPerLevel * nbLevels);
		
		Double nbPages = new Double(0);
		for (double d = nbLevels; d > 0; d--) {
			nbPages += Math.pow(nbPagesPerLevel.doubleValue(), d);
		}
		nbPages = nbPages * nbPagesTopLevel + nbPagesTopLevel;

		return new Integer(nbPages.intValue());
	}
}
