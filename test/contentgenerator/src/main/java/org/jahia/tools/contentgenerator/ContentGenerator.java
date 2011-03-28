package org.jahia.tools.contentgenerator;

import java.util.Properties;

import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;
import org.jahia.tools.contentgenerator.properties.PropertyLoader;

public class ContentGenerator {
	// @TODO: create goal Maven
	// @TODO: default properties
	// @TODO: gerer plusieurs niveaux de pages, configurable
	// @TODO: gerer contenu different pour fr et en
	// @TODO: decouper traitements n / 20,000
	// @TODO: ajouter verification environnement au debut (dossiers etc)

	public static void main(final String[] args) {
		Properties properties = readPropertiesFile();
		ContentGeneratorService contentGenerator = new ContentGeneratorService();
		contentGenerator.execute(properties);
	}

	private static Properties readPropertiesFile() {
		return PropertyLoader.loadProperties(ContentGeneratorCst.PROPERTIES_FILE_NAME);
	}
}
