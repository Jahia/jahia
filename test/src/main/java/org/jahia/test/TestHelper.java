/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
import org.jahia.exceptions.JahiaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Base64;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
    public static final String TCK_TEMPLATES = "Jahia Test";
    public static final String ACME_TEMPLATES = "Web templates";

    public static JahiaSite createSite(String name) throws Exception {
        return createSite(name, "localhost"+System.currentTimeMillis(), TCK_TEMPLATES, null);
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet, File importFile)
            throws Exception {

        ProcessingContext ctx = Jahia.getThreadParamBean();
        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        JahiaSite site = service.getSiteByKey(name);

        if (site != null) {
            service.removeSite(site);
        }

        site = service.addSite(admin, name, serverName, name, name, null, ctx.getLocale(), templateSet,
                               importFile == null ? "noImport" : "fileImport", importFile, null, false, false, ctx);

        ctx.setSite(site);

        return site;
    }

    public static JahiaSite createPrepackagedSite(String name,
            String serverName, String templateSet, String prepackedZIPFile,
            String siteZIPName) throws Exception {
        JahiaSite site = null;
        File tempFile = null;
        try {
            tempFile = extractSiteImportZip(prepackedZIPFile, siteZIPName);
            site = TestHelper.createSite(name, serverName, templateSet,
                    tempFile);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        return site;
    }

    private static File extractSiteImportZip(String prepackagedZIPFile,
            String siteZIPName) {
        File siteZIPFile = null;
        ZipInputStream zis = null;
        OutputStream os = null;
        try {
            zis = new ZipInputStream(new FileInputStream(new File(
                    prepackagedZIPFile)));
            ZipEntry z = null;
            while ((z = zis.getNextEntry()) != null) {
                if (siteZIPName.equalsIgnoreCase(z.getName())) {
                    siteZIPFile = File.createTempFile("import", ".zip");
                    os = new FileOutputStream(siteZIPFile);
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = zis.read(buf)) > 0) {
                        os.write(buf, 0, r);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return siteZIPFile;
    }

    
    public static void removeAllSites(JahiaSitesService service) throws JahiaException {
        final Iterator<JahiaSite> sites = service.getSites();
        while (sites.hasNext()) {
            JahiaSite jahiaSite = sites.next();
            service.removeSite(jahiaSite);
        }
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
        List<String> sqlStatements;
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
