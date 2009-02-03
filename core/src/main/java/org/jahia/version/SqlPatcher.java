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

package org.jahia.version;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jahia.admin.database.DatabaseScripts;
import org.jahia.hibernate.manager.JahiaVersionManager;
import org.jahia.hibernate.manager.SpringContextSingleton;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 29 août 2007
 * Time: 10:44:44
 * To change this template use File | Settings | File Templates.
 */
public class SqlPatcher implements Patcher {
    private Logger logger = Logger.getLogger(SqlPatcher.class);
    private String dbSchema;
    private static Map schemas;
    private DatabaseScripts scriptHelper = new DatabaseScripts();

    static {
        schemas = new HashMap();
        schemas.put("jsqlconnect.script", "sqlserver");
        schemas.put("sqlserver_jtds.script", "sqlserver");
        schemas.put("sqlserver_tds.script", "sqlserver");
    }

    public SqlPatcher() {
        dbSchema = org.jahia.settings.SettingsBean.getInstance().getPropertiesFile().getProperty("db_script");
        if (schemas.containsKey(dbSchema)) {
            dbSchema = (String) schemas.get(dbSchema);
        } else {
            dbSchema = dbSchema.substring(0, dbSchema.indexOf('.'));
        }
    }

    public boolean canHandlePatch(Patch patch, int lastVersion, int currentVersion) {
        if ("sql".equals(patch.getExt())) {
            String db = patch.getFile().getParentFile().getName();
            if (!db.equals(dbSchema)) {
                return false;
            }
            return patch.getNumber() > lastVersion && patch.getNumber() < currentVersion;
        }
        return false;
    }

    public int execute(Patch patch) {
        VersionService.getInstance().setSubStatus("org.jahia.admin.patchmanagement.sql");
        JahiaVersionManager versionMgr = (JahiaVersionManager) SpringContextSingleton
                .getInstance().getContext().getBean(
                        JahiaVersionManager.class.getName());
        try {
            int count = 0;
            List stmts = scriptHelper.getScriptFileStatements(patch.getFile());
            int total = stmts.size();
            for (Iterator iterator = stmts.iterator(); iterator.hasNext();) {
                String line = (String) iterator.next();
                try {
                    versionMgr.executeSqlStmt(line);
                } catch (Exception e) {
                    // first let's check if it is a DROP TABLE query, if it is,
                    // we will just fail silently.
                    String upperCaseLine = line.toUpperCase().trim();
                    if (!upperCaseLine.startsWith("DROP") && !upperCaseLine.startsWith("ALTER TABLE")
                        && !upperCaseLine.startsWith("CREATE INDEX")) {
                        logger.error("Error while trying to execute query : " + line + " from script " + patch.getName(), e);
                        // continue to propagate the exception upwards.
                        return 1;
                    } else if(upperCaseLine.startsWith("CREATE INDEX")){
                        logger.warn("Error while trying to execute query : " + line, e);
                    }
                }
                count++;
                VersionService.getInstance().setPercentCompleted(((double)count*100)/(double)total);
            }

        } catch (Exception e) {
            logger.error("Error when executing SQL script " + patch.getName(),e);
            return 2;
        } 
        return 0;
    }
   

}
