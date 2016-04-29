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

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;

import javax.jcr.RepositoryException;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.ModuleManagerHelper;
import org.jahia.services.modulemanager.OperationResult;
import org.jahia.services.modulemanager.payload.BundleInfo;
import org.jahia.services.modulemanager.payload.BundleStateReport;
import org.jahia.services.modulemanager.payload.NodeStateReport;
import org.jahia.services.modulemanager.payload.OperationResultImpl;
import org.jahia.services.modulemanager.persistence.BundlePersister;
import org.jahia.services.modulemanager.persistence.PersistedBundle;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.core.io.Resource;

/**
 * The main entry point service for the module management service, providing functionality for module deployment, undeployment, start and
 * stop operations, which are performed in a seamless way on a standalone installation as well as across the platform cluster.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleManagerImpl implements ModuleManager {

    private static enum Operation {
        start, stop, uninstall;
    }

    private static final Logger logger = LoggerFactory.getLogger(ModuleManagerImpl.class);

    private BundlePersister persister;

    private JahiaTemplateManagerService templateManagerService;

    @Override
    public BundleStateReport getBundleState(final String bundleKey, String target)
            throws ModuleManagementException {
        return null;
    }

    @Override
    public Set<NodeStateReport> getNodesBundleStates(String target) throws ModuleManagementException {
        return null;
    }

    private OperationResult install(PersistedBundle info, final String target) throws RepositoryException {
        try {
            FrameworkService.getBundleContext().installBundle(info.getLocation());
            return new OperationResultImpl(true, "Operation successfully performed", info.getBundleInfo());
        } catch (BundleException e) {
            throw new ModuleManagementException(e);
        }
    }

    @Override
    public OperationResult install(Resource bundleResource, String target) throws ModuleManagementException {
        return install(bundleResource, target, false);
        // save to a temporary file and create Bundle data object

//        OperationResult result = null;
//        try {
            // get the manifest
//            Manifest manifest = ModuleManagerHelper.getJarFileManifest(bundleResource.getFile());
//            if (ModuleManagerHelper.isPackageModule(manifest)) {
//                if (ModuleManagerHelper.isValidJahiaPackageFile(manifest, context, originalFilename)) {
//                    JarFile jarFile = new JarFile(bundleResource.getFile());
//                    try {
//                        ModulesPackage pack = ModulesPackage.create(jarFile);
//                        List<String> providedBundles = new ArrayList<String>(pack.getModules().keySet());
//                        for (Map.Entry<String, ModulesPackage.PackagedModule> entry : pack.getModules().entrySet()) {
//                            OperationResult res = installModule(
//                                    new FileSystemResource(entry.getValue().getModuleFile()),
//                                    ModuleManagerHelper.getJarFileManifest(entry.getValue().getModuleFile()), context,
//                                    providedBundles, forceUpdate);
//                            // to be reviewed
//                            if (installResult == null) {
//                                installResult = res;
//                            } else {
//                                if (res != null && res.isSuccess()) {
//                                    installResult.getBundleInfoList().addAll(res.getBundleInfoList());
//                                }
//                            }
//                        }
//                    } catch (Exception ex) {
//                        logger.error("Error during jahia package installation.", ex);
//                    } finally {
//                        IOUtils.closeQuietly(jarFile);
//                    }
//
//                } else {
//                    installResult = new OperationResultImpl(false,
//                            "Operation aborted. Please, check the bundle package name or license.");
//                }
//            } else {
//                installResult = installModule(bundleResource, manifest, context, null, forceUpdate);
//            }

//            final PersistedBundle moduleInfo = persister.extract(bundleResource);
//
//            if (moduleInfo == null) {
//                return OperationResultImpl.NOT_VALID_BUNDLE;
//            }
//
//            // TODO check for missing dependencies before installing?
//
//            persister.store(moduleInfo);
//
//            // store bundle in JCR and create operation node
//            result = install(moduleInfo, target);
//        } catch (Exception e) {
//            throw new ModuleManagementException(e);
//        }

//        if (installResult != null && installResult.isSuccess()) {
//            try {
//                startBundles(context, installResult.getBundleInfoList(), SettingsBean.getInstance());
//            } catch (BundleException bex) {
//                logger.error("An error occured during starting installed bundles", bex);
//            }
//        }
//        return result;
    }

    @Override
    public OperationResult install(Resource bundleResource, String target, boolean start)
            throws ModuleManagementException {
        OperationResult result = null;
        try {
            final PersistedBundle moduleInfo = persister.extract(bundleResource);

            if (moduleInfo == null) {
                return OperationResultImpl.NOT_VALID_BUNDLE;
            }

            // TODO check for missing dependencies before installing?

            persister.store(moduleInfo);

            // store bundle in JCR and create operation node
            result = install(moduleInfo, target);
            if (result.isSuccess()) {
                Bundle bundle = BundleUtils.getBundle(moduleInfo.getSymbolicName(), moduleInfo.getVersion());
                if (bundle != null) {
                    bundle.start();
                }
            }
        } catch (Exception e) {
            throw new ModuleManagementException(e);
        }

        return result;
    }

    private OperationResult installModule(Resource bundleResource, Manifest manifest, MessageContext context,
            List<String> providedBundles, String target) throws IOException, BundleException {
        String symbolicName = ModuleManagerHelper.getManifestSymbolicName(manifest);
        String version = ModuleManagerHelper.getManifestVersion(manifest);
        String groupId = ModuleManagerHelper.getManifestGroupId(manifest);

        try {

            if (ModuleManagerHelper.isDifferentModuleWithSameIdExists(symbolicName, groupId, context,
                    templateManagerService)) {
                return new OperationResultImpl(false,
                        "Module installation failed because a module exists with the same name " + symbolicName,
                        new BundleInfo(symbolicName, version));
            }

//            if (!forceUpdate && ModuleManagerHelper.isModuleExists(templateManagerService.getTemplatePackageRegistry(),
//                    symbolicName, version, context)) {
//                return new OperationResultImpl(false,
//                        "Module installation failed because a module exists with the same name and version. "
//                                + symbolicName + "-" + version,
//                        new BundleInfo(symbolicName, version));
//            }

            final PersistedBundle moduleInfo = persister.extract(bundleResource);

            if (moduleInfo == null) {
                return OperationResultImpl.NOT_VALID_BUNDLE;
            }

            // TODO check for missing dependencies before installing?

            persister.store(moduleInfo);

            // store bundle in JCR and create operation node
            OperationResult result = install(moduleInfo, target);
            result.getBundleInfoList().add(moduleInfo.getBundleInfo());

            return result;
        } catch (Exception ex) {
            // Add message to the context
            if (context != null) {
                context.addMessage(new MessageBuilder().source("moduleInstallionFailed")
                        .code("serverSettings.manageModules.install.failed").arg(ex.getMessage()).error().build());
            }
            return new OperationResultImpl(false, ex.getMessage(), new BundleInfo(symbolicName, version));
        }
    }

    /**
     * Performs the specified operation of the bundle.
     * 
     * @param bundleKey
     *            the key of the bundle to perform operation on
     * @param operation
     *            the operation type to be performed
     * @param target
     *            the target cluster group
     * @return the result of the operation
     */
    private OperationResult performOperation(String bundleKey, Operation operation, String target) {
        logger.info("Performing {} operation for bundle {} on nodes {}", new Object[] { operation, bundleKey, target });
        try {
            PersistedBundle info = persister.find(bundleKey);
            if (info != null) {
                Bundle bundle = BundleUtils.getBundle(info.getSymbolicName(), info.getVersion());
                if (bundle != null) {
                    switch (operation) {
                        case start:
                            bundle.start();
                            break;

                        case stop:
                            bundle.stop();
                            break;

                        case uninstall:
                            bundle.uninstall();
                            break;

                        default:
                            throw new UnsupportedOperationException("Unknown bundle operation: " + operation);
                    }

                    return new OperationResultImpl(true, "Operation successfully performed", info.getBundleInfo());
                }
                return new OperationResultImpl(true, "Bundle not found", info.getBundleInfo());
            }
            return new OperationResultImpl(false, "Bundle not found");
        } catch (BundleException e) {
            throw new ModuleManagementException(e);
        }
    }

    public void setPersister(BundlePersister persister) {
        this.persister = persister;
    }

    /**
     * Set the Jahia template manager service
     * 
     * @param templateManagerService
     *            the template manager service bean to set
     */
    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    @Override
    public OperationResult start(String bundleKey, String target) {
        return performOperation(bundleKey, Operation.start, target);
    }

    private void startBundles(MessageContext context, List<BundleInfo> bundleInfoList, SettingsBean settingsBean)
            throws BundleException {
        for (BundleInfo bundleInfo : bundleInfoList) {
            org.osgi.framework.Bundle bundle = BundleUtils.getBundle(bundleInfo.getSymbolicName(),
                    bundleInfo.getVersion());
            if (bundle != null) {
                Set<ModuleVersion> allVersions = templateManagerService.getTemplatePackageRegistry()
                        .getAvailableVersionsForModule(bundle.getSymbolicName());
                JahiaTemplatesPackage currentVersion = templateManagerService.getTemplatePackageRegistry()
                        .lookupById(bundle.getSymbolicName());
                if (allVersions.size() == 1 || ((settingsBean.isDevelopmentMode() && currentVersion != null
                        && BundleUtils.getModule(bundle).getVersion().compareTo(currentVersion.getVersion()) > 0))) {
                    start(bundleInfo.getKey(), null);
                    if (context != null) {
                        context.addMessage(new MessageBuilder().source("moduleFile")
                                .code("serverSettings.manageModules.install.uploadedAndStarted")
                                .args(new String[] { bundle.getSymbolicName(), bundle.getVersion().toString() })
                                .build());
                    }
                    logger.info(
                            "Module has been successfully uploaded and started. Please check its status in the list.");
                } else {
                    if (context != null) {
                        context.addMessage(new MessageBuilder().source("moduleFile")
                                .code("serverSettings.manageModules.install.uploaded")
                                .args(new String[] { bundle.getSymbolicName(), bundle.getVersion().toString() })
                                .build());
                    }
                    logger.info("Module has been successfully uploaded. Check status in the list.");
                }
            }
        }
    }

    @Override
    public OperationResult stop(String bundleKey, String target) {
        return performOperation(bundleKey, Operation.stop, target);
    }

    @Override
    public OperationResult uninstall(String bundleKey, String target) {
        return performOperation(bundleKey, Operation.uninstall, target);
    }
}
