package org.jahia.tools.contentgenerator.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jahia.tools.contentgenerator.ContentGeneratorService;
import org.jahia.tools.contentgenerator.bo.ExportBO;

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

		String zipFilePath = contentGeneratorService.generateSite(export);

		getLog().info("Site archive created and available here: " + zipFilePath);
	}
}
