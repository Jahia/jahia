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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.jahia.clipbuilder.sql.bean.QueryBean;
import org.jahia.clipbuilder.sql.config.DbProps;
import org.jahia.clipbuilder.sql.database.hibernate.model.DatabaseConfiguration;
import org.jahia.clipbuilder.sql.util.DeployUtilities;

/**
 *  Description of the Class
 *
 *@author    ktlili
 */
public class EditQueryAction extends BaseAction {
	/**
	 *  Description of the Field
	 */
	public static String DATABASE_MYSQL = "mysql";
	/**
	 *  Description of the Field
	 */
	public static String DATABASE_ORACLE = "oracle";
	/**
	 *  Description of the Field
	 */
	public static String DATABASE_POSTGRE = "postgre";
	/**
	 *  Description of the Field
	 */
	public static String DATABASE_HSQL = "hsql";



	/**
	 *  Gets the KeyMethodMap attribute of the EditQueryAction object
	 *
	 *@return    The KeyMethodMap value
	 */
	public Map getKeyMethodMap() {
		Map map = new HashMap();
		map.put("init", "init");
		map.put("sql.button.executeQuery", "executeQuery");
		map.put("sql.button.executeQueryForLink", "executeQuery");
		map.put("sql.button.deploy", "deploy");
		map.put("sql.button.saveAsDefault", "saveOrUpdate");
		map.put("menu.back", "backToMenuBuilder");
		return map;
	}


	/**
	 *  Gets the ClippersDirectoryPath attribute of the EditQueryAction object
	 *
	 *@param  request  Description of Parameter
	 *@return          The ClippersDirectoryPath value
	 */
	public String getClippersDirectoryPath(HttpServletRequest request) {
        String path = getServlet().getServletContext().getRealPath("/");
        if ( !path.endsWith(File.separator) ){
            path+=File.separator;
        }
        path += getResources(request).getMessage("clippers.repository.path");
		return path;
	}


	/**
	 *  Gets the ClippersDirectoryPathDeploy attribute of the ManageAction object
	 *
	 *@param  request  Description of Parameter
	 *@return          The ClippersDirectoryPathDeploy value
	 */
	public String getClippersDirectoryPathDeploy(HttpServletRequest request) {
		String path = org.jahia.clipbuilder.util.JahiaUtils.getPortletDiretcoryPath(request);
		if (path == null) {
			// clip builder is not part of jahia
			path = getResources(request).getMessage("clippers.repository.deploy.path");
		}
		return path;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping  Description of Parameter
	 *@param  actionForm     Description of Parameter
	 *@param  request        Description of Parameter
	 *@param  response       Description of Parameter
	 *@return                Description of the Returned Value
	 */
	public ActionForward executeQuery(ActionMapping actionMapping,
			ActionForm actionForm,
			HttpServletRequest request,
			HttpServletResponse response) {
		EditQueryForm form = (EditQueryForm) actionForm;
		HttpSession session = request.getSession();
		// get params
		String title = form.getTitle();
		String database = form.getDatabase();
		String databaseName = form.getDatabaseName();
		String databaseUrl = form.getDatabaseUrl();
		String userName = form.getUserName();
		String password = form.getUserPassword();
		String sql = form.getSqlQuery();
		String tableSize = form.getTableSize();

		//check table size value
		try {
			int tableSizeInt = Integer.parseInt(tableSize);
			if (tableSizeInt < 1) {
				throw new Exception("Table size not positive.");
			}
		}
		catch (Exception ex) {
			log.error("Table size error");
			ActionMessages messages = new ActionMessages();
			messages.add("error", new ActionMessage("error.sql.Numberformat", ex.toString()));
			saveMessages(request, messages);
			return actionMapping.getInputForward();
		}

		String driverAndUrl[] = buildDriverAndUrl(database, databaseName, databaseUrl);

		//set properties
		DbProps.DRIVER = driverAndUrl[0];
		DbProps.URL = driverAndUrl[1];
		DbProps.PASSWORD = password;
		DbProps.USERNAME = userName;

		try {
			log.info("Executing Query method.");

			if (log.isDebugEnabled()) {
				log.debug("Execute query method. sql : " + sql);
			}

			//If current query's results have not been cached, do so now.
			QueryBean qb = new QueryBean();
			qb.executeQuery(sql);

			//Get column headings/query results and assign them to request attributes.
			String[] queryColumns = (String[]) qb.getQueryColumns();
			List queryData = (List) qb.getQueryData();
			session.setAttribute("title", title);
			session.setAttribute("queryColumns", queryColumns);
			session.setAttribute("queryData", queryData);
			session.setAttribute("tableSize", tableSize);
			session.setAttribute("resultSet", qb.getResultSet());

			return actionMapping.getInputForward();
		}
		catch (Exception ex) {
			ActionMessages messages = new ActionMessages();
			messages.add("error", new ActionMessage("error.sql", ex.toString()));
			saveMessages(request, messages);
			log.error("Enable to execute query due to: ", ex);
		}

		return actionMapping.getInputForward();
	}


	/**
	 *  Deploy
	 *
	 *@param  actionMapping  Description of Parameter
	 *@param  actionForm     Description of Parameter
	 *@param  request        Description of Parameter
	 *@param  response       Description of Parameter
	 *@return                Description of the Returned Value
	 */
	public ActionForward deploy(ActionMapping actionMapping,
			ActionForm actionForm,
			HttpServletRequest request,
			HttpServletResponse response) {
		EditQueryForm form = (EditQueryForm) actionForm;
		// get params
		String title = form.getTitle();
		String database = form.getDatabase();
		String databaseName = form.getDatabaseName();
		String databaseUrl = form.getDatabaseUrl();
		String userName = form.getUserName();
		String password = form.getUserPassword();
		String sqlQuery = form.getSqlQuery();
		String portletName = form.getPortletname();
		String portletDescription = form.getPortletdescription();

		//generate clipper.xml file
		String sqlPatternDrirectoryPath = getClippersDirectoryPath(request) + File.separator + "sqlpattern" + File.separator + "sqlClipperPortletPattern.war";

		//deploy
		String driverAndUrl[] = buildDriverAndUrl(database, databaseName, databaseUrl);
		String driver = driverAndUrl[0];
		String url = driverAndUrl[1];
		String patternWar = sqlPatternDrirectoryPath;
		String warTarget = getClippersDirectoryPathDeploy(request) + File.separator + "jahia_sql_clip_" + title + ".war";
		try {
			DeployUtilities.getInstance().deploy(patternWar, warTarget, portletName, portletDescription, driver, url, databaseName, userName, password, title, sqlQuery);
            request.setAttribute("deployed",Boolean.TRUE);
        }
		catch (Exception ex) {
			log.error("Error has occured during deploy step: " + ex.getMessage(), ex);
		}

		return actionMapping.getInputForward();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping  Description of Parameter
	 *@param  actionForm     Description of Parameter
	 *@param  request        Description of Parameter
	 *@param  response       Description of Parameter
	 *@return                Description of the Returned Value
	 */
	public ActionForward backToMenuBuilder(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
		return actionMapping.findForward("menuBuilder");
	}



	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping  Description of Parameter
	 *@param  actionForm     Description of Parameter
	 *@param  request        Description of Parameter
	 *@param  response       Description of Parameter
	 *@return                Description of the Returned Value
	 */
	public ActionForward init(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
		EditQueryForm form = (EditQueryForm) actionForm;

		//fill form from database
		DatabaseConfiguration dConf = getDefaultConfigurationManager().
				getDefaultConfiguration();
		if (dConf != null) {
			form.setDatabase(dConf.getDatabaseType());
			form.setDatabaseName(dConf.getDatabaseName());
			form.setDatabaseUrl(dConf.getDatabaseUrl());
			form.setUserName(dConf.getUserName());
			form.setUserPassword(dConf.getUserPassword());
		}
		else {

			//message: no default configuration
			ActionMessages messages = new ActionMessages();
			messages.add("error", new ActionMessage("sql.messages.database.empty"));

			saveMessages(request, messages);
		}
		return actionMapping.getInputForward();
	}



	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping  Description of Parameter
	 *@param  actionForm     Description of Parameter
	 *@param  request        Description of Parameter
	 *@param  response       Description of Parameter
	 *@return                Description of the Returned Value
	 */
	public ActionForward saveOrUpdate(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
		EditQueryForm form = (EditQueryForm) actionForm;

		//fill from form
		DatabaseConfiguration dConf = getDefaultConfigurationManager().getDefaultConfiguration();
		if (dConf == null) {
			dConf = new DatabaseConfiguration();
		}
		dConf.setDatabaseType(form.getDatabase());
		dConf.setDatabaseName(form.getDatabaseName());
		dConf.setDatabaseUrl(form.getDatabaseUrl());
		dConf.setUserName(form.getUserName());
		dConf.setUserPassword(form.getUserPassword());

		// save or update
		getDefaultConfigurationManager().saveOrUpdateDefaultConfiguration(dConf);

		return actionMapping.getInputForward();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  database      Description of Parameter
	 *@param  databaseName  Description of Parameter
	 *@param  databaseUrl   Description of Parameter
	 *@return               Description of the Returned Value
	 */
	private String[] buildDriverAndUrl(String database, String databaseName,
			String databaseUrl) {
		// SQL
		String driver = null;
		String url = null;
		if (database.equalsIgnoreCase(DATABASE_MYSQL)) {
			driver = "com.mysql.jdbc.Driver";
			url = "jdbc:mysql://" + databaseUrl + "/" + databaseName;
		}
		else if (database.equalsIgnoreCase(DATABASE_ORACLE)) {
			driver = "oracle.jdbc.driver.OracleDriver";
			url = "jdbc:oracle:thin:@" + databaseUrl + ":" + databaseName;
		}
		else if (database.equalsIgnoreCase(DATABASE_POSTGRE)) {
			driver = "org.postgresql.Driver";
			url = "jdbc:postgresql://" + databaseUrl + "/" + databaseName;
		}
		else if (database.equalsIgnoreCase(DATABASE_HSQL)) {
			driver = "org.hsqldb.jdbcDriver";
			url = "jdbc:hsqldb:" + databaseUrl + "/" + databaseName;
		}

		String[] result = {driver, url};
		return result;
	}

}
