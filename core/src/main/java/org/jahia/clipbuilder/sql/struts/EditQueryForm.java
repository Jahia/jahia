/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
