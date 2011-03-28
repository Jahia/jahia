package org.jahia.tools.contentgenerator;

public class ContentGeneratorCst {

	public static final String PROPERTIES_FILENAME = "jahiaContentGenerator";

	public static final String DB_HOST_PROPERTY = "mysql_in_Host";
	public static final String DB_PORT_PROPERTY = "mysql_in_Port";
	public static final String DB_LOGIN_PROPERTY = "mysql_in_Login";
	public static final String DB_PASSWORD_PROPERTY = "mysql_in_Password";
	public static final String DB_DATABASE_PROPERTY = "mysql_in_Database";
	public static final String DB_TABLE_PROPERTY = "mysql_in_Table";

	public static final String NB_RECORDS_PROPERTY = "jahia_cg_nbRecords";
	public static final String NB_PAGES_TOP_LEVEL = "jahia_cg_nbPagesOnTopLevel";
	public static final String NB_LEVELS = "jahia_cg_nbLevels";
	public static final String NB_PAGES_PER_LEVEL = "jahia_cg_nbPagesPerLevel";
	
	public static final String OUTPUT_DIR_PROPERTY = "jaha_cg_outputDir";
	
	public static final Integer SQL_RECORDSET_SIZE = new Integer(5000);
	private ContentGeneratorCst() {
		
	}
}
