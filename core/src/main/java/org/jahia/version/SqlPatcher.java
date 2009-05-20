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
    private static Map<String, String> schemas;
    private DatabaseScripts scriptHelper = new DatabaseScripts();

    static {
        schemas = new HashMap<String, String>();
        schemas.put("jsqlconnect.script", "sqlserver");
        schemas.put("sqlserver_jtds.script", "sqlserver");
        schemas.put("sqlserver_tds.script", "sqlserver");
    }

    public SqlPatcher() {
        dbSchema = org.jahia.settings.SettingsBean.getInstance().getPropertiesFile().getProperty("db_script");
        if (schemas.containsKey(dbSchema)) {
            dbSchema = schemas.get(dbSchema);
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
            return patch.getNumber() == 0 || (patch.getNumber() > lastVersion && patch.getNumber() <= currentVersion);
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
            List<String> stmts = scriptHelper.getScriptFileStatements(patch.getFile());
            int total = stmts.size();
            for (String line : stmts) {
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
