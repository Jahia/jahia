package org.jahia.tools.contentgenerator.mojo;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jahia.tools.contentgenerator.ContentGeneratorService;
import org.jahia.tools.contentgenerator.FileService;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;
import org.jahia.tools.contentgenerator.properties.DatabaseProperties;

/**
 * @goal generate
 * @author Guillaume Lucazeau
 * 
 */
public abstract class ContentGeneratorMojo extends AbstractMojo {

	/**
	 * @parameter expression="${jahia.cg.mysql.host}" default-value="localhost"
	 */
	protected String mysql_host;

	/**
	 * @parameter expression="${jahia.cg.mysql.login}"
	 */

	protected String mysql_login;

	/**
	 * @parameter expression="${jahia.cg.mysql.password}"
	 */
	protected String mysql_password;

	/**
	 * @parameter expression="${jahia.cg.mysql_db}"
	 */
	protected String mysql_db;

	/**
	 * @parameter expression="${jahia.cg.mysql_table}" default-value="articles"
	 */
	protected String mysql_table;

	/**
	 * @parameter expression="${jahia.cg.nbPagesOnTopLevel}" default-value="1"
	 */
	protected Integer nbPagesOnTopLevel;

	/**
	 * @parameter expression="${jahia.cg.nbSubLevels}" default-value="2"
	 */
	protected Integer nbSubLevels;

	/**
	 * @parameter expression="${jahia.cg.nbPagesPerLevel}" default-value="3"
	 */
	protected Integer nbPagesPerLevel;

	/**
	 * @parameter expression="${jahia.cg.outputDirectory}"
	 *            default-value="output"
	 */
	protected String outputDirectory;

	/**
	 * @parameter expression="${jahia.cg.outputFileName}"
	 *            default-value="jahia-cg-output.xml"
	 */
	protected String outputFileName;

	/**
	 * @parameter expression="${jahia.cg.createMapYn}" default-value="false"
	 */
	protected Boolean createMapYn;

	/**
	 * @parameter expression="${jahia.cg.ouputMapName}" default-value="jahia-cg.output.csv"
	 */
	protected String outputMapName;

	/**
	 * @parameter expression="${jahia.cg.pagesHaveVanity}" default-value="true"
	 */
	protected Boolean pagesHaveVanity;

	/**
	 * @parameter expression="${jahia.cg.siteKey}" default-value="testSite"
	 */
	protected String siteKey;

	/**
	 * @parameter expression="${jahia.cg.siteLanguages}" default-value="en,fr"
	 */
	protected String siteLanguages;

	/**
	 * @parameter expression="${jahia.cg.addFiles}" default-value="none"
	 */
	protected String addFiles;

	/**
	 * @parameter expression="${jahia.cg.poolDirectory}"
	 */
	protected String poolDirectory;

	/**
	 * @parameter expression="${jahia.cg.numberOfFilesToGenerate}"
	 */
	protected Integer numberOfFilesToGenerate;

	/**
	 * @parameter expression="${jahia.cg.numberOfBigTextPerPage}"
	 *            default-value="1"
	 */
	protected Integer numberOfBigTextPerPage;

	/**
	 * @parameter expression="${jahia.cg.numberOfUsers}" default-value="25"
	 */
	protected Integer numberOfUsers;

	/**
	 * @parameter expression="${jahia.cg.numberOfGroups}" default-value="5"
	 */
	protected Integer numberOfGroups;

    /**
     * @parameter expression="${jahia.cg.groupsAclRatio}" defaule-value="0"
     */
    protected double groupAclRatio;

    /**
     * @parameter expression="${jahia.cg.usersAclRatio}" defaule-value="0"
     */
    protected double usersAclRatio;

	public abstract void execute() throws MojoExecutionException, MojoFailureException;

	/**
	 * Get properties and initialize ExportBO
	 * 
	 * @return a new export BO containing all the parameters
	 */
	protected ExportBO initExport() throws MojoExecutionException {
		ExportBO export = new ExportBO();
		ContentGeneratorService contentGeneratorService = ContentGeneratorService.getInstance();

		/**
		 * Database
		 */
        DatabaseProperties.HOSTNAME = mysql_host;

		if (mysql_db == null) {
			throw new MojoExecutionException("No database name provided");
		}
		DatabaseProperties.DATABASE = mysql_db;

		if (mysql_login == null) {
			throw new MojoExecutionException("No database user provided");
		}
		DatabaseProperties.USER = mysql_login;

		if (mysql_password == null) {
			throw new MojoExecutionException("No database user password provided");
		}
		DatabaseProperties.PASSWORD = mysql_password;

		if (mysql_table == null) {
			getLog().info(
					"No MySQL table name provided, uses default \"" + ContentGeneratorCst.MYSQL_TABLE_DEFAULT + "\"");
			DatabaseProperties.TABLE = "articles";
		} else {
			DatabaseProperties.TABLE = mysql_table;
		}

        export.setNbPagesTopLevel(nbPagesOnTopLevel);
        export.setNbSubLevels(nbSubLevels);
        export.setNbSubPagesPerPage(nbPagesPerLevel);

		String pathSeparator = System.getProperty("file.separator");
		if (outputDirectory == null) {
			throw new MojoExecutionException("outputDirectory property can not be null");
		}
		File fOutputDirectory = new File(outputDirectory);
		if (!fOutputDirectory.exists()) {
			fOutputDirectory.mkdirs();
		}

		File outputFile = new File(outputDirectory + pathSeparator + outputFileName);
		export.setOutputFile(outputFile);
		export.setOutputDir(outputDirectory);

        export.setCreateMap(createMapYn);

		File outputMapFile = new File(outputDirectory + pathSeparator + outputMapName);
		export.setMapFile(outputMapFile);
        export.setPagesHaveVanity(pagesHaveVanity);
        export.setSiteKey(siteKey);
        export.setSiteLanguages(Arrays.asList(siteLanguages.split(",")));
		export.setAddFilesToPage(addFiles);

		if (ContentGeneratorCst.VALUE_ALL.equals(export.getAddFilesToPage())
				|| ContentGeneratorCst.VALUE_RANDOM.equals(export.getAddFilesToPage())) {
			if (poolDirectory == null) {
				throw new MojoExecutionException("Pool directory property can not be null");
			}
			File fPoolDirectory = new File(poolDirectory);
			if (!fPoolDirectory.exists()) {
				fPoolDirectory.mkdirs();
			}
			export.setFilesDirectory(fPoolDirectory);

			FileService fileService = new FileService();
			List<String> filesNamesAvailable = fileService.getFileNamesAvailable(export.getFilesDirectory());
			export.setFileNames(filesNamesAvailable);

		}

        export.setNumberOfBigTextPerPage(numberOfBigTextPerPage);
        export.setNumberOfUsers(numberOfUsers);
        export.setNumberOfGroups(numberOfGroups);
		export.setNumberOfFilesToGenerate(numberOfFilesToGenerate);
        export.setGroupAclRatio(groupAclRatio);
        export.setUsersAclRatio(usersAclRatio);

		Integer totalPages = contentGeneratorService.getTotalNumberOfPagesNeeded(nbPagesOnTopLevel, nbSubLevels,
				nbPagesPerLevel);
		export.setTotalPages(totalPages);
		if (export.getTotalPages().compareTo(ContentGeneratorCst.MAX_TOTAL_PAGES) > 0) {
			throw new MojoExecutionException("You asked to generate " + export.getTotalPages()
					+ " pages, the maximum allowed is " + ContentGeneratorCst.MAX_TOTAL_PAGES);
		}

		return export;
	}
}
