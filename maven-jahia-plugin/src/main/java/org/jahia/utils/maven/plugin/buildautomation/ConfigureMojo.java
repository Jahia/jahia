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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils.maven.plugin.buildautomation;

import org.jahia.utils.maven.plugin.AbstractManagementMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 * User: islam
 * Date: Jul 28, 2008
 * Time: 3:40:32 PM
 * To change this template use File | Settings | File Templates.
 *
 * @goal configure
 * @requiresDependencyResolution runtime
 * @requiresProject false
 */
public class ConfigureMojo extends AbstractManagementMojo

{
    /**
     * @parameter expression="${jahia.configure.active}" default-value="false"
     * activates the configure goal
     */
    protected boolean active;

    // Now for the build automation parameters

    /**
     * @parameter   expression="${jahia.configure.deploymentMode}"
     * can be clustered or standalone
     */
    protected String deploymentMode;


    //The following are jahia.properties values that are not present in the skeleton but that
    //do not require to be changed so we set them here but they can easyly be changed

    /**
     * properties file path
     *
     * @parameter  default-value="$context/WEB-INF/var/content/filemanager/"
     *
     */
    protected String jahiaFileRepositoryDiskPath;

    /**
     * properties file path
     *
     * @parameter  default-value="Testing_release"
     */
    protected String release;
    /**
     * properties file path
     *
     * @parameter  default-value="Tomcat"
     */
    protected String  server ;
    /**
     * properties file path
     *
     * @parameter  default-value="${jahia.configure.localIp}"
     */
    protected String    localIp        ;
    /**
     * properties file path
     *
     * @parameter  default-value="$context/WEB-INF/etc/"
     */
    protected String   jahiaEtcDiskPath   ;
    /**
     * properties file path
     *
     * @parameter  default-value="$context/WEB-INF/var/"
     */
    protected String   jahiaVarDiskPath    ;
    /**
     * properties file path
     *
     * @parameter     default-value="$context/WEB-INF/var/new_templates/"
     */
    protected String   jahiaNewTemplatesDiskPath;
    /**
     * properties file path
     *
     * @parameter  default-value="$context/WEB-INF/var/new_webapps/"
     */
    protected String jahiaNewWebAppsDiskPath ;
    /**
     * properties file path
     *
     * @parameter   default-value="$context/WEB-INF/var/shared_templates/"
     */
    protected String jahiaSharedTemplatesDiskPath    ;
    /**
     * properties file path
     *
     * @parameter default-value="$webContext/jsp/jahia/templates/"
     */
    protected String jahiaTemplatesHttpPath   ;
    /**
     * properties file path
     *
     * @parameter     default-value="$webContext/jsp/jahia/engines/"
     */
    protected String jahiaEnginesHttpPath   ;
    /**
     * properties file path
     *
     * @parameter default-value="$webContext/jsp/jahia/javascript/jahia.js"
     */
    protected String jahiaJavaScriptHttpPath ;
    /**
     * properties file path
     *
     * @parameter   default-value="http\\://localhost\\:8080/manager"
     */
    protected String  jahiaWebAppsDeployerBaseURL   ;
    /**
     * properties file path
     *
     * @parameter    default-value="java\\:comp/env/jdbc/jahia"
     */
    protected String  datasource_name  ;
    /**
     * properties file path
     *
     * @parameter  default-value="false"
     */
    protected String  outputCacheActivated   ;
    /**
     * properties file path
     *
     * @parameter   default-value="-1"
     */
    protected String  outputCacheDefaultExpirationDelay     ;
    /**
     * properties file path
     *
     * @parameter   default-value="false"
     */
    protected String outputCacheExpirationOnly    ;
    /**
     * properties file path
     *
     * @parameter  default-value="true"
     */
    protected String  outputContainerCacheActivated   ;
    /**
     * properties file path
     *
     * @parameter     default-value="14400"
     */
    protected String containerCacheDefaultExpirationDelay  ;
    /**
     * properties file path
     *
     * @parameter      default-value="false"
     */
    protected String containerCacheLiveModeOnly  ;
    /**
     * properties file path
     *
     * @parameter     default-value="false"
     */
    protected String  esiCacheActivated     ;
    /**
     * properties file path
     *
     * @parameter default-value="org.jahia.services.webapps_deployer.JahiaTomcatWebAppsDeployerBaseService"
     */
    protected String Jahia_WebApps_Deployer_Service  ;
    /**
     * properties file path
     *
     * @parameter    default-value="mySite"
     */
    protected String defautSite;
    /**
     * properties file path
     *
     * @parameter   default-value="false"
     */
    protected String cluster_activated ;
    /**
     * properties file path
     *
     * @parameter  expression="${jahia.configure.cluster_node_serverId}" default-value="Jahia1"
     */
    protected String cluster_node_serverId;
    /**
     * properties jahiaRootPassword
     *
     * @parameter  expression="${jahia.configure.jahiaRootPassword}" default-value="root1234"
     */
    protected String jahiaRootPassword;
    /**
     * properties file path
     *
     * @parameter   expression="${jahia.deploy.processingServer}"
     */
    protected String  processingServer;
    /**
     * properties file path
     *
     * @parameter  default-value="DBJahiaText"
     */
    protected String  bigtext_service   ;
    /**
     * properties file path
     *
     * @parameter  default-value="$context/WEB-INF/var/templates/"
     */
    protected String jahiaFilesTemplatesDiskPath;
    /**
     * properties file path
     *
     * @parameter  default-value="$context/WEB-INF/var/imports/"
     */
    protected String jahiaImportsDiskPath;

    /**
     * properties file path
     *
     * @parameter  default-value="$context/WEB-INF/var/content/bigtext/"
     */
    protected String jahiaFilesBigTextDiskPath;

    /**
     * List of nodes in the cluster.
     *
     * @parameter
     */
    protected List clusterNodes;

    /**
     * Database type used
     *
     * @parameter expression="${jahia.configure.databaseType}"
     */
    protected String databaseType;

    /**
     * URL to connect to the database
     *
     * @parameter expression="${jahia.configure.databaseUrl}"
     */
    protected String databaseUrl;

    /**
     * List of nodes in the cluster.
     *
     * @parameter expression="${jahia.configure.databaseUsername}"
     */
    protected String databaseUsername;

    /**
     * List of nodes in the cluster.
     *
     * @parameter expression="${jahia.configure.databasePassword}"
     */
    protected String databasePassword;


    /**
     * List of nodes in the cluster.
     *
     * @parameter
     */
    protected List  siteImportLocation;


    /**
     * My Properties.
     *
     * @parameter
     */
    private Properties myProperties;

    /**
     * List of nodes in the cluster.
     *
     * @parameter expression="${jahia.configure.overwritedb}"  default-value="true"
     *
     * This property is here for instance when we are in a clustered mode we don not want the database scripts to be
     * executed for every node
     *
     */
    protected String   overwritedb;

    /**
     * properties db_starthsqlserver
     *
     * @parameter  default-value="true"
     */
    protected String db_starthsqlserver;

    /**
     * properties db_starthsqlserver
     *
     * @parameter  default-value="true"
     */
    protected String developmentMode;

     /**
     * properties storeFilesInDB
     *
     * @parameter  default-value="true"
     */
    protected String storeFilesInDB;


    JahiaPropertiesBean jahiaPropertiesBean;
    DatabaseConnection db = new DatabaseConnection();
    File webappDir;
    Properties dbProps;
    File databaseScript;
    JahiaPropertiesConfigurator jahiaPropertiesConfigurator;
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        if (active) {
            setProperties();
            if(cluster_activated.equals("true")){
                deployOnCluster();
            }else{
                getLog().info("Deployed in standalone for server in "+webappDir);
            }
            jahiaPropertiesConfigurator.generateJahiaProperties();
        }
    }

    private void deployOnCluster() throws MojoExecutionException, MojoFailureException {
        getLog().info(" Deploying in cluster for server in "+webappDir);
        jahiaPropertiesBean.setClusterNodes(clusterNodes);
        jahiaPropertiesBean.setProcessingServer(processingServer);
    }
    private void cleanDatabase() {
        //if it is a mysql, try to drop the database and create a new one  your user must have full rights on this database
        int begindatabasename=databaseUrl.indexOf("/",13);
        int enddatabaseName=databaseUrl.indexOf("?",begindatabasename);
        String databaseName= "`" + databaseUrl.substring(begindatabasename+1,enddatabaseName) + "`";  //because of the last '/' we added +1
        try {

            db.query("drop  database if exists "+databaseName);
            db.query("create database "+databaseName);
            db.query("alter database "+databaseName+" charset utf8");
        } catch (Throwable t) {
            // ignore because if this fails it's ok
            getLog().info("error in " + databaseName + " because of"+t);
        }
    }

    private void updateConfigurationFiles(String webappPath, Properties dbProps) {
        SpringManagerConfigurator.updateDataSourceConfiguration(webappPath + "/WEB-INF/etc/spring/applicationcontext-manager.xml", dbProps);
        SpringHibernateConfigurator.updateDataSourceConfiguration(webappPath + "/WEB-INF/etc/spring/applicationcontext-hibernate.xml", dbProps);
        QuartzConfigurator.updateDataSourceConfiguration(webappPath + "/WEB-INF/etc/config/quartz.properties", dbProps);
        getLog().info(" value of files storage :"+storeFilesInDB);
        JackrabbitConfigurator.updateDataSourceConfiguration(webappPath + "/WEB-INF/etc/repository/jackrabbit/repository.xml", dbProps, jahiaPropertiesBean.getCluster_activated(), jahiaPropertiesBean.getCluster_node_serverId());
        JahiaXmlConfigurator.updateDataSourceConfiguration(webappPath + "/META-INF/context.xml", dbProps, databaseUsername, databasePassword, databaseUrl);

        if(cluster_activated.equals("true")){
            IndexationPolicyConfigurator.updateDataSourceConfigurationForCluster(webappPath + "/WEB-INF/etc/spring/applicationcontext-indexationpolicy.xml");
        }
    }

    private void setProperties() {

        //create the bean that will contain all the necessary information for the building of the Jahia.properties file
        jahiaPropertiesBean= new JahiaPropertiesBean();
        jahiaPropertiesBean.setBigtext_service(bigtext_service);
        jahiaPropertiesBean.setCluster_activated(cluster_activated);
        jahiaPropertiesBean.setCluster_node_serverId(cluster_node_serverId);
        jahiaPropertiesBean.setClusterNodes(null);
        jahiaPropertiesBean.setContainerCacheDefaultExpirationDelay(containerCacheDefaultExpirationDelay);
        jahiaPropertiesBean.setContainerCacheLiveModeOnly(containerCacheLiveModeOnly);
        jahiaPropertiesBean.setDatasource_name(datasource_name);
        jahiaPropertiesBean.setDefautSite(defautSite);
        jahiaPropertiesBean.setEsiCacheActivated(esiCacheActivated);
        jahiaPropertiesBean.setJahia_WebApps_Deployer_Service(getJahia_WebApps_Deployer_Service());
        jahiaPropertiesBean.setJahiaEnginesHttpPath(jahiaEnginesHttpPath);
        jahiaPropertiesBean.setJahiaEtcDiskPath(jahiaEtcDiskPath);
        jahiaPropertiesBean.setJahiaFileRepositoryDiskPath(jahiaFileRepositoryDiskPath);
        jahiaPropertiesBean.setJahiaFilesBigTextDiskPath(jahiaFilesBigTextDiskPath);
        jahiaPropertiesBean.setJahiaFilesTemplatesDiskPath(jahiaFilesTemplatesDiskPath);
        jahiaPropertiesBean.setJahiaImportsDiskPath(jahiaImportsDiskPath);
        jahiaPropertiesBean.setJahiaJavaScriptHttpPath(jahiaJavaScriptHttpPath);
        jahiaPropertiesBean.setJahiaNewTemplatesDiskPath(jahiaNewTemplatesDiskPath);
        jahiaPropertiesBean.setJahiaNewWebAppsDiskPath(jahiaNewWebAppsDiskPath);
        jahiaPropertiesBean.setJahiaSharedTemplatesDiskPath(jahiaSharedTemplatesDiskPath);
        jahiaPropertiesBean.setJahiaTemplatesHttpPath(jahiaTemplatesHttpPath);
        jahiaPropertiesBean.setJahiaVarDiskPath(jahiaVarDiskPath);
        jahiaPropertiesBean.setJahiaWebAppsDeployerBaseURL(jahiaWebAppsDeployerBaseURL);
        jahiaPropertiesBean.setLocalIp(localIp);
        jahiaPropertiesBean.setOutputCacheActivated(outputCacheActivated);
        jahiaPropertiesBean.setOutputCacheDefaultExpirationDelay(outputCacheDefaultExpirationDelay);
        jahiaPropertiesBean.setOutputCacheExpirationOnly(outputCacheExpirationOnly);
        jahiaPropertiesBean.setOutputContainerCacheActivated(outputContainerCacheActivated);
        jahiaPropertiesBean.setOutputContainerCacheActivated(outputContainerCacheActivated);
        jahiaPropertiesBean.setProcessingServer(processingServer);
        jahiaPropertiesBean.setRelease(release);
        jahiaPropertiesBean.setServer(server);
        jahiaPropertiesBean.setLocalIp(localIp);
        jahiaPropertiesBean.setDb_script(databaseType + ".script");
        jahiaPropertiesBean.setDevelopmentMode(developmentMode);

        //now set the common properties to both a clustered environment and a standalone one
        webappDir = getWebappDeploymentDir();
        dbProps = new Properties();
        //database script always ends with a .script
        databaseScript = new File(webappDir + "/WEB-INF/var/db/" + databaseType + ".script");
        try {
            dbProps.load(new FileInputStream(databaseScript));
            dbProps.put("storeFilesInDB",storeFilesInDB);
        } catch (IOException e) {
            getLog().error("Error in loading database settings because of "+e);
        }
        if(!databaseType.equals("hypersonic")){
            db_starthsqlserver = "false";
            jahiaPropertiesBean.setDb_StartHsqlServer(db_starthsqlserver);
            //create dbdata folder as this one is created by hypersonic at launch

        }
        jahiaPropertiesConfigurator = new JahiaPropertiesConfigurator(webappDir.getPath(), jahiaPropertiesBean);

        getLog().info("updating Jackrabbit, Spring and Quartz configuration files");

        //updates jackrabbit, quartz and spring files
        updateConfigurationFiles(webappDir.getPath(), dbProps);
        getLog().info("creating database tables and copying license file");
        try {
            copyLicense(webappDir.getPath() + "/WEB-INF/etc/config/licenses/license-free.xml", webappDir.getPath() + "/WEB-INF/etc/config/license.xml");
            if(overwritedb.equals("true")){
                if (!databaseScript.exists()) {
                    getLog().info("cannot find script in "+databaseScript.getPath());
                    throw new MojoExecutionException("Cannot find script for database "+databaseType);
                }
                db.databaseOpen(dbProps.getProperty("jahia.database.driver"), databaseUrl, databaseUsername, databasePassword);
                if(databaseType.equals("mysql")){
                    getLog().info("database is mysql trying to drop it and create a new one");
                    cleanDatabase();
                    //you have to reopen the database connection as before you just dropped the database
                    db.databaseOpen(dbProps.getProperty("jahia.database.driver"), databaseUrl, databaseUsername, databasePassword);
                }
                createDBTables(databaseScript);
                insertDBCustomContent();
            }

            deleteTomcatFiles();
            if(siteImportLocation!=null){
                getLog().info("copying site Export to tomcat's import location from  to "+webappDir + "/WEB-INF/var/imports");
                importSites();
            }else{
                getLog().info("no site import found ");
            }

        } catch (Exception e) {
            getLog().error("exception in setting the properties because of "+e);
        }
    }

    private void importSites(){
        for (int i= 0;i<siteImportLocation.size();i++){
            try {
                copy((String)siteImportLocation.get(i), webappDir + "/WEB-INF/var/imports");
            } catch (IOException e) {
                getLog().error("error in copying sitImport file "+e);
            }
        }
    }


    private void deleteTomcatFiles(){

        boolean delete1 =deleteDir(new File(targetServerDirectory+"/temp"));
        boolean delete2 =deleteDir(new File(targetServerDirectory+"/work"));
        boolean delete3 = deleteDir(new File(webappDir + "/WEB-INF/var/repository"));
        if(delete1&&delete2&&delete3){
            getLog().info("finished deleting tomcat /temp /work and /var/repository files");
            (new File(targetServerDirectory+"/temp")).mkdir();
            (new File(targetServerDirectory+"/work")).mkdir();
            (new File(webappDir + "/WEB-INF/var/repository")).mkdir();
        }
    }

    private  boolean deleteDir(File dir) {

        //There is only one file in var/repository that we do not want to delete which is  indexing_configuration.xml
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //getLog().info("name is "+dir.getName());
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    getLog().info("no sucess in deleting"+children[i]);
                    return false;
                }
            }


        }

        return dir.delete();

    }

    //copy method for the licence for instance
    private  void copyLicense(String fromFileName, String toFileName)
            throws IOException {
        File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);

        if (!fromFile.exists())
            throw new IOException("FileCopy: " + "no such source file: "
                    + fromFileName);
        if (!fromFile.isFile())
            throw new IOException("FileCopy: " + "can't copy directory: "
                    + fromFileName);
        if (!fromFile.canRead())
            throw new IOException("FileCopy: " + "source file is unreadable: "
                    + fromFileName);

        if (toFile.isDirectory()){
            toFile = new File(toFile, fromFile.getName());

        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1)
                to.write(buffer, 0, bytesRead); // write
        } finally {
            if (from != null)
                try {
                    from.close();
                } catch (IOException e) {
                }
            if (to != null)
                try {
                    to.close();
                } catch (IOException e) {
                }
        }
    }


    private void copy(String fromFileName, String toFileName)
            throws IOException {
        File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);

        if (!fromFile.exists())
            throw new IOException("FileCopy: " + "no such source file: "
                    + fromFileName);
        if (!fromFile.isFile())
            throw new IOException("FileCopy: " + "can't copy directory: "
                    + fromFileName);
        if (!fromFile.canRead())
            throw new IOException("FileCopy: " + "source file is unreadable: "
                    + fromFileName);
        toFile.mkdir();
        if (toFile.isDirectory()){
            toFile = new File(toFile, fromFile.getName());

        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1)
                to.write(buffer, 0, bytesRead); // write
        } finally {
            if (from != null)
                try {
                    from.close();
                } catch (IOException e) {
                }
            if (to != null)
                try {
                    to.close();
                } catch (IOException e) {
                }
        }
    }
    private void createDBTables(File dbScript) throws Exception {

        List sqlStatements;
        String line;

        DatabaseScripts scripts = new DatabaseScripts();

// get script runtime...
        try {
            sqlStatements = scripts.getSchemaSQL(dbScript);
        } catch (Exception e) {
            throw e;
        }
// drop each tables (if present) and (re-)create it after...
        final Iterator sqlIter = sqlStatements.iterator();
        while (sqlIter.hasNext()) {
            line = (String) sqlIter.next();
            final String lowerCaseLine = line.toLowerCase();
            final int tableNamePos = lowerCaseLine.indexOf("create table");
            if (tableNamePos != -1) {
                final String tableName = line.substring("create table".length() +
                        tableNamePos,
                        line.indexOf("(")).trim();
//getLog().info("Creating table [" + tableName + "] ...");
                try {
                    db.query("DROP TABLE " + tableName);
                } catch (Throwable t) {
                    // ignore because if this fails it's ok
                    getLog().debug("Drop failed on " + tableName + " because of "+t+" but that's acceptable...");
                }
            }
            try {
                db.query(line);
            } catch (Exception e) {
                // first let's check if it is a DROP TABLE query, if it is,
                // we will just fail silently.

                String upperCaseLine = line.toUpperCase().trim();
                if (!upperCaseLine.startsWith("DROP") && !upperCaseLine.startsWith("ALTER TABLE")
                        && !upperCaseLine.startsWith("CREATE INDEX")) {
                    getLog().error("Error while trying to execute query : " + line, e);
// continue to propagate the exception upwards.
                    throw e;
                } else if(upperCaseLine.startsWith("CREATE INDEX")){
                    getLog().warn("Error while trying to execute query : " + line, e);
                }
            }
        }


    }
// end createDBTables()


    /**
     * Insert database custom data, like root user and properties.
     */
    private void insertDBCustomContent() throws Exception {

        getLog().debug("Inserting customized settings into database...");

// get two keys...
        final String rootName = "root";

        final String password =encryptPassword(jahiaRootPassword);   //root1234
        getLog().info("Encrypted root password for jahia is " +jahiaRootPassword);
        final int siteID0 = 0;
        final String rootKey = rootName + ":" + siteID0;
        final String grpKey0 = "administrators" + ":" + siteID0;

// query insert root user...
        db.queryPreparedStatement("INSERT INTO jahia_users(id_jahia_users, name_jahia_users, password_jahia_users, key_jahia_users) VALUES(0,?,?,?)",
                new Object[] { rootName, password, rootKey } );

// query insert root first name...
        db.queryPreparedStatement("INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'firstname', ?, 'jahia',?)",
                new Object[] { "root", rootKey } );

// query insert root last name...
        db.queryPreparedStatement("INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'lastname', ?, 'jahia',?)",
                new Object[] { "", rootKey } );

// query insert root e-mail address...
        db.queryPreparedStatement("INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'email', ?, 'jahia',?)",
                new Object[] { (String) "", rootKey } );

// query insert administrators group...
        db.queryPreparedStatement("INSERT INTO jahia_grps(id_jahia_grps, name_jahia_grps, key_jahia_grps, siteid_jahia_grps) VALUES(?,?,?,null)",
                new Object[] { new Integer(siteID0), "administrators", grpKey0 } );

// query insert administrators group access...
        db.queryPreparedStatement("INSERT INTO jahia_grp_access(id_jahia_member, id_jahia_grps, membertype_grp_access) VALUES(?,?,1)",
                new Object[] { rootKey,grpKey0 } );

// create guest user
        db.queryPreparedStatement("INSERT INTO jahia_users(id_jahia_users, name_jahia_users, password_jahia_users, key_jahia_users) VALUES(1,?,?,?)",
                new Object[] {"guest", "*", "guest:0" } );

//db.queryPreparedStatement("INSERT INTO jahia_version(install_number, build, release_number, install_date) VALUES(0, ?,?,?)",
//new Object[] { new Integer(Jahia.getBuildNumber()), Jahia.getReleaseNumber() + "." + Jahia.getPatchNumber(), new Timestamp(System.currentTimeMillis()) } );
    }
// end insertDBCustomContent()


    public String encryptPassword (String password) {
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
            md = null;
        } catch (NoSuchAlgorithmException ex) {

            result = null;
        }

        return result;
    }

    public String getJahia_WebApps_Deployer_Service() {
        return Jahia_WebApps_Deployer_Service;
    }

    public void setJahia_WebApps_Deployer_Service(String jahia_WebApps_Deployer_Service) {
        Jahia_WebApps_Deployer_Service = jahia_WebApps_Deployer_Service;
    }

}
