package org.jahia.tools.contentgenerator.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jahia.tools.contentgenerator.ContentGeneratorService;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;
import org.jahia.tools.contentgenerator.properties.DatabaseProperties;

/**
 * @goal generate
 * @author Guillaume Lucazeau
 * 
 */
public abstract class ContentGeneratorMojo extends AbstractMojo {
	protected static final Logger logger = Logger.getLogger(ContentGeneratorMojo.class.getName());

	/**
	 * @parameter expression="${jahia.cg.mysql.host}"
	 */
	protected String mysql_host;

	/**
	private static final Logger logger = Logger.getLogger(GeneratePagesMojo.class.getName());
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
	 * @parameter expression="${jahia.cg.mysql_table}"
	 */
	protected String mysql_table;

	/**
	 * @parameter expression="${jahia.cg.nbPagesOnTopLevel}"
	 */
	protected Integer nbPagesOnTopLevel;

	/**
	 * @parameter expression="${jahia.cg.nbSubLevels}"
	 */
	protected Integer nbSubLevels;

	/**
	 * @parameter expression="${jahia.cg.nbPagesPerLevel}"
	 */
	protected Integer nbPagesPerLevel;

	/**
	 * @parameter expression="${jahia.cg.outputDirectory}" default-value="output"
	 */
	protected String outputDirectory;

	/**
	 * @parameter expression="${jahia.cg.outputFileName}" default-value="jahia-cg-output.xml"
	 */
	protected String outputFileName;

	/**
	 * @parameter expression="${jahia.cg.createMapYn}"
	 */
	protected Boolean createMapYn;

	/**
	 * @parameter expression=${jahia.cg.ouputMapName}
	 */
	protected String outputMapName;

	/**
	 * @parameter expression="${jahia.cg.pagesHaveVanity}"
	 */
	protected Boolean pagesHaveVanity;

	/**
	 * @parameter expression="${jahia.cg.siteKey}"
	 */
	protected String siteKey;

	/**
	 * @parameter expression="${jahia.cg.addFiles}"
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

	@Override
	public abstract void execute() throws MojoExecutionException, MojoFailureException;
	
	/**
	 * Get properties and initialize ExportBO
	 * 
	 * @return a new export BO containing all the parameters
	 */
	protected ExportBO initExport() throws MojoExecutionException, MojoFailureException {
		ExportBO export = new ExportBO();
		ContentGeneratorService contentGeneratorService = ContentGeneratorService.getInstance();

		/**
		 * Database
		 */
		if (mysql_host == null) {
			DatabaseProperties.HOSTNAME = ContentGeneratorCst.MYSQL_HOST_DEFAULT;
		} else {
			DatabaseProperties.HOSTNAME = mysql_host;
		}

		if (mysql_db == null) {
			throw new MojoExecutionException("No database name provided");
		} else {
			DatabaseProperties.DATABASE = mysql_db;
		}

		if (mysql_login == null) {
			throw new MojoExecutionException("No database user provided");
		} else {
			DatabaseProperties.USER = mysql_login;
		}

		if (mysql_password == null) {
			throw new MojoExecutionException("No database user password provided");
		} else {
			DatabaseProperties.PASSWORD = mysql_password;
		}

		if (mysql_table == null) {
			logger.info("No MySQL table name provided, uses default \"" + ContentGeneratorCst.MYSQL_TABLE_DEFAULT + "\"");
			DatabaseProperties.TABLE = "articles";
		} else {
			DatabaseProperties.TABLE = mysql_table;
		}

		if (nbPagesOnTopLevel == null) {
			export.setNbPagesTopLevel(ContentGeneratorCst.NB_PAGES_TOP_LEVEL_DEFAULT);
		} else {
			export.setNbPagesTopLevel(nbPagesOnTopLevel);
		}

		if (nbSubLevels == null) {
			export.setNbSubLevels(ContentGeneratorCst.NB_SUB_LEVELS_DEFAULT);
		} else {
			export.setNbSubLevels(nbSubLevels);
		}

		if (nbSubLevels == null) {
			export.setNbSubPagesPerPage(ContentGeneratorCst.NB_SUBPAGES_PER_PAGE_DEFAULT);
		} else {
			export.setNbSubPagesPerPage(nbPagesPerLevel);
		}

		String pathSeparator = System.getProperty("file.separator");
		if (outputDirectory == null) {
			throw new MojoExecutionException("outputDirectory property can not be null");
		} else {
			File fOutputDirectory = new File(outputDirectory);
			if (!fOutputDirectory.exists()) {
				fOutputDirectory.mkdirs();
			}

			if (outputFileName == null) {
				outputFileName = ContentGeneratorCst.OUTPUT_FILE_DEFAULT;
			}
			File outputFile = new File(outputDirectory + pathSeparator + outputFileName);
			export.setOutputFile(outputFile);
		}

		if (createMapYn == null) {
			export.setCreateMap(ContentGeneratorCst.CREATE_MAP_DEFAULT);
		} else {
			export.setCreateMap(createMapYn);
		}

		if (outputMapName == null) {
			outputMapName = ContentGeneratorCst.OUTPUT_MAP_DEFAULT;
		}
		File outputMapFile = new File(outputDirectory + pathSeparator + outputMapName);
		export.setMapFile(outputMapFile);

		if (pagesHaveVanity == null) {
			export.setPagesHaveVanity(ContentGeneratorCst.HAS_VANITY_DEFAULT);
		} else {
			export.setPagesHaveVanity(pagesHaveVanity);
		}

		if (siteKey == null) {
			export.setSiteKey(ContentGeneratorCst.SITE_KEY_DEFAULT);
		} else {
			export.setSiteKey(siteKey);
		}

		if (addFiles == null) {
			export.setAddFilesToPage(ContentGeneratorCst.ADD_FILE_TO_PAGE_DEFAULT);
		} else {
			export.setAddFilesToPage(addFiles);
		}

		if (ContentGeneratorCst.VALUE_ALL.equals(export.getAddFilesToPage())
				|| ContentGeneratorCst.VALUE_RANDOM.equals(export.getAddFilesToPage())) {
			if (poolDirectory == null) {
				throw new MojoExecutionException("Pool directory property can not be null");
			} else {
				File fPoolDirectory = new File(poolDirectory);
				if (!fPoolDirectory.exists()) {
                    fPoolDirectory.mkdirs();
				}
				export.setFilesDirectory(fPoolDirectory);
				export.setFileNames(getFileNamesAvailable(export.getFilesDirectory()));
			}
		}
		
		export.setNumberOfFilesToGenerate(numberOfFilesToGenerate);
		

		Integer totalPages = contentGeneratorService.getTotalNumberOfPagesNeeded(nbPagesOnTopLevel, nbSubLevels, nbPagesPerLevel);
		export.setTotalPages(totalPages);
		if (export.getTotalPages().compareTo(ContentGeneratorCst.MAX_TOTAL_PAGES) > 0) {
			throw new MojoExecutionException("You asked to generate " + export.getTotalPages()
					+ " pages, the maximum allowed is " + ContentGeneratorCst.MAX_TOTAL_PAGES);
		}

		return export;
	}

	/**
	 * Returns a list of the files that can be used as attachements
	 * 
	 * @param filesDirectoryPath
	 *            directory containing the files that will be uploaded into the
	 *            Jahia repository and can be used as attachements
	 * @return list of file names
	 */
	protected List<String> getFileNamesAvailable(File filesDirectory) {
		List<String> fileNames = new ArrayList<String>();
		File[] files = filesDirectory.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				logger.debug("\"" + files[i].getName() + "\" added to the list of available files.");
				fileNames.add(files[i].getName());
			}
		}
		return fileNames;
	}
}
