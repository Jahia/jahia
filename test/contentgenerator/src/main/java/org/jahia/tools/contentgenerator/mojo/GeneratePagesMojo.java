package org.jahia.tools.contentgenerator.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jahia.tools.contentgenerator.ContentGeneratorService;
import org.jahia.tools.contentgenerator.bo.ExportBO;

/**
 * @goal generate-pages
 * @author Guillaume Lucazeau
 * 
 */
public class GeneratePagesMojo extends ContentGeneratorMojo {
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ContentGeneratorService contentGeneratorService = new ContentGeneratorService();
		ExportBO export = super.initExport();
		contentGeneratorService.generatePages(export);
	}
}
