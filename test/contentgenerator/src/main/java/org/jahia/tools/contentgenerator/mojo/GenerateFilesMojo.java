package org.jahia.tools.contentgenerator.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jahia.tools.contentgenerator.ContentGeneratorService;
import org.jahia.tools.contentgenerator.bo.ExportBO;

/**
 * Generates files that can be used as attachments
 * @goal generate-files
 */
public class GenerateFilesMojo extends ContentGeneratorMojo
{
    public void execute() throws MojoExecutionException, MojoFailureException
    {
		ContentGeneratorService contentGeneratorService = new ContentGeneratorService();
		ExportBO export = super.initExport();
		contentGeneratorService.generateFiles(export);
    }
}
