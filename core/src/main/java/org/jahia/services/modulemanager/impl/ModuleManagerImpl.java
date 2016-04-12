/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.modulemanager.impl;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModulesPackage;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.ModuleManagerHelper;
import org.jahia.services.modulemanager.OperationResult;
import org.jahia.services.modulemanager.model.BundleDTO;
import org.jahia.services.modulemanager.model.ClusterNodeInfo;
import org.jahia.services.modulemanager.payload.BundleInfo;
import org.jahia.services.modulemanager.payload.BundleStateReport;
import org.jahia.services.modulemanager.payload.NodeStateReport;
import org.jahia.services.modulemanager.payload.OperationResultImpl;
import org.jahia.services.modulemanager.persistence.ModuleInfoPersister;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * The main entry point service for the module management service, providing functionality for module deployment, undeployment, start and
 * stop operations, which are performed in a seamless way on a standalone installation as well as across the platform cluster.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleManagerImpl implements ModuleManager {

    private static final Logger logger = LoggerFactory.getLogger(ModuleManagerImpl.class);
    private ClusterNodeInfo clusterNodeInfo;

    // MEGA-JAR handling review: template manager service
    private JahiaTemplateManagerService templateManagerService;

    private ModuleInfoPersister persister;

    private static void populateFromManifest(BundleDTO bundle, Resource bundleFile) throws IOException {
        JarInputStream jarIs = new JarInputStream(bundleFile.getInputStream());
        try {
            Manifest mf = jarIs.getManifest();
            if (mf != null) {
                bundle.setSymbolicName(mf.getMainAttributes().getValue("Bundle-SymbolicName"));
                String version = mf.getMainAttributes().getValue("Implementation-Version");
                if (version == null) {
                    version = mf.getMainAttributes().getValue("Bundle-Version");
                }
                bundle.setVersion(version);
                bundle.setDisplayName(mf.getMainAttributes().getValue("Bundle-Name"));
                bundle.setGroupId(mf.getMainAttributes().getValue("Jahia-GroupId"));
            }
        } finally {
            IOUtils.closeQuietly(jarIs);
        }
    }

    private static BundleDTO toBundle(Resource bundleResource) throws IOException {
        // store bundle into a temporary file
        DigestInputStream dis = toDigestInputStream(bundleResource.getInputStream());

        BundleDTO b = new BundleDTO();
        // populate data from manifest
        populateFromManifest(b, bundleResource);

        if (StringUtils.isBlank(b.getSymbolicName()) || StringUtils.isBlank(b.getVersion())) {
            // not a valid JAR or bundle information is missing -> we stop here
            return null;
        }

        b.setBundleKey(b.getSymbolicName() + "-" + b.getVersion());

        // calculate checksum
        b.setChecksum(Hex.encodeHexString(dis.getMessageDigest().digest()));

        // keep original filename if available
        b.setFileName(StringUtils.defaultIfBlank(bundleResource.getFilename(),
                b.getSymbolicName() + "-" + b.getVersion() + ".jar"));

        b.setJarFile(bundleResource);

        return b;
    }

    static DigestInputStream toDigestInputStream(InputStream is) {
        try {
            return new DigestInputStream(is, MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private OperationResult installModule(Resource bundleResource, Manifest manifest, MessageContext context, List<String> providedBundles, boolean forceUpdate, String... nodes) throws IOException, BundleException {
        String symbolicName = ModuleManagerHelper.getManifestSymbolicName(manifest);
        String version = ModuleManagerHelper.getManifestVersion(manifest);
        String groupId = ModuleManagerHelper.getManifestGroupId(manifest);

        try {

            if (ModuleManagerHelper.isDifferentModuleWithSameIdExists(symbolicName, groupId, context, templateManagerService)) {
                return new OperationResultImpl(false, "Module installation failed because a module exists with the same name " + symbolicName, new BundleInfo(symbolicName, version));
            }

            if (!forceUpdate && ModuleManagerHelper.isModuleExists(templateManagerService.getTemplatePackageRegistry(), symbolicName, version, context)) {
                return new OperationResultImpl(false, "Module installation failed because a module exists with the same name and version. " + symbolicName + "-" + version , new BundleInfo(symbolicName, version));
            }

            final BundleDTO bundle = toBundle(bundleResource);

            if (bundle == null) {
                return OperationResultImpl.NOT_VALID_BUNDLE;
            }

            // check, if we have this bundle already installed
            // FIXME: and what about forceUpdate ??
            if (persister.alreadyInstalled(bundle)) {
                // we have exactly same bundle installed already -> refuse
                return OperationResultImpl.ALREADY_INSTALLED;
            }

            // TODO check for missing dependencies before installing?

            persister.addBundle(bundle);

            // store bundle in JCR and create operation node
            OperationResult result = install(persister.getLocation(bundle), nodes);
            result.getBundleInfoList().add(new BundleInfo(symbolicName, version));

            return result;
        } catch (Exception ex) {
            // Add message to the context
            if (context != null) {
                context.addMessage(new MessageBuilder().source("moduleInstallionFailed")
                        .code("serverSettings.manageModules.install.failed")
                        .arg(ex.getMessage())
                        .error()
                        .build());
            }
            return new OperationResultImpl(false, ex.getMessage(), new BundleInfo(symbolicName, version));
        }
    }

    private void startBundles(MessageContext context, List<BundleInfo> bundleInfoList, SettingsBean settingsBean) throws BundleException {
        for (BundleInfo bundleInfo : bundleInfoList) {
            org.osgi.framework.Bundle bundle = BundleUtils.getBundle(bundleInfo.getSymbolicName(), bundleInfo.getVersion());
            if (bundle != null) {
                Set<ModuleVersion> allVersions = templateManagerService.getTemplatePackageRegistry().getAvailableVersionsForModule(bundle.getSymbolicName());
                JahiaTemplatesPackage currentVersion = templateManagerService.getTemplatePackageRegistry().lookupById(bundle.getSymbolicName());
                if (allVersions.size() == 1 ||
                        ((settingsBean.isDevelopmentMode() && currentVersion != null && BundleUtils.getModule(bundle).getVersion().compareTo(currentVersion.getVersion()) > 0))) {
                    start(bundleInfo.getSymbolicName() + "-" + bundleInfo.getVersion());
                    if (context != null) {
                        context.addMessage(new MessageBuilder().source("moduleFile")
                                .code("serverSettings.manageModules.install.uploadedAndStarted")
                                .args(new String[]{bundle.getSymbolicName(), bundle.getVersion().toString()})
                                .build());
                    }
                    logger.info("Module has been successfully uploaded and started. Please check its status in the list.");
                } else {
                    if (context != null) {
                        context.addMessage(new MessageBuilder().source("moduleFile")
                                .code("serverSettings.manageModules.install.uploaded")
                                .args(new String[]{bundle.getSymbolicName(), bundle.getVersion().toString()})
                                .build());
                    }
                    logger.info("Module has been successfully uploaded. Check status in the list.");
                }
            }
        }
    }

    @Override
    public OperationResult install(Resource bundleResource, MessageContext context, String originalFilename, boolean forceUpdate, String... nodes) throws ModuleManagementException {
        // save to a temporary file and create Bundle data object

        OperationResult installResult = null;
        try {
            // get the manifest
            Manifest manifest = ModuleManagerHelper.getJarFileManifest(bundleResource.getFile());
            if (ModuleManagerHelper.isPackageModule(manifest)) {
                if (ModuleManagerHelper.isValidJahiaPackageFile(manifest, context, originalFilename)) {
                    JarFile jarFile = new JarFile(bundleResource.getFile());
                    try {
                        ModulesPackage pack = ModulesPackage.create(jarFile);
                        List<String> providedBundles = new ArrayList<String>(pack.getModules().keySet());
                        for (Map.Entry<String, ModulesPackage.PackagedModule> entry : pack.getModules().entrySet()) {
                            OperationResult res = installModule(new FileSystemResource(entry.getValue().getModuleFile()), ModuleManagerHelper.getJarFileManifest(entry.getValue().getModuleFile()), context, providedBundles, forceUpdate);
                            // to be reviewed
                            if (installResult == null) {
                                installResult = res;
                            } else {
                                if (res != null && res.isSuccess()) {
                                    installResult.getBundleInfoList().addAll(res.getBundleInfoList());
                                }
                            }
                        }
                    } catch (Exception ex) {
                        logger.error("Error during jahia package installation.", ex);
                    } finally {
                        IOUtils.closeQuietly(jarFile);
                    }

                } else {
                    installResult = new OperationResultImpl(false, "Operation aborted. Please, check the bundle package name or license.");
                }
            } else {
                installResult = installModule(bundleResource, manifest, context, null, forceUpdate);
            }

        } catch (Exception e) {
            throw new ModuleManagementException(e);
        }

        if (installResult != null && installResult.isSuccess()) {
            try {
                startBundles(context, installResult.getBundleInfoList(), SettingsBean.getInstance());
            } catch (BundleException bex) {
                logger.error("An error occured during starting installed bundles", bex);
            }
        }
        return installResult;
    }

    private OperationResult install(String location, final String[] nodeIds) throws RepositoryException {
        try {
            Bundle b = FrameworkService.getBundleContext().installBundle(location);
            return new OperationResultImpl(true, "Operation successfully performed", new BundleInfo(b.getSymbolicName(), b.getVersion().toString()));
        } catch (BundleException e) {
            throw new ModuleManagementException(e);
        }
    }


    public void setPersister(ModuleInfoPersister persister) {
        this.persister = persister;
    }


    @Override
    public OperationResult start(String bundleKey, String... nodes) {
        try {
            BundleDTO bundleDTO = persister.getBundle(bundleKey);
            if (bundleDTO != null) {
                Bundle bundle = BundleUtils.getBundle(bundleDTO.getSymbolicName(), bundleDTO.getVersion());
                if (bundle != null) {
                    bundle.start();
                    return new OperationResultImpl(true, "Operation successfully performed", new BundleInfo(bundleDTO.getSymbolicName(), bundleDTO.getVersion()));
                }
                return new OperationResultImpl(true, "Bundle not found", new BundleInfo(bundleDTO.getSymbolicName(), bundleDTO.getVersion()));
            }
            return new OperationResultImpl(false, "Bundle not found");
        } catch (BundleException e) {
            throw new ModuleManagementException(e);
        }
    }

    @Override
    public OperationResult stop(String bundleKey, String... nodes) {
        try {
            BundleDTO bundleDTO = persister.getBundle(bundleKey);
            if (bundleDTO != null) {
                Bundle bundle = BundleUtils.getBundle(bundleDTO.getSymbolicName(), bundleDTO.getVersion());
                if (bundle != null) {
                    bundle.stop();
                    return new OperationResultImpl(true, "Operation successfully performed", new BundleInfo(bundleDTO.getSymbolicName(), bundleDTO.getVersion()));
                }
                return new OperationResultImpl(true, "Bundle not found", new BundleInfo(bundleDTO.getSymbolicName(), bundleDTO.getVersion()));
            }
            return new OperationResultImpl(false, "Bundle not found");
        } catch (BundleException e) {
            throw new ModuleManagementException(e);
        }
    }

    @Override
    public OperationResult uninstall(String bundleKey, String... nodes) {
        try {
            BundleDTO bundleDTO = persister.getBundle(bundleKey);
            if (bundleDTO != null) {
                Bundle bundle = BundleUtils.getBundle(bundleDTO.getSymbolicName(), bundleDTO.getVersion());
                if (bundle != null) {
                    bundle.uninstall();
                    return new OperationResultImpl(true, "Operation successfully performed", new BundleInfo(bundleDTO.getSymbolicName(), bundleDTO.getVersion()));
                }
                return new OperationResultImpl(true, "Bundle not found", new BundleInfo(bundleDTO.getSymbolicName(), bundleDTO.getVersion()));
            }
            return new OperationResultImpl(false, "Bundle not found");
        } catch (BundleException e) {
            throw new ModuleManagementException(e);
        }
    }

    public void setClusterNodeInfo(ClusterNodeInfo clusterNodeInfo) {
        this.clusterNodeInfo = clusterNodeInfo;
    }

    @Override
    public BundleStateReport getBundleState(final String bundleKey, String... targetNodes) throws ModuleManagementException {
//        final String[] finalTargetNodes = targetNodes == null || targetNodes.length == 0
//                ? new String[] { clusterNodeInfo.getId() } : targetNodes;
//
//        Map<String, String> map = new HashMap<String,String>();
//        try {
//            map = persister.doExecute(new OCMCallback<Map<String, String>>() {
//                @Override
//                public Map<String, String>  doInOCM(ObjectContentManager ocm) {
//                    Map<String, String> result = new HashMap<String,String>();
//                    for (String targetNode : finalTargetNodes) {
//                        String path = "/module-management/nodes/" +targetNode+ "/bundles/" + bundleKey;
//                        NodeBundle nodeBundle = (NodeBundle) ocm.getObject(NodeBundle.class, path);
//                        if (nodeBundle != null) {
//                            result.put(targetNode, nodeBundle.getState());
//                        }
//                    }
//                    return result;
//                }
//            });
//
//            BundleStateReport bundleStateReport = new BundleStateReport(bundleKey,map);
//            return  bundleStateReport;
//        } catch (RepositoryException e) {
//            throw new ModuleManagementException(e);
//        }
        return null;
    }

    @Override
    public Set<NodeStateReport> getNodesBundleStates(String... targetNodes) throws ModuleManagementException {
//        final Set<String> finalTargetNodes = new HashSet<>();
//        if (targetNodes == null || targetNodes.length == 0) {
//            finalTargetNodes.add(clusterNodeInfo.getId());
//        } else {
//            finalTargetNodes.addAll(Arrays.asList(targetNodes));
//        }
//        Set<NodeStateReport> result = new HashSet<NodeStateReport>();
//        try {
//            result = persister.doExecute(new OCMCallback<Set<NodeStateReport>>() {
//                @Override
//                public Set<NodeStateReport> doInOCM(ObjectContentManager ocm) throws RepositoryException {
//                    List<ClusterNode> nodes = new ArrayList<ClusterNode>();
//                    Set<NodeStateReport> result = new HashSet<NodeStateReport>();
//                    Node node = ocm.getSession().getNode("/module-management/nodes");
//                    NodeIterator ops = node.getNodes();
//                    if (ops.hasNext()) {
//                        String nodePath = ops.nextNode().getPath();
//                        if(finalTargetNodes.contains(nodePath.substring("/module-management/nodes/".length()))) {
//                            nodes.add((ClusterNode) ocm.getObject(ClusterNode.class, nodePath));
//                        }
//                    }
//                    for (ClusterNode clusterNode : nodes)
//                    {
//                        Set<BundleStateReport> bundleStateReports = new HashSet<BundleStateReport>();
//                        for (String key : clusterNode.getBundles().keySet()) {
//                            Map<String,String> map = new HashMap<String, String>();
//                            NodeBundle nodeBundle = clusterNode.getBundles().get(key);
//                            map.put(nodeBundle.getBundle().getIdentifier(),nodeBundle.getState());
//                            BundleStateReport bundleStateReport = new BundleStateReport(nodeBundle.getBundle().getName(),map);
//                            bundleStateReports.add(bundleStateReport);
//                        }
//                        NodeStateReport nodeStateReport = new NodeStateReport(clusterNode.getIdentifier(),bundleStateReports);
//                        result.add(nodeStateReport);
//                    }
//                    return result;
//                }
//            });
//        } catch (RepositoryException e) {
//            throw new ModuleManagementException(e);
//        }
//        return result;
        return null;
    }

//    @Override
//    public OperationState getOperationState(final String operationId) throws ModuleManagementException {
//        try {
//            Operation operation = persister.doExecute(new OCMCallback<Operation>() {
//                @Override
//                public Operation doInOCM(ObjectContentManager ocm) throws RepositoryException {
//                    return (Operation) ocm.getObjectByUuid(operationId);
//                }
//            });
//            OperationState operationState = new OperationState(operation.getName(), operation.getAction(), operation.getInfo(), operation.getState(), operation.isCompleted());
//            return operationState;
//        } catch (RepositoryException e) {
//            throw new ModuleManagementException(e);
//        }
//    }
//


    /**
     * Set the Jahia template manager service
     * @param templateManagerService the template manager service bean to set
     */
    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }
}
