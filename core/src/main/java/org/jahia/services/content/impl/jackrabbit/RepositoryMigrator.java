/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.impl.jackrabbit;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.JahiaRepositoryCopier;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.apache.jackrabbit.core.RepositoryCopier;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.DatabaseUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility tool to automatically migrate an existing Jackrabbit repository from BLOB storage to a DataStore one, if required.
 *
 * @author Sergiy Shyrkov
 */
public class RepositoryMigrator {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryMigrator.class);

    private static Document readConfig(File cfgFile) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder(false);
        saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);
        return saxBuilder.build(cfgFile);
    }

    @SuppressWarnings("unchecked")
    private static void removeCopyPrefix(File sourceConfigFile, File targetConfigFile)
            throws JDOMException, IOException {
        Document doc = readConfig(sourceConfigFile);

        Element repo = doc.getRootElement();

        // change back table prefix
        for (Element param : (List<Element>) XPath.selectNodes(repo,
                "//param[@name=\"schemaObjectPrefix\"]")) {
            String prefix = param.getAttributeValue("value");
            if (prefix.startsWith("COPY_")) {
                param.setAttribute("value", StringUtils.substringAfter(prefix, "COPY_"));
            }
        }

        writeToFile(doc, targetConfigFile);

        logger.info("Removed 'COPY_' prefix in file {} and stored result in {}", sourceConfigFile,
                targetConfigFile);
    }

    @SuppressWarnings("unchecked")
    private static void removeElements(Element root, String xpath) throws JDOMException {
        for (Element param : (List<Element>) XPath.selectNodes(root, xpath)) {
            param.getParent().removeContent(param);
        }

    }

    private static void writeToFile(Document doc, File file) throws IOException {
        Format customFormat = Format.getPrettyFormat();
        customFormat.setLineSeparator(System.getProperty("line.separator"));
        XMLOutputter xmlOutputter = new XMLOutputter(customFormat);
        FileWriter out = null;
        try {
            out = new FileWriter(file);
            xmlOutputter.output(doc, out);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private File configFile;

    private boolean keepBackup;

    private boolean performMigrationToDataStoreIfNeeded;

    private File repoHome;

    private File scriptsDir;

    private SettingsBean settingsBean;

    private File targetConfigFile;

    RepositoryMigrator(File configFile, File repoHome, File targetConfigFile,
            boolean performMigrationToDataStoreIfNeeded) {
        super();
        this.configFile = configFile;
        this.repoHome = repoHome;
        this.targetConfigFile = targetConfigFile;
        this.performMigrationToDataStoreIfNeeded = performMigrationToDataStoreIfNeeded;
    }

    @SuppressWarnings("unchecked")
    private File createTempConfigFile() throws JDOMException, IOException {
        Document doc = readConfig(targetConfigFile != null ? targetConfigFile : configFile);

        Element repo = doc.getRootElement();
        Namespace ns = repo.getNamespace();

        // prefix table names
        for (Element param : (List<Element>) XPath.selectNodes(repo,
                "//param[@name=\"schemaObjectPrefix\"]")) {
            param.setAttribute("value", "COPY_" + param.getAttributeValue("value"));
        }

        if (targetConfigFile == null) {
            // we are migrating from 6.5 BLOB store

            // do we use DB store for binaries?
            Element param = (Element) XPath.selectSingleNode(repo,
                    "//param[@name=\"externalBLOBs\"]");
            boolean useDatabaseDataStore = param != null
                    && !Boolean.valueOf(param.getAttributeValue("value"));

            // remove externalBLOBs params
            removeElements(repo, "//param[@name=\"externalBLOBs\"]");

            // remove supportHighlighting param from global SearchIndex element
            removeElements(repo, "/Repository/SearchIndex/param[@name=\"supportHighlighting\"]");

            if (XPath.selectSingleNode(repo,
                    "/Repository/SearchIndex/param[@name=\"indexingConfiguration\"]") == null) {
                // add separate indexing configuration for global index
                Element searchIndex = (Element) XPath.selectSingleNode(repo,
                        "/Repository/SearchIndex");
                if (searchIndex != null) {
                    searchIndex.addContent(new Element("param", ns).setAttribute("name",
                            "indexingConfiguration").setAttribute("value",
                            "${rep.home}/indexing_configuration_version.xml"));
                }
            }

            if (XPath.selectSingleNode(repo, "/Repository/DataStore") == null) {
                // add datastore element
                Element store = new Element("DataStore", ns);
                if (useDatabaseDataStore) {
                    store.setAttribute("class", "org.apache.jackrabbit.core.data.db.DbDataStore");
                    store.addContent(new Element("param").setAttribute("name", "dataSourceName")
                            .setAttribute("value", "jahiaDS"));
                    store.addContent(new Element("param")
                            .setAttribute("name", "schemaObjectPrefix").setAttribute("value",
                                    "COPY_JR_"));
                    store.addContent(new Element("param")
                            .setAttribute("name", "schemaCheckEnabled").setAttribute("value",
                                    "false"));
                    store.addContent(new Element("param").setAttribute("name", "copyWhenReading")
                            .setAttribute("value", "true"));
                    store.addContent(new Element("param").setAttribute("name", "minRecordLength")
                            .setAttribute("value", "1024"));
                } else {
                    store.setAttribute("class", "org.apache.jackrabbit.core.data.FileDataStore");
                    store.addContent(new Element("param").setAttribute("name", "minRecordLength")
                            .setAttribute("value", "1024"));
                    store.addContent(new Element("param").setAttribute("name", "path")
                            .setAttribute("value", "${rep.home}/datastore"));
                }
                repo.addContent(store);
            }
        }
        File tempConfigFile = new File(configFile.getParentFile(), "repository-migration-temp.xml");

        writeToFile(doc, tempConfigFile);

        logger.info("Created temporary repository configuration for migration at {}",
                tempConfigFile);

        return tempConfigFile;
    }

    private File createTempRepoHome() throws IOException {
        File repoHomeCopy = new File(repoHome.getParentFile(), "repository-migration");
        repoHomeCopy.mkdir();

        File toCopy = new File(repoHome, "indexing_configuration.xml");
        if (toCopy.exists()) {
            FileUtils.copyFileToDirectory(toCopy, repoHomeCopy);
        }

        toCopy = new File(repoHome, "indexing_configuration_version.xml");
        if (toCopy.exists()) {
            FileUtils.copyFileToDirectory(toCopy, repoHomeCopy);
        }

        logger.info("Using folder {} for migrated repository", repoHomeCopy);

        return repoHomeCopy;
    }

    private void dbExecute(String fileName, String description) throws IOException, SQLException {
        DatabaseUtils.executeScript(new FileReader(new File(scriptsDir, fileName)));

        logger.info(description);
    }

    private void dbInitSettings() throws IOException, JDOMException {
        settingsBean = SettingsBean.getInstance();

        scriptsDir = new File(settingsBean.getJahiaDatabaseScriptsPath(), "sql/migration/"
                + getDbType());

        logger.info("Migration SQL scripts will be looked up in folder {}", scriptsDir);
    }

    private String getDbType() throws JDOMException, IOException {
        Document doc = readConfig(configFile);
        Element repo = doc.getRootElement();
        String type = ((Element) XPath.selectSingleNode(repo,
                "/Repository/DataSources/DataSource/param[@name=\"databaseType\"]"))
                .getAttributeValue("value");
        return StringUtils.isEmpty(type) || "default".equals(type) ? "derby" : type;
    }

    void migrate() {
        try {
            JahiaRepositoryConfig sourceCfg = performMigrationToDataStoreIfNeeded ? JahiaRepositoryConfig.create(
                    configFile.toString(), repoHome.toString()) : null;

            if (targetConfigFile != null || performMigrationToDataStoreIfNeeded
                    && sourceCfg.getDataStore() == null) {

                long timer = System.currentTimeMillis();
                if (targetConfigFile != null) {
                    logger.info(
                            "Will perform repository migration using target configuration file {}",
                            targetConfigFile);
                } else {
                    logger.info("Will perform repository migration from BLOB store to DataStore");
                }

                boolean clusterActivated = Boolean.getBoolean("cluster.activated");
                if (clusterActivated) {
                    // deactivate the cluster for the time of migration
                    System.setProperty("cluster.activated", "false");
                    sourceCfg = null; // reload the configuration
                }

                keepBackup = Boolean.getBoolean("jahia.jackrabbit.backupRepositoryByMigration");

                try {
                    dbInitSettings();

                    dbExecute("jackrabbit-migration-1-create-temp-tables.sql",
                            "Temporary DB tables created");

                    File tempRepoHome = createTempRepoHome();
                    File tempConfigFile = createTempConfigFile();

                    if (sourceCfg == null) {
                        sourceCfg = JahiaRepositoryConfig.create(configFile.toString(), repoHome.toString());
                    }

                    JahiaRepositoryConfig targetCfg = JahiaRepositoryConfig.create(tempConfigFile.toString(), tempRepoHome.toString());

                    performMigration(sourceCfg, targetCfg);

                    JCRContentUtils.deleteJackrabbitIndexes(repoHome);
                    JCRContentUtils.deleteJackrabbitIndexes(tempRepoHome);

                    // swap configuration with the target one
                    File target = new File(tempConfigFile.getParentFile(),
                            "repository-migration-target.xml");
                    removeCopyPrefix(tempConfigFile, target);
                    logger.info("Created target repository configuration after migration at {}",
                            target);

                    if (keepBackup) {
                        File configBackup = new File(configFile.getParentFile(),
                                "repository-original.xml");
                        FileUtils.copyFile(configFile, configBackup);
                        logger.info("Backup original configuration to {}", configBackup);
                    }

                    FileUtils.copyFile(target, configFile);
                    logger.info("Replaced original repository.xml with the target one");

                    if (!keepBackup) {
                        FileUtils.deleteQuietly(tempConfigFile);
                        FileUtils.deleteQuietly(target);
                    }

                    File workspaceConfig = new File(tempRepoHome, "workspaces/default/workspace.xml");
                    removeCopyPrefix(workspaceConfig, workspaceConfig);
                    workspaceConfig = new File(tempRepoHome, "workspaces/live/workspace.xml");
                    removeCopyPrefix(workspaceConfig, workspaceConfig);

                    dbExecute("jackrabbit-migration-2-drop-original-tables.sql",
                            "Original DB tables dropped");

                    dbExecute("jackrabbit-migration-3-rename-temp-tables.sql",
                            "Temporary DB tables renamed");

                    swapRepositoryHome(tempRepoHome);

                    logger.info("Complete repository migration took {} ms", (System.currentTimeMillis() - timer));
                } catch (Exception e) {
                    logger.warn("Unable to perform migration from BLOB store to DataStore. Cause: "
                            + e.getMessage(), e);
                } finally {
                    if (clusterActivated) {
                        // activate the cluster back
                        System.setProperty("cluster.activated", "true");
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Unable to check the source repository configuration."
                    + " Skip DataStore migration check.", e);
        }
    }

    private void performMigration(JahiaRepositoryConfig sourceCfg, JahiaRepositoryConfig targetCfg)
            throws RepositoryException {
        logger.info("Start migrating repository...");
        long globalTimer = System.currentTimeMillis();

        RepositoryImpl source = JahiaRepositoryImpl.create(sourceCfg);
        RepositoryImpl target = JahiaRepositoryImpl.create(targetCfg);

        int batchSize = Integer.getInteger("jahia.jackrabbit.persistenceCopierBatchSize", 500);
        try {
            if (batchSize <= 1) {
                new RepositoryCopier(source, target).copy();
            } else {
                new JahiaRepositoryCopier(source, target, batchSize).copy();
            }
        } finally {
            target.shutdown();
            source.shutdown();
        }

        logger.info("Repository data migrated in {} ms", (System.currentTimeMillis() - globalTimer));
    }

    private void swapRepositoryHome(File repoHomeCopy) throws IOException {
        String repoHomePath = repoHome.getAbsolutePath();
        try {
            if (keepBackup) {
                File backupDir = new File(repoHome.getParentFile(), "repository-original");
                FileUtils.moveDirectory(repoHome, backupDir);
                logger.info("Backup of the source repository folder at {}", backupDir);
            } else {
                try {
                    FileUtils.deleteDirectory(repoHome);
                } catch (IOException e) {
                    logger.warn("Issue while deleting the " + repoHomePath
                            + " directory, we will try to empty it");
                    FileUtils.cleanDirectory(repoHome);
                }
            }

            FileUtils.moveDirectory(repoHomeCopy, repoHome);
        } catch (IOException e) {
            logger.error(
                    "The migration was successful, except the last step: we are unable to delete directory {}."
                            + " Jahia server will be stopped now.", repoHomePath);
            String repoHomeCopyPath = repoHomeCopy.getAbsolutePath();
            logger.error(
                    "Please delete the directory {} manually (perhaps an OS reboot is required to free the file locks)"
                            + " and copy the content of {} to {}", new String[] { repoHomePath,
                            repoHomeCopyPath, repoHomePath });
            logger.error(
                    "Also delete the following folders if they are present:\n{}\\index\n{}\\workspaces\\default\\index\n{}\\workspaces\\live\\index",
                    new String[] { repoHomePath, repoHomePath, repoHomePath });

            logger.error("Start Jahia server afterwards.", new String[] { repoHomePath,
                    repoHomeCopyPath, repoHomePath });
            System.exit(1);
        }

        logger.info("Replaced repository with the migrated copy");
    }

}
