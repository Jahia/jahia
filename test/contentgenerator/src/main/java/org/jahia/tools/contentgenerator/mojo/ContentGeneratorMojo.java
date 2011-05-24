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
public class ContentGeneratorMojo extends AbstractMojo {
	private static final Logger logger = Logger.getLogger(ContentGeneratorMojo.class.getName());

	/**
	 * @parameter expression="${jahia.cg.mysql.host}"
	 */
	private String mysql_host;

	/**
	 * @parameter expression="${jahia.cg.mysql.login}"
	 */

	private String mysql_login;

	/**
	 * @parameter expression="${jahia.cg.mysql.password}"
	 */
	private String mysql_password;

	/**
	 * @parameter expression="${jahia.cg.mysql_db}"
	 */
	private String mysql_db;

	/**
	 * @parameter expression="${jahia.cg.mysql_table}"
	 */
	private String mysql_table;

	/**
	 * @parameter expression="${jahia.cg.nbPagesOnTopLevel}"
	 */
	private Integer nbPagesOnTopLevel;

	/**
	 * @parameter expression="${jahia.cg.nbSubLevels}"
	 */
	private Integer nbSubLevels;

	/**
	 * @parameter expression="${jahia.cg.nbPagesPerLevel}"
	 */
	private Integer nbPagesPerLevel;

	/**
	 * @parameter expression="${jahia.cg.outputDirectory}"
	 */
	private String outputDirectory;

	/**
	 * @parameter expression="${jahia.cg.outputFileName}"
	 */
	private String outputFileName;

	/**
	 * @parameter expression="${jahia.cg.createMapYn}"
	 */
	private Boolean createMapYn;

	/**
	 * @parameter expression=${jahia.cg.ouputMapName}
	 */
	private String outputMapName;

	/**
	 * @parameter expression="${jahia.cg.pagesHaveVanity}"
	 */
	private Boolean pagesHaveVanity;

	/**
	 * @parameter expression="${jahia.cg.siteKey}"
	 */
	private String siteKey;

	/**
	 * @parameter expression="${jahia.cg.addFiles}"
	 */
	private String addFiles;

	/**
	 * @parameter expression="${jahia.cg.poolDirectory}"
	 */
	private String poolDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ContentGeneratorService contentGeneratorService = new ContentGeneratorService();
		ExportBO export = this.initExport();
		contentGeneratorService.execute(export);
	}

	/**
	 * Get properties and initialize ExportBO
	 * 
	 * @return a new export BO containing all the parameters
	 */
	private ExportBO initExport() throws MojoExecutionException, MojoFailureException {
		ExportBO export = new ExportBO();

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

		if (mysql_password == null) {
			logger.info("No MySQL table name provided, uses default \"article\"");
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
				throw new MojoExecutionException("Output directory \"" + outputDirectory
						+ "\" does not exist or is not readable");
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
					throw new MojoExecutionException(
							"Directory containing files to include does not exist or is not readable: " + poolDirectory);
				}
				export.setFilesDirectory(fPoolDirectory);
				export.setFileNames(getFileNamesAvailable(export.getFilesDirectory()));
			}
		}

		Integer totalPages = getTotalNumberOfPagesNeeded(nbPagesOnTopLevel, nbSubLevels, nbPagesPerLevel);
		if (export.getTotalPages().compareTo(ContentGeneratorCst.MAX_TOTAL_PAGES) > 0) {
			throw new MojoExecutionException("You asked to generate " + export.getTotalPages()
					+ " pages, the maximum allowed is " + ContentGeneratorCst.MAX_TOTAL_PAGES);
		}
		export.setTotalPages(totalPages);

		return export;
	}

	/**
	 * Calculates the number of pages needed, used to know how much articles we
	 * will need
	 * 
	 * @param nbPagesTopLevel
	 * @param nbLevels
	 * @param nbPagesPerLevel
	 * @return number of pages needed
	 */
	private Integer getTotalNumberOfPagesNeeded(Integer nbPagesTopLevel, Integer nbLevels, Integer nbPagesPerLevel) {
		Double nbPages = new Double(0);
		for (double d = nbLevels; d > 0; d--) {
			nbPages += Math.pow(nbPagesPerLevel.doubleValue(), d);
		}
		nbPages = nbPages * nbPagesTopLevel + nbPagesTopLevel;

		return new Integer(nbPages.intValue());
	}

	/**
	 * Returns a list of the files that can be used as attachements
	 * 
	 * @param filesDirectoryPath
	 *            directory containing the files that will be uploaded into the
	 *            Jahia repository and can be used as attachements
	 * @return list of file names
	 */
	private List<String> getFileNamesAvailable(File filesDirectory) {
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
