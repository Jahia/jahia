package org.jahia.tools.contentgenerator.properties;


public final class ContentGeneratorCst {

	/**
	 * Configuration
	 */
	public static final String PROPERTIES_FILE_NAME = "jahiaContentGenerator.properties";
	public static final Integer SQL_RECORDSET_SIZE = Integer.valueOf(10000);
	public static final Integer MAX_TOTAL_PAGES = Integer.valueOf(200000);
	
	public static final String VALUE_ALL = "all";
	public static final String VALUE_NONE = "none";
	public static final String VALUE_RANDOM = "random";
	
	/**
	 * Default value
	 */
	public static final String MYSQL_HOST_DEFAULT = "localhost";
	public static final String MYSQL_TABLE_DEFAULT = "articles";
	
	public static final Integer NB_PAGES_TOP_LEVEL_DEFAULT = Integer.valueOf(1);
	public static final Integer NB_SUB_LEVELS_DEFAULT = Integer.valueOf(2);
	public static final Integer NB_SUBPAGES_PER_PAGE_DEFAULT = Integer.valueOf(3);
	public static final String OUTPUT_FILE_DEFAULT = "jahia-cg.output.xml";
	public static final String OUTPUT_MAP_DEFAULT = "jahia-cg.output.csv";
	public static final Boolean HAS_VANITY_DEFAULT = Boolean.TRUE;
	public static final Boolean CREATE_MAP_DEFAULT = Boolean.FALSE;
	public static final String SITE_KEY_DEFAULT = "ACME";
	public static final String ADD_FILE_TO_PAGE_DEFAULT = ContentGeneratorCst.VALUE_NONE;
	
	private ContentGeneratorCst() {

	}
}
