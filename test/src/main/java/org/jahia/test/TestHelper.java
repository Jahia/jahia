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
package org.jahia.test;

import org.jahia.admin.database.DatabaseScripts;
import org.jahia.bin.Jahia;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Base64;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 12, 2009
 * Time: 4:49:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestHelper {

    static Logger logger = LoggerFactory.getLogger(TestHelper.class);

    public static JahiaSite createSite(String name) throws Exception {

        ProcessingContext ctx = Jahia.getThreadParamBean();
        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        JahiaSite site = service.getSiteByKey("testSite");

        if (site != null) {
            service.removeSite(site);
        }

        site = service.addSite(admin, name, "localhost", name, name, null,
                ctx.getLocale(), "Jahia TCK templates (Jahia Test Compatibility Kit)", "noImport", null, null, false, false, ctx);
        ctx.setSite(site);

        return site;
    }

    public static void deleteSite(String name) throws Exception {
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        service.removeSite(service.getSiteByKey(name));
    }

    public static void cleanDatabase() throws Exception {
        createDBTables();
        ServicesRegistry.getInstance().getCacheService().flushAllCaches();
    }

    /**
     * Insert the database tables described in the database script. Before the
     * insertion, since you're sure that the user want overwrite his database,
     * each table is dropped, table after table.
     *
     * @throws Exception an exception occured during the process.
     */
    private static void createDBTables() throws Exception {
        File object;
        List sqlStatements;
        String line;
        final DatabaseScripts scripts = new DatabaseScripts();
        logger.info("Creating database tables...");

// construct script path...
        final StringBuffer script = new StringBuffer().append(Jahia.getThreadParamBean().settings().getJahiaDatabaseScriptsPath());
        script.append(File.separator);
        final DataSource bean = (DataSource) SpringContextSingleton.getInstance().getContext().getBean("dataSource");
        final Connection db = bean.getConnection();
        script.append(File.separator+db.getMetaData().getDatabaseProductName().toLowerCase()+".script");

// get script runtime...
        try {
            object = new File(script.toString());
            sqlStatements = scripts.getSchemaSQL(object);
        } catch (Exception e) {
            logger.error("Jahia can't read the appropriate database script." + e);
            throw e;
        }

// drop each tables (if present) and (re-)create it after...
        final Statement statement = db.createStatement();
        for (Object sqlStatement : sqlStatements) {
            line = (String) sqlStatement;
            final String lowerCaseLine = line.toLowerCase();
            final int tableNamePos = lowerCaseLine.indexOf("create table");
            if (tableNamePos != -1) {
                final String tableName = line.substring("create table".length() +
                        tableNamePos,
                        line.indexOf("(")).trim();
                logger.debug("Creating table [" + tableName + "] ...");
                try {
                    statement.execute("DROP TABLE " + tableName);
                } catch (Throwable t) {
                    // ignore because if this fails it's ok
                    logger.debug("Drop failed on " + tableName + " but that's acceptable...");
                }
            }
            try {
                statement.execute(line);
                logger.debug("Executed sql : " + line);
            } catch (Exception e) {
                // first let's check if it is a DROP TABLE query, if it is,
                // we will just fail silently.
                String upperCaseLine = line.toUpperCase().trim();
                if (!upperCaseLine.startsWith("DROP") && !upperCaseLine.startsWith("ALTER TABLE")
                        && !upperCaseLine.startsWith("CREATE INDEX") && !upperCaseLine.startsWith("DELETE FROM")) {
                    logger.debug("Error while trying to execute query : " + line + " from script " + script.toString() + e);
// continue to propagate the exception upwards.
                    throw e;
                } else if (upperCaseLine.startsWith("CREATE INDEX")) {
                    logger.debug("Error while trying to execute query : " + line + e);
                }
            }
        }
        statement.close();
        insertDBCustomContent(db);
    }

    private static String encryptPassword (String password) {
        if (password == null) {
            return null;
        }

        if (password.length () == 0) {
            return null;
        }

        String result = null;

        try {
            MessageDigest md = MessageDigest.getInstance ("SHA-1");
            if (md != null) {
                md.reset ();
                md.update (password.getBytes ());
                result = new String (Base64.encodeBase64 (md.digest ()));
            }
        } catch (NoSuchAlgorithmException ex) {

            result = null;
        }

        return result;
    }

    /**
     * Insert database custom data, like root user and properties.
     *
     * @throws Exception an exception occured during the process.
     */
    private static void insertDBCustomContent(Connection con) throws Exception {


// get two keys...
        final String rootName = "root";
        final int siteID0 = 0;
        final String rootKey = rootName + ":" + siteID0;
        final String grpKey0 = "administrators:" + siteID0;
// query insert root user...
        queryPreparedStatement(con,"INSERT INTO jahia_users(id_jahia_users, name_jahia_users, password_jahia_users, key_jahia_users) VALUES(0,?,?,?)",
                new Object[] { rootName, encryptPassword("root1234"), rootKey } );

// query insert root first name...
        queryPreparedStatement(con,"INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'firstname', ?, 'jahia',?)",
                new Object[] { "", rootKey } );

// query insert root last name...
        queryPreparedStatement(con,"INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'lastname', ?, 'jahia',?)",
                new Object[] { "", rootKey } );

// query insert root e-mail address...
        queryPreparedStatement(con,"INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'email', ?, 'jahia',?)",
                new Object[] {"", rootKey } );

// query insert administrators group...
        queryPreparedStatement(con,"INSERT INTO jahia_grps(id_jahia_grps, name_jahia_grps, key_jahia_grps, siteid_jahia_grps) VALUES(?,?,?,null)",
                new Object[] {siteID0, "administrators", grpKey0 } );

// query insert administrators group access...
        queryPreparedStatement(con,"INSERT INTO jahia_grp_access(id_jahia_member, id_jahia_grps, membertype_grp_access) VALUES(?,?,1)",
                new Object[] { rootKey,grpKey0 } );

// create guest user
        queryPreparedStatement(con,"INSERT INTO jahia_users(id_jahia_users, name_jahia_users, password_jahia_users, key_jahia_users) VALUES(1,?,?,?)",
                new Object[] {"guest", "*", "guest:0" } );

        queryPreparedStatement(con,"INSERT INTO jahia_version(install_number, build, release_number, install_date) VALUES(0, ?,?,?)",
                new Object[] { new Integer("12346789"), "132456" + "." + "123456", new Timestamp(System.currentTimeMillis()) } );


    }

    private static void queryPreparedStatement(Connection theConnection,String sqlCode, Object[] params)
        throws Exception {
        PreparedStatement ps = theConnection.prepareStatement(sqlCode);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i+1,params[i]);
        }
        ps.execute();
        ps.close();
    } // end query
}
