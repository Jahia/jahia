/**
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.jahia.data.templates.ModuleState;
import org.jahia.services.modulemanager.persistence.ModuleInfoPersister.OCMCallback;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.model.ClusterNodeInfo;
import org.jahia.services.modulemanager.model.NodeBundle;
import org.jahia.services.modulemanager.model.NodeOperation;
import org.jahia.services.modulemanager.model.Operation;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Cluster node level operation processor, that is responsible for executing module operations on the current cluster node.
 * 
 * @author Sergiy Shyrkov
 */
public class NodeOperationProcessor extends BaseOperationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NodeOperationProcessor.class);

    private ClusterNodeInfo clusterNodeInfo;

    private String clusterNodePath;

    private String operationLogPath;

    private JahiaTemplateManagerService templateManagerService;
    
    /**
     * Performs the check if the operation can be started or not, which is based on the dependencies of it.
     * 
     * @param op
     *            the operation to check
     * @return <code>true</code> if the operation could be started; <code>false</code> otherwise
     * @throws RepositoryException
     */
    private boolean canStart(final NodeOperation op) throws RepositoryException {
        final List<String> dependsOn = op.getDependsOn();
        final String opPath = op.getPath();
        if (dependsOn == null || dependsOn.isEmpty()) {
            // this operation does not depend on others -> give it a go
            logger.info("No dependencies for operation {}. It can be started immediately.", opPath);
            return true;
        }
        logger.info("Checking prerequisites for operation {}", opPath);
        return persister.doExecute(new OCMCallback<Boolean>() {
            @Override
            public Boolean doInOCM(ObjectContentManager ocm) throws RepositoryException {
                for (String dependency : dependsOn) {
                    NodeOperation dependentOp = (NodeOperation) ocm.getObjectByUuid(dependency);
                    if (dependentOp == null) {
                        logger.warn("No dependent operation ({}) found for node operation {}", dependency, op);
                        continue;
                    }
                    if (!dependentOp.isCompleted()) {
                        logger.info("Operation {} is still waiting for the dependent one {} to be completed", opPath,
                                dependentOp);
                        return Boolean.FALSE;
                    }
                }
                logger.info("Operation {} can be started", opPath);
                return Boolean.TRUE;
            }
        });
    }

    protected void completeGlobalOperationFor(NodeOperation op, ObjectContentManager ocm) {
        // update the state of the global operation
        Operation globalOp = op.getOperation();
        globalOp.setState(op.getState());
        globalOp.setInfo(op.getInfo());
        ocm.update(globalOp);

        // archive the global operation
        ocm.move(globalOp.getPath(), "/module-management/operationLog/" + globalOp.getName());
    }

    private void ensureNodeBundlePresent(NodeOperation op, ObjectContentManager ocm) throws PathNotFoundException {
        String bundleKey = op.getOperation().getBundle().getName();
        String path = clusterNodePath + "/bundles/" + bundleKey;
        NodeBundle nodeBundle = (NodeBundle) ocm.getObject(NodeBundle.class, path);

        if (nodeBundle == null) {
            throw new ModuleManagementException("Bundle " + bundleKey + " is not installed on the current node.");
        }
    }

    private void performAction(Bundle osgiBundle, String action) throws ModuleManagementException {
        try {
            switch (action) {
                case "start":
                    osgiBundle.start();
                    break;
                case "stop":
                    osgiBundle.stop();
                    ;
                    break;
                case "uninstall":
                    osgiBundle.uninstall();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown bundle action " + action);
            }
        } catch (BundleException e) {
            throw new ModuleManagementException("Error performing action " + action + " on a bundle "
                    + osgiBundle.getSymbolicName() + "-" + osgiBundle.getVersion() + ". Cause: " + e.getMessage(), e);
        }
    }

    protected boolean performAction(NodeOperation op, ObjectContentManager ocm)
            throws PathNotFoundException, ModuleManagementException {
        boolean success = true;
        long startTime = System.currentTimeMillis();
        logger.info("Start performing node operation {}", logger.isDebugEnabled() ? op : op.getPath());

        String action = op.getOperation().getAction();

        if (action.equals("install")) {
            performActionInstall(op, ocm);
        } else {
            ensureNodeBundlePresent(op, ocm);

            org.jahia.services.modulemanager.model.Bundle b = op.getOperation().getBundle();

            Bundle osgiBundle = BundleUtils.getBundle(b.getSymbolicName(), b.getVersion());
            if (osgiBundle == null) {
                throw new ModuleManagementException(
                        "Bundle " + op.getOperation().getBundle().getName() + " is not installed on the current node.");
            }

            performAction(osgiBundle, action);
            String clusterBundlePath = clusterNodePath + "/bundles/" + b.getName();
            if ("uninstall".equals(action)) {
                ocm.remove(clusterBundlePath);
            } else {
                NodeBundle nodeBundle = new NodeBundle(b.getName());
                nodeBundle.setPath(clusterBundlePath);
                nodeBundle.setBundle(b);
                ModuleState moduleState = templateManagerService.getModuleStates().get(osgiBundle);
                nodeBundle.setState(moduleState != null ? moduleState.getState().toString().toLowerCase() : "failed");
                ocm.update(nodeBundle);
            }
        }

        logger.info("Done performing node operation {} with status {} in {} ms",
                new Object[] { logger.isDebugEnabled() ? op : op.getPath(), success ? "success" : "failure",
                        System.currentTimeMillis() - startTime });
        return success;
    }

    private boolean performActionInstall(NodeOperation op, ObjectContentManager ocm) {
        boolean success = true;
        long startTime = System.currentTimeMillis();
        logger.info("Start performing node operation {}", logger.isDebugEnabled() ? op : op.getPath());

        org.jahia.services.modulemanager.model.Bundle b = op.getOperation().getBundle();
        String nodeBundlePath = clusterNodePath + "/bundles/" + b.getName();

        Bundle osgiBundle = BundleUtils.getBundle(b.getSymbolicName(), b.getVersion());
        InputStream is = null;
        try {
            is = b.getFile().getUrl().openStream();
            if (osgiBundle == null) {
                // installing new bundle
                osgiBundle = FrameworkService.getBundleContext().installBundle("jcr:" + b.getName(), is);
            } else {
                // updating existing one
                osgiBundle.update(is);
            }
            NodeBundle nodeBundle = new NodeBundle(b.getName());
            nodeBundle.setPath(nodeBundlePath);
            nodeBundle.setBundle(b);
            ModuleState moduleState = templateManagerService.getModuleStates().get(osgiBundle);
            nodeBundle.setState(moduleState != null ? moduleState.getState().toString().toLowerCase() : "unknown");
            ocm.insert(nodeBundle);
        } catch (IOException | BundleException e) {
            throw new ModuleManagementException(
                    "Error performing install action on a bundle " + b.getName() + ". Cause: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        logger.info("Done performing node operation {} with status {} in {} ms",
                new Object[] { logger.isDebugEnabled() ? op : op.getPath(), success ? "success" : "failure",
                        System.currentTimeMillis() - startTime });

        return success;
    }

    /**
     * Starts the operation by changing its state and processing the required action.
     * 
     * @param op
     *            the operation to be started
     * @throws RepositoryException
     *             in case of errors
     */
    private void processOperation(final NodeOperation op) throws RepositoryException {
        persister.doExecute(new OCMCallback<Void>() {
            @Override
            public Void doInOCM(ObjectContentManager ocm) throws RepositoryException {
                // update operation state to "processing"
                op.setState("processing");
                ocm.update(op);
                ocm.save();

                try {
                    // execute the action and update operation state depending on the result
                    op.setState(performAction(op, ocm) ? "successful" : "failed");
                } catch (Exception e) {
                    // change the state of the operation to failed, providing the failure cause
                    op.setState("failed");
                    op.setInfo("Cause: " + ExceptionUtils.getMessage(e) + "\nRoot cause: "
                            + ExceptionUtils.getRootCauseMessage(e) + "\n" + ExceptionUtils.getFullStackTrace(e));

                    // re-throw the cause
                    if (e instanceof RepositoryException) {
                        throw (RepositoryException) e;
                    } else if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw new RuntimeException(e);
                    }
                } finally {
                    ocm.update(op);

                    if (clusterNodeInfo.isProcessingServer() && !op.getOperation().isCompleted()) {
                        // we have to complete the global operation as this node is the last one in processing chain
                        completeGlobalOperationFor(op, ocm);
                    }

                    // archive this node operation
                    ocm.move(op.getPath(), new StringBuilder(operationLogPath.length() + op.getName().length())
                            .append(operationLogPath).append(op.getName()).toString());
                    ocm.save();
                }

                return null;
            }

        });
    }

    /**
     * Checks for the next open operation and processes it.
     * 
     * @throws ModuleManagementException
     *             in case of an error
     */
    protected boolean processSingleOperation() throws ModuleManagementException {
        boolean processed = false;
        logger.debug("Checking for available node-level module operations");
        try {
            NodeOperation op = persister.getNextNodeOperation(clusterNodeInfo.getId());
            if (op == null) {
                // no operations to be processed found -> return
                logger.debug("No node-level module operations to be processed found");
            } else if ("open".equals(op.getState())) {
                // we can start the operation now
                logger.info("Found open node-level module operation to be started: {}",
                        logger.isDebugEnabled() ? op : op.getPath());
                long startTime = System.currentTimeMillis();
                if (canStart(op)) {
                    processOperation(op);
                    logger.info("Node-level module operation {} processed in {} ms", op.getPath(),
                            System.currentTimeMillis() - startTime);
                    processed = true;
                }
            } else {
                // schedule processing
                tryLater();
            }
        } catch (RepositoryException e) {
            throw new ModuleManagementException(e);
        }

        return processed;
    }

    public void setClusterNodeInfo(ClusterNodeInfo clusterNodeInfo) {
        this.clusterNodeInfo = clusterNodeInfo;
        clusterNodePath = clusterNodeInfo != null ? "/module-management/nodes/" + clusterNodeInfo.getId() : null;
        operationLogPath = clusterNodeInfo != null ? clusterNodePath + "/operationLog/" : null;
    }

    /**
     * Injects an instance of the template management service.
     * 
     * @param templateManagerService
     *            an instance of the corresponding service
     */
    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }
}
