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

import java.util.Set;

import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.OperationResult;
import org.jahia.services.modulemanager.payload.BundleStateReport;
import org.jahia.services.modulemanager.payload.NodeStateReport;
import org.jahia.services.modulemanager.payload.OperationResultImpl;
import org.jahia.services.modulemanager.persistence.BundlePersister;
import org.jahia.services.modulemanager.persistence.PersistedBundle;
import org.jahia.services.modulemanager.spi.BundleService;
import org.jahia.services.modulemanager.spi.BundleService.Operation;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * The main entry point service for the module management service, providing functionality for module deployment, undeployment, start and
 * stop operations, which are performed in a seamless way on a standalone installation as well as across the platform cluster.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleManagerImpl implements ModuleManager {

    private static final Logger logger = LoggerFactory.getLogger(ModuleManagerImpl.class);

    private BundleService bundleService;

    private BundlePersister persister;

    @Override
    public BundleStateReport getBundleState(final String bundleKey, String target) throws ModuleManagementException {
        return null;
    }

    @Override
    public Set<NodeStateReport> getNodesBundleStates(String target) throws ModuleManagementException {
        return null;
    }

    private OperationResult install(PersistedBundle info, final String target, boolean start)
            throws ModuleManagementException {
        try {
            bundleService.install(info.getLocation(), target, start);
            return new OperationResultImpl(true, "Operation successfully performed", info.getBundleInfo());
        } catch (BundleException e) {
            throw new ModuleManagementException(e);
        }
    }

    @Override
    public OperationResult install(Resource bundleResource, String target) throws ModuleManagementException {
        return install(bundleResource, target, false);
    }

    @Override
    public OperationResult install(Resource bundleResource, String target, boolean start)
            throws ModuleManagementException {
        long startTime = System.currentTimeMillis();
        logger.info("Performing installation for bundle {} on target {}", new Object[] { bundleResource, target });

        OperationResult result = null;
        PersistedBundle bundleInfo = null;
        try {
            bundleInfo = persister.extract(bundleResource);

            if (bundleInfo == null) {
                return OperationResultImpl.NOT_VALID_BUNDLE;
            }

            // TODO check for missing dependencies before installing?

            persister.store(bundleInfo);

            // store bundle in JCR and create operation node
            result = install(bundleInfo, target, start);
            if (result.isSuccess()) {
                bundleService.performOperation(bundleInfo, Operation.START, target);
            }
        } catch (Exception e) {
            throw new ModuleManagementException(e);
        } finally {
            logger.info("Installation completed for bundle {} on target {} in {} ms. Opearation result: {}",
                    new Object[] { bundleInfo != null ? bundleInfo.getBundleInfo() : bundleResource, target,
                            System.currentTimeMillis() - startTime, result });
        }

        return result;
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
        long startTime = System.currentTimeMillis();
        logger.info("Performing {} operation for bundle {} on target {}",
                new Object[] { operation, bundleKey, target });

        OperationResult opResult = null;
        PersistedBundle info = null;
        try {
            info = persister.find(bundleKey);
            if (info != null) {
                bundleService.performOperation(info, operation, target);
                opResult = new OperationResultImpl(true, "Operation successfully performed", info.getBundleInfo());
            } else {
                opResult = new OperationResultImpl(false, "Bundle not found");
            }
        } catch (BundleException e) {
            if (BundleException.RESOLVE_ERROR == e.getType()) {
                opResult = new OperationResultImpl(false, "Bundle not found", info.getBundleInfo());
            } else {
                throw new ModuleManagementException(e);
            }
        } finally {
            logger.info("{} operation completed for bundle {} on target {} in {} ms. Opearation result: {}",
                    new Object[] { operation, bundleKey, target, System.currentTimeMillis() - startTime, opResult });
        }

        return opResult;
    }

    public void setPersister(BundlePersister persister) {
        this.persister = persister;
    }

    @Override
    public OperationResult start(String bundleKey, String target) {
        return performOperation(bundleKey, Operation.START, target);
    }

    @Override
    public OperationResult stop(String bundleKey, String target) {
        return performOperation(bundleKey, Operation.STOP, target);
    }

    @Override
    public OperationResult uninstall(String bundleKey, String target) {
        return performOperation(bundleKey, Operation.UNINSTALL, target);
    }

    public void setBundleService(BundleService bundleService) {
        this.bundleService = bundleService;
    }
}
