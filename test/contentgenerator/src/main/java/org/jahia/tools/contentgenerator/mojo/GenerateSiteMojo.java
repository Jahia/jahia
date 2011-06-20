package org.jahia.tools.contentgenerator.mojo;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jahia.tools.contentgenerator.ContentGeneratorService;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.w3c.dom.DOMException;

/**
 * @goal generate-site
 * @author Guillaume Lucazeau
 * 
 */
public class GenerateSiteMojo extends ContentGeneratorMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ContentGeneratorService contentGeneratorService = ContentGeneratorService.getInstance();
		ExportBO export = super.initExport();

		getLog().info("Jahia content generator starts");
		getLog().info(export.getSiteKey() + " site will be created");

		String zipFilePath = null;
		try {
			zipFilePath = contentGeneratorService.generateSite(export);
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		getLog().info("Site archive created and available here: " + zipFilePath);
	}
}
