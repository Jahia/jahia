package org.jahia.tools.contentgenerator.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.jahia.tools.contentgenerator.ContentGeneratorService;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;

/**
 * Displays help
 * 
 * @goal help
 */
public class HelpMojo extends AbstractMojo {

	public void execute() throws MojoExecutionException {
		getLog().info("Jahia 6.5 content generator");
		getLog().info(" ");
		getLog().info("Goals:");
		getLog().info("jahia-cg:help");
		getLog().info("jahia-cg:generate-pages");
		getLog().info("jahia-cg:generate-site");
		getLog().info("jahia-cg:generate-files");
		getLog().info("");
		
		getLog().info("Parameters:");
		getLog().info("* jahia.cg.mysql.host (optional, default = " + ContentGeneratorCst.MYSQL_HOST_DEFAULT + ")");
		getLog().info("* jahia.cg.mysql.login");
		getLog().info("* jahia.cg.mysql.password");
		getLog().info("* jahia.cg.mysql_db");
		getLog().info("* jahia.cg.mysql_table (optional, default = " + ContentGeneratorCst.MYSQL_TABLE_DEFAULT + ")");
		getLog().info(
				"* jahia.cg.nbPagesOnTopLevel (optional, default = " + ContentGeneratorCst.NB_PAGES_TOP_LEVEL_DEFAULT
						+ ")");
		getLog().info("* jahia.cg.nbSubLevels (optional, default = " + ContentGeneratorCst.NB_SUB_LEVELS_DEFAULT + ")");
		getLog().info(
				"* jahia.cg.nbPagesPerLevel (optional, default = " + ContentGeneratorCst.NB_SUBPAGES_PER_PAGE_DEFAULT
						+ ")");
		getLog().info("* jahia.cg.outputDirectory");
		getLog().info("* jahia.cg.outputFileName (optional, default = " + ContentGeneratorCst.OUTPUT_FILE_DEFAULT + ")");

		getLog().info("* jahia.cg.createMapYn (optional, default = " + ContentGeneratorCst.CREATE_MAP_DEFAULT + ")");
		getLog().info(
				"  if true, the tool will generate another file containing the absolute path of each page, one per line");

		getLog().info("* jahia.cg.ouputMapName (optional, default = " + ContentGeneratorCst.OUTPUT_MAP_DEFAULT + ")");
		getLog().info("* jahia.cg.pagesHaveVanity (optional, default = " + ContentGeneratorCst.HAS_VANITY_DEFAULT + ")");
		getLog().info("  if true, add a vanity URL to each page, on the template \"/page<n>\"");

		getLog().info("* jahia.cg.siteKey (optional, default = " + ContentGeneratorCst.SITE_KEY_DEFAULT + ")");

		getLog().info(
				"* jahia.cg.addFiles (optional, default = " + ContentGeneratorCst.ADD_FILE_TO_PAGE_DEFAULT
						+ ", other values:" + ContentGeneratorCst.VALUE_ALL + ", " + ContentGeneratorCst.VALUE_RANDOM
						+ ")");
		getLog().info(
				"  Add a <publication> bloc to the generated pages. The content generator will pick all the files available from the pool directory, at least once.");
		getLog().info("  If there is less files than required, it will process the files list from the beginning.");

		getLog().info("* jahia.cg.poolDirectory (required if jahia.cg.addFiles != " + ContentGeneratorCst.VALUE_NONE + ")");
		getLog().info("* jahia.cg.numberOfFilesToGenerate required for file generation. Files will be generated in poolDirectory path.");
		getLog().info("* jahia.cg.numberOfFilesToGenerate required for file generation. Files will be generated in poolDirectory path.");
		getLog().info("* jahia.cg.numberOfBigTextPerPage (optional, default = 1)");
		getLog().info("* jahia.cg.numberOfUsers: generate user1 to user${numberOfUsers}. Password is \"password\"");
		getLog().info("* jahia.cg.numberOfGroups: generate group1 to user${numberOfGroups}. First groups contain one more user is some users can not make a full group. ");
		
		getLog().info("");
		getLog().info("NB:");
		getLog().info("1. You can not generate more than " + ContentGeneratorCst.MAX_TOTAL_PAGES + " pages");
		getLog().info("2. The content generator will select " + ContentGeneratorCst.SQL_RECORDSET_SIZE + " articles maximum, and reuse them if you asked for more pages");
		
		//getLog().info(" ");
		//getLog().info("With your current configuration Jahia-cg will generate " + ContentGeneratorService.getInstance().getTotalNumberOfPagesNeeded(nbPagesTopLevel, nbLevels, nbPagesPerLevel) + " pages");
		
		getLog().info(" ");
		getLog().info("Generated content with default values:");
		getLog().info("one-top-page");
		getLog().info("   |-- sub-page-1");
		getLog().info("   |   |-- sub-page-1-1");
		getLog().info("   |   |-- sub-page-1-2");
		getLog().info("   |   |-- sub-page-1-3");
		getLog().info("   |-- sub-page-2");
		getLog().info("   |   |-- sub-page-2-1");
		getLog().info("   |   |-- sub-page-2-2");
		getLog().info("   |   |-- sub-page-2-3");
		getLog().info("   |-- sub-page-3");
		getLog().info("       |-- sub-page-3-1");
		getLog().info("       |-- sub-page-3-2");
		getLog().info("       |-- sub-page-3-3");
	}
}
