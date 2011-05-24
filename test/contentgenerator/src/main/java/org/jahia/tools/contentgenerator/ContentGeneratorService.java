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

	private static final Logger logger = Logger.getLogger(ContentGeneratorService.class.getName());

	public void execute(ExportBO export) {
		long start = System.currentTimeMillis();
		logger.info("Jahia content generator starts");

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
}
