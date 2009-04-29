/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.clipbuilder.sql.struts;

import org.apache.struts.action.*;

/**
 *  Struts form
 *
 *@author    ktlili
 */
public class EditQueryForm extends ActionForm {
	private String title;
	private String database;
	private String databaseName;
	private String databaseUrl;
	private String userName;
	private String userPassword;
	private String sqlQuery;
	private String tableSize;
	private String portletname;
	private String portletdescription;


	/**
	 *  Sets the Database attribute of the EditQueryForm object
	 *
	 *@param  database  The new Database value
	 */
	public void setDatabase(String database) {
		this.database = database;
	}


	/**
	 *  Sets the SqlQuery attribute of the EditQueryForm object
	 *
	 *@param  sqlQuery  The new SqlQuery value
	 */
	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}


	/**
	 *  Sets the UserName attribute of the EditQueryForm object
	 *
	 *@param  userName  The new UserName value
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}


	/**
	 *  Sets the UserPassword attribute of the EditQueryForm object
	 *
	 *@param  userPassword  The new UserPassword value
	 */
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}


	/**
	 *  Sets the DatabaseName attribute of the EditQueryForm object
	 *
	 *@param  databaseName  The new DatabaseName value
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}


	/**
	 *  Sets the DatabaseUrl attribute of the EditQueryForm object
	 *
	 *@param  databaseUrl  The new DatabaseUrl value
	 */
	public void setDatabaseUrl(String databaseUrl) {

		this.databaseUrl = databaseUrl;

	}


	/**
	 *  Sets the Title attribute of the EditQueryForm object
	 *
	 *@param  title  The new Title value
	 */
	public void setTitle(String title) {
		this.title = title;
	}


	/**
	 *  Sets the Portletdescription attribute of the EditQueryForm object
	 *
	 *@param  portletdescription  The new Portletdescription value
	 */
	public void setPortletdescription(String portletdescription) {
		this.portletdescription = portletdescription;
	}


	/**
	 *  Sets the Portletname attribute of the EditQueryForm object
	 *
	 *@param  portletname  The new Portletname value
	 */
	public void setPortletname(String portletname) {
		this.portletname = portletname;
	}


	/**
	 *  Sets the TableSize attribute of the EditQueryForm object
	 *
	 *@param  tableSize  The new TableSize value
	 */
	public void setTableSize(String tableSize) {
		this.tableSize = tableSize;
	}


	/**
	 *  Gets the Database attribute of the EditQueryForm object
	 *
	 *@return    The Database value
	 */
	public String getDatabase() {
		if (isEmpty(database)) {
			database = EditQueryAction.DATABASE_MYSQL;
		}
		return database;
	}


	/**
	 *  Gets the SqlQuery attribute of the EditQueryForm object
	 *
	 *@return    The SqlQuery value
	 */
	public String getSqlQuery() {
		return sqlQuery;
	}


	/**
	 *  Gets the UserName attribute of the EditQueryForm object
	 *
	 *@return    The UserName value
	 */
	public String getUserName() {
		return userName;
	}


	/**
	 *  Gets the UserPassword attribute of the EditQueryForm object
	 *
	 *@return    The UserPassword value
	 */
	public String getUserPassword() {
		return userPassword;
	}


	/**
	 *  Gets the DatabaseName attribute of the EditQueryForm object
	 *
	 *@return    The DatabaseName value
	 */
	public String getDatabaseName() {
		if (isEmpty(databaseName)) {
			databaseName = "example";
		}
		return databaseName;
	}


	/**
	 *  Gets the DatabaseUrl attribute of the EditQueryForm object
	 *
	 *@return    The DatabaseUrl value
	 */
	public String getDatabaseUrl() {
		if (isEmpty(databaseUrl)) {
			databaseUrl = "localhost:3306";
		}
		return databaseUrl;
	}


	/**
	 *  Gets the Title attribute of the EditQueryForm object
	 *
	 *@return    The Title value
	 */
	public String getTitle() {
		return title;
	}


	/**
	 *  Gets the Portletdescription attribute of the EditQueryForm object
	 *
	 *@return    The Portletdescription value
	 */
	public String getPortletdescription() {
		return portletdescription;
	}


	/**
	 *  Gets the Portletname attribute of the EditQueryForm object
	 *
	 *@return    The Portletname value
	 */
	public String getPortletname() {
		return portletname;
	}


	/**
	 *  Gets the TableSize attribute of the EditQueryForm object
	 *
	 *@return    The TableSize value
	 */
	public String getTableSize() {
		return tableSize;
	}


	/**
	 *  Gets the Empty attribute of the EditQueryForm object
	 *
	 *@param  value  Description of Parameter
	 *@return        The Empty value
	 */
	private boolean isEmpty(String value) {
		return value == null || value.equalsIgnoreCase("");
	}

}
