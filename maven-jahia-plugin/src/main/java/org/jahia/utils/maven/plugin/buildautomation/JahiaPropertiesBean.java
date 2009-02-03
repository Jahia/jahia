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

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: islam
 * Date: 16 juil. 2008
 * Time: 10:33:15
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPropertiesBean {
    private File outputDirectory;
    private String jahiaFileRepositoryDiskPath;
    private String  release ;
    private String  server   ;
    private String localIp  ;
    private String jahiaEtcDiskPath ;
    private String jahiaVarDiskPath ;
    private String jahiaNewTemplatesDiskPath  ;
    private String  jahiaNewWebAppsDiskPath ;
    private String jahiaSharedTemplatesDiskPath ;
    private String jahiaTemplatesHttpPath ;
    private String jahiaEnginesHttpPath  ;
    private String jahiaJavaScriptHttpPath  ;
    private String jahiaWebAppsDeployerBaseURL ;
    private String datasource_name ;
    private String outputCacheActivated  ;
    private String outputCacheDefaultExpirationDelay ;
    private String outputCacheExpirationOnly   ;
    private String outputContainerCacheActivated  ;
    private String  containerCacheDefaultExpirationDelay  ;
    private String containerCacheLiveModeOnly  ;
    private String esiCacheActivated    ;
    private String Jahia_WebApps_Deployer_Service ;
    private String defautSite  ;
    private String cluster_activated  ;
    private String  cluster_node_serverId   ;
    private String  processingServer;
    private String  jahiaFilesTemplatesDiskPath  ;
    private String jahiaImportsDiskPath ;
    private String    jahiaFilesBigTextDiskPath;
    private String bigtext_service  ;
    private String   jahiaxmlPath ;
    private List  clusterNodes  ;
    private String db_script;
    private String db_starthsqlserver;
    private String developmentMode;


    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getJahiaFileRepositoryDiskPath() {
        return jahiaFileRepositoryDiskPath;
    }

    public void setJahiaFileRepositoryDiskPath(String jahiaFileRepositoryDiskPath) {
        this.jahiaFileRepositoryDiskPath = jahiaFileRepositoryDiskPath;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getDb_script() {
        return db_script;
    }

    public void setDb_script(String db_script) {
        this.db_script = db_script;
    }

    public String getJahiaEtcDiskPath() {
        return jahiaEtcDiskPath;
    }

    public void setJahiaEtcDiskPath(String jahiaEtcDiskPath) {
        this.jahiaEtcDiskPath = jahiaEtcDiskPath;
    }

    public String getJahiaVarDiskPath() {
        return jahiaVarDiskPath;
    }

    public void setJahiaVarDiskPath(String jahiaVarDiskPath) {
        this.jahiaVarDiskPath = jahiaVarDiskPath;
    }

    public String getJahiaNewTemplatesDiskPath() {
        return jahiaNewTemplatesDiskPath;
    }

    public void setJahiaNewTemplatesDiskPath(String jahiaNewTemplatesDiskPath) {
        this.jahiaNewTemplatesDiskPath = jahiaNewTemplatesDiskPath;
    }

    public String getJahiaNewWebAppsDiskPath() {
        return jahiaNewWebAppsDiskPath;
    }

    public void setJahiaNewWebAppsDiskPath(String jahiaNewWebAppsDiskPath) {
        this.jahiaNewWebAppsDiskPath = jahiaNewWebAppsDiskPath;
    }

    public String getJahiaSharedTemplatesDiskPath() {
        return jahiaSharedTemplatesDiskPath;
    }

    public void setJahiaSharedTemplatesDiskPath(String jahiaSharedTemplatesDiskPath) {
        this.jahiaSharedTemplatesDiskPath = jahiaSharedTemplatesDiskPath;
    }

    public String getJahiaTemplatesHttpPath() {
        return jahiaTemplatesHttpPath;
    }

    public void setJahiaTemplatesHttpPath(String jahiaTemplatesHttpPath) {
        this.jahiaTemplatesHttpPath = jahiaTemplatesHttpPath;
    }

    public String getJahiaEnginesHttpPath() {
        return jahiaEnginesHttpPath;
    }

    public void setJahiaEnginesHttpPath(String jahiaEnginesHttpPath) {
        this.jahiaEnginesHttpPath = jahiaEnginesHttpPath;
    }

    public String getJahiaJavaScriptHttpPath() {
        return jahiaJavaScriptHttpPath;
    }

    public void setJahiaJavaScriptHttpPath(String jahiaJavaScriptHttpPath) {
        this.jahiaJavaScriptHttpPath = jahiaJavaScriptHttpPath;
    }

    public String getJahiaWebAppsDeployerBaseURL() {
        return jahiaWebAppsDeployerBaseURL;
    }

    public void setJahiaWebAppsDeployerBaseURL(String jahiaWebAppsDeployerBaseURL) {
        this.jahiaWebAppsDeployerBaseURL = jahiaWebAppsDeployerBaseURL;
    }

    public String getDatasource_name() {
        return datasource_name;
    }

    public void setDatasource_name(String datasource_name) {
        this.datasource_name = datasource_name;
    }

    public String getOutputCacheActivated() {
        return outputCacheActivated;
    }

    public void setOutputCacheActivated(String outputCacheActivated) {
        this.outputCacheActivated = outputCacheActivated;
    }

    public String getOutputCacheDefaultExpirationDelay() {
        return outputCacheDefaultExpirationDelay;
    }

    public void setOutputCacheDefaultExpirationDelay(String outputCacheDefaultExpirationDelay) {
        this.outputCacheDefaultExpirationDelay = outputCacheDefaultExpirationDelay;
    }

    public String getOutputCacheExpirationOnly() {
        return outputCacheExpirationOnly;
    }

    public void setOutputCacheExpirationOnly(String outputCacheExpirationOnly) {
        this.outputCacheExpirationOnly = outputCacheExpirationOnly;
    }

    public String getOutputContainerCacheActivated() {
        return outputContainerCacheActivated;
    }

    public void setOutputContainerCacheActivated(String outputContainerCacheActivated) {
        this.outputContainerCacheActivated = outputContainerCacheActivated;
    }

    public String getContainerCacheDefaultExpirationDelay() {
        return containerCacheDefaultExpirationDelay;
    }

    public void setContainerCacheDefaultExpirationDelay(String containerCacheDefaultExpirationDelay) {
        this.containerCacheDefaultExpirationDelay = containerCacheDefaultExpirationDelay;
    }

    public String getContainerCacheLiveModeOnly() {
        return containerCacheLiveModeOnly;
    }

    public void setContainerCacheLiveModeOnly(String containerCacheLiveModeOnly) {
        this.containerCacheLiveModeOnly = containerCacheLiveModeOnly;
    }

    public String getEsiCacheActivated() {
        return esiCacheActivated;
    }

    public void setEsiCacheActivated(String esiCacheActivated) {
        this.esiCacheActivated = esiCacheActivated;
    }

    public String getJahia_WebApps_Deployer_Service() {
        return Jahia_WebApps_Deployer_Service;
    }

    public void setJahia_WebApps_Deployer_Service(String jahia_WebApps_Deployer_Service) {
        Jahia_WebApps_Deployer_Service = jahia_WebApps_Deployer_Service;
    }

    public String getDefautSite() {
        return defautSite;
    }

    public void setDefautSite(String defautSite) {
        this.defautSite = defautSite;
    }

    public String getCluster_activated() {
        return cluster_activated;
    }

    public void setCluster_activated(String cluster_activated) {
        this.cluster_activated = cluster_activated;
    }

    public String getCluster_node_serverId() {
        return cluster_node_serverId;
    }

    public void setCluster_node_serverId(String cluster_node_serverId) {
        this.cluster_node_serverId = cluster_node_serverId;
    }

    public String getProcessingServer() {
        return processingServer;
    }

    public void setProcessingServer(String processingServer) {
        this.processingServer = processingServer;
    }

    public String getBigtext_service() {
        return bigtext_service;
    }

    public void setBigtext_service(String bigtext_service) {
        this.bigtext_service = bigtext_service;
    }

    public String getJahiaFilesTemplatesDiskPath() {
        return jahiaFilesTemplatesDiskPath;
    }

    public void setJahiaFilesTemplatesDiskPath(String jahiaFilesTemplatesDiskPath) {
        this.jahiaFilesTemplatesDiskPath = jahiaFilesTemplatesDiskPath;
    }

    public String getJahiaImportsDiskPath() {
        return jahiaImportsDiskPath;
    }

    public void setJahiaImportsDiskPath(String jahiaImportsDiskPath) {
        this.jahiaImportsDiskPath = jahiaImportsDiskPath;
    }

    public String getJahiaFilesBigTextDiskPath() {
        return jahiaFilesBigTextDiskPath;
    }

    public void setJahiaFilesBigTextDiskPath(String jahiaFilesBigTextDiskPath) {
        this.jahiaFilesBigTextDiskPath = jahiaFilesBigTextDiskPath;
    }

    public String getJahiaxmlPath() {
        return jahiaxmlPath;
    }

    public void setJahiaxmlPath(String jahiaxmlPath) {
        this.jahiaxmlPath = jahiaxmlPath;
    }

    public List getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(List clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public void setDb_StartHsqlServer(String db_starthsqlserver) {
        this.db_starthsqlserver = db_starthsqlserver;
    }

    public String getDb_starthsqlserver() {
        return db_starthsqlserver;
    }

    public void setDevelopmentMode(String developmentMode) {
        this.developmentMode = developmentMode;
    }

    public String getDevelopmentMode() {
        return developmentMode;
    }
}
