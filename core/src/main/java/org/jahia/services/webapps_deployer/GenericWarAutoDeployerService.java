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
package org.jahia.services.webapps_deployer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.webapps.JahiaWebAppDef;
import org.jahia.data.webapps.JahiaWebAppsPackage;
import org.jahia.data.webapps.JahiaWebAppsWarHandler;
import org.jahia.exceptions.JahiaException;

/**
 * Generic portlet WAR deployer service. Can be used by any server which
 * supports auto-deployment of unexploded WAR files. The specified WAR file is
 * copied into the Jetspeed's hot-deployment (<code>WEB-INF/etc/jetspeed/deploy</code>)
 * folder.<br>
 * Jetspeed adds required JetspeedContainerServlet entry into the web.xml file
 * and portlet.tld and copies (moves) the WAR file into the server deployment
 * folder, which is specified by the <code>autodeployment.target.dir</code>
 * property in the <code>WEB-INF/etc/jetspeed/conf/jetspeed.properties</code>
 * file. Its default value is <code>${applicationRoot}/../</code>, which
 * means the folder, one level up from the Jahia Web application folder.<br>
 * Note this implementation does not support undeployment of the deployed Web
 * applications. You can undeploy them by deleting the deployed WAR files from
 * the server's deploy folder.
 * 
 * @author Sergiy Shyrkov
 */
public class GenericWarAutoDeployerService extends JahiaWebAppsDeployerService {

    private static GenericWarAutoDeployerService instance;

    private static Logger logger = Logger
            .getLogger(GenericWarAutoDeployerService.class);

    public static GenericWarAutoDeployerService getInstance() {

        if (instance == null) {
            synchronized (GenericWarAutoDeployerService.class) {
                if (instance == null) {
                    instance = new GenericWarAutoDeployerService();
                }
            }
        }
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.services.webapps_deployer.JahiaWebAppsDeployerService#canDeploy()
     */
    @Override
    public boolean canDeploy() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.services.webapps_deployer.JahiaWebAppsDeployerService#deploy(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public boolean deploy(String context, String filePath)
            throws JahiaException {
        boolean success = false;

        List webApps = getWebApps(filePath);
        if (webApps != null && webApps.size() > 0) {
            JahiaWebAppDef webAppDef = (JahiaWebAppDef) webApps.get(0);

            if ("portlet".equals(webAppDef.getType())) {
                logger.info("Deploying '" + filePath
                        + "' as a WAR with portlets");
                success = deployWarWithPortlets(context, filePath, webAppDef);
            } else {
                logger.info("Deploying '" + filePath
                        + "' as a WAR without portlets");
                success = deployWar(context, filePath, webAppDef);
            }
            if (success) {
                webAppDef.setName(FilenameUtils.getBaseName(filePath));
                registerWebApps(webAppDef.getContextRoot(), filePath, webApps);
            }
        } else {
            logger.warn("Unable to handle the WAR file '" + filePath
                    + "'. Skipping deployment");
        }

        return success;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.services.webapps_deployer.JahiaWebAppsDeployerService#deploy(java.util.List)
     */
    @Override
    public boolean deploy(List files) {

        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            if (file.isFile() && file.canWrite()) {
                if ("war".equals(FilenameUtils.getExtension(file.getName()))) {
                    JahiaWebAppsPackage pack = null;
                    try {
                        pack = loadWebAppInfo(file.getAbsolutePath());
                    } catch (JahiaException e) {
                        logger.error(
                                "Error loading Web application info from resource '"
                                        + file.getPath()
                                        + "'. Skipping deployment.", e);
                        continue;
                    }
                    if (pack != null) {
                        try {
                            deploy(pack.getContextRoot(), file
                                    .getAbsolutePath());
                        } catch (JahiaException e) {
                            logger.error(
                                    "Error deploying Web application from resource '"
                                            + file.getPath()
                                            + "'. Skipping deployment.", e);
                            continue;
                        } finally {
                            file.delete();
                        }
                    } else {
                        logger
                                .warn("Unable to deploy Web application from resource '"
                                        + file.getPath()
                                        + "'. Skipping deployment.");
                        continue;
                    }
                } else {
                    logger.warn("The resource '" + file.getPath()
                            + "' is not a WAR file. Skipping deployment.");
                }

            } else {
                logger.warn("The resource '" + file.getPath()
                        + "' is either a directory or a not writable file."
                        + " Skipping deployment.");
            }
        }
        return true;
    }

    private boolean deployWar(String context, String filePath,
            JahiaWebAppDef webAppDef) {
        boolean success = false;

        String autoDeploymentDirPath = getAutoDeploymentDir();
        File autoDeploymentDir = StringUtils.isNotEmpty(autoDeploymentDirPath) ? new File(
                autoDeploymentDirPath)
                : null;
        if (autoDeploymentDir != null && autoDeploymentDir.isDirectory()) {
            try {
                // copy the file to the server's deploy directory
                FileUtils.copyFile(new File(filePath), new File(
                        autoDeploymentDir, context + ".war"));
                success = true;
            } catch (IOException e) {
                logger.error("Error copying WAR file '"
                        + filePath
                        + "' to the file '"
                        + new File(settingsBean
                                .getJetspeedDeploymentDirectory(), context
                                + ".war").getPath() + "'", e);
            }
        } else {
            logger
                    .error("Auto deployment directory does not exists under the path '"
                            + (autoDeploymentDir != null ? autoDeploymentDir
                                    .getPath() : null)
                            + "'. Check the 'autodeployment.target.dir' property in the jetspeed.properties file.");
        }

        return success;
    }

    private boolean deployWarWithPortlets(String context, String filePath,
            JahiaWebAppDef webAppDef) {
        boolean success = false;

        try {
            // copy the file to the Jetspeed's deploy directory
            FileUtils.copyFile(new File(filePath), new File(settingsBean
                    .getJetspeedDeploymentDirectory(), context + ".war"));
            success = true;
        } catch (IOException e) {
            logger.error("Error copying WAR file '"
                    + filePath
                    + "' to the file '"
                    + new File(settingsBean.getJetspeedDeploymentDirectory(),
                            context + ".war").getPath() + "'", e);
        }

        return success;
    }

    private String getAutoDeploymentDir() {
        return null;
    }

    private List getWebApps(String warFilePath) {
        List webApps = null;

        if (warFilePath.endsWith(".war")) {
            try {
                JahiaWebAppsWarHandler warHanlder = new JahiaWebAppsWarHandler(
                        warFilePath);
                webApps = warHanlder.getWebAppsPackage().getWebApps();
                warHanlder.closeArchiveFile();
            } catch (Exception e) {
                logger.error("Error handling the WAR file '" + warFilePath
                        + "'. Skipping deployment", e);
            }
        } else {
            logger.warn("The resource '" + warFilePath
                    + "' is not a WAR file. Skipping deployment.");
        }

        return webApps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.services.JahiaService#stop()
     */
    @Override
    public void stop() throws JahiaException {
        // no need to do anything
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.services.webapps_deployer.JahiaWebAppsDeployerService#undeploy(org.jahia.data.applications.ApplicationBean)
     */
    @Override
    public boolean undeploy(ApplicationBean app) throws JahiaException {
        boolean success = false;

        String autoDeploymentDirPath = getAutoDeploymentDir();
        File autoDeploymentDir = StringUtils.isNotEmpty(autoDeploymentDirPath) ? new File(
                autoDeploymentDirPath)
                : null;
        if (autoDeploymentDir != null && autoDeploymentDir.isDirectory()) {
            // delete the file from the server's deploy directory
            File warFile = new File(autoDeploymentDir, app.getContext()
                    + ".war");
            if (warFile.isFile() && warFile.exists()) {
                success = warFile.delete();
            }
        } else {
            logger
                    .error("Unable to undeploy application under context '"
                            + app.getContext()
                            + "'. Auto deployment directory does not exists under the path '"
                            + (autoDeploymentDir != null ? autoDeploymentDir
                                    .getPath() : null)
                            + "'. Check the 'autodeployment.target.dir' property in the jetspeed.properties file.");
        }

        return success;
    }
}
