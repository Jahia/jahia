package org.jahia.tools.contentgenerator.properties;

public final class ContentGeneratorCst {

	/**
	 * Properties name
	 */
	public static final String DB_HOST_PROPERTY = "mysql_in_Host";
	public static final String DB_PORT_PROPERTY = "mysql_in_Port";
	public static final String DB_LOGIN_PROPERTY = "mysql_in_Login";
	public static final String DB_PASSWORD_PROPERTY = "mysql_in_Password";
	public static final String DB_DATABASE_PROPERTY = "mysql_in_Database";
	public static final String DB_TABLE_PROPERTY = "mysql_in_Table";

	public static final String NB_PAGES_TOP_LEVEL = "jahia_cg_nbPagesOnTopLevel";
	public static final String NB_SUB_LEVELS = "jahia_cg_nbSubLevels";
	public static final String NB_SUBPAGES_PER_PAGE = "jahia_cg_nbPagesPerLevel";

	public static final String OUTPUT_DIR_PROPERTY = "jaha_cg_outputDir";
	public static final String OUTPUT_FILE_PROPERTY = "jaha_cg_outputFilename";
	

	/**
	 * Default properties
	 */
	public static final Integer NB_PAGES_TOP_LEVEL_DEFAULT = Integer.valueOf(5);
	public static final Integer NB_SUB_LEVELS_DEFAULT = Integer.valueOf(2);
	public static final Integer NB_SUBPAGES_PER_PAGE_DEFAULT = Integer.valueOf(10);
	public static final String OUTPUT_DIR_DEFAULT = "/home/guillaume";
	public static final String OUTPUT_FILE_DEFAULT = "jahiaExport.xml";

	/**
	 * Configuration
	 */
	public static final String PROPERTIES_FILE_NAME = "jahiaContentGenerator.properties";
	public static final Integer SQL_RECORDSET_SIZE = Integer.valueOf(5000);
	public static final Integer MAX_TOTAL_PAGES = Integer.valueOf(100000);

	private ContentGeneratorCst() {

	}
}
