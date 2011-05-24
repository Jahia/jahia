package org.jahia.tools.contentgenerator;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.ExportBO;

public class ContentGeneratorService {

	private static final Logger logger = Logger.getLogger(ContentGeneratorService.class.getName());

	public void execute(ExportBO export) {
		logger.info("Jahia content generator starts");
		logger.info(export.getTotalPages() + " pages will be created");

		List<ArticleBO> articles = DatabaseService.getInstance().getArticles(export);

		generateExport(export, articles);
		logger.info("Completed, " + export.getTotalPages() + " pages created");
		logger.info("Please remember that your export contains articles randomly picked from Wikipedia data, and the content generator is not responsible for the articles content. Thank you!");
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
}
