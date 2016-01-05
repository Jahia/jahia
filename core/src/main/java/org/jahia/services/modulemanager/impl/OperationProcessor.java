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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.jahia.services.modulemanager.persistence.ModuleInfoPersister.OCMCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.model.ClusterNode;
import org.jahia.services.modulemanager.model.NodeOperation;
import org.jahia.services.modulemanager.model.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.LinkedList;
import java.util.List;

/**
 * Module global operation processor, that is responsible for controlling the operation lifecycle and creating corresponding cluster-node
 * level operations.
 * 
 * @author Sergiy Shyrkov
 */
public class OperationProcessor extends BaseOperationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OperationProcessor.class);

    private NodeOperation createNodeOperation(Operation op, ClusterNode cn, ObjectContentManager ocm)
            throws PathNotFoundException, RepositoryException {
        NodeOperation nodeOp = new NodeOperation();
        nodeOp.setName(JCRContentUtils.findAvailableNodeName(ocm.getSession().getNode(cn.getPath() + "/operations"),
                op.getName()));
        nodeOp.setPath(cn.getPath() + "/operations/" + nodeOp.getName());
        nodeOp.setOperation(op);

        return nodeOp;
    }

    private void createNodeOperations(final Operation op, ObjectContentManager ocm)
            throws PathNotFoundException, RepositoryException {
        List<NodeOperation> dependsOn = new LinkedList<>();

        List<ClusterNode> clusterNodes = persister.getClusterNodes(ocm);

        // first iterate over non-processing nodes
        for (ClusterNode cn : clusterNodes) {
            if (cn.isProcessingServer()) {
                continue;
            }
            NodeOperation nodeOp = createNodeOperation(op, cn, ocm);
            // TODO verify the way we detect if the node is currently started or not
            if (cn.isStarted()) {
                dependsOn.add(nodeOp);
            }
            ocm.insert(nodeOp);
        }

        // now the processing node
        for (ClusterNode cn : clusterNodes) {
            if (!cn.isProcessingServer()) {
                continue;
            }
            NodeOperation nodeOp = createNodeOperation(op, cn, ocm);
            if (!dependsOn.isEmpty()) {
                // we have to wait for active non-processing nodes, so store the list of dependent operations
                List<String> uuids = new LinkedList<>();
                for (NodeOperation dependency : dependsOn) {
                    uuids.add(dependency.getIdentifier());
                }
                nodeOp.setDependsOn(uuids);
            }
            ocm.insert(nodeOp);
        }

        ocm.save();
    }

    /**
     * Checks for the next open operation and starts it by changing its state and creating corresponding cluster node level operations.
     * 
     * @return <code>true</code> in case an open operation was started; <code>false</code> if no open operation are found or if there is
     *         another operation in progress already
     * @throws ModuleManagementException
     *             in case of an error
     */
    protected boolean processSingleOperation() throws ModuleManagementException {
        boolean processed = false;
        logger.debug("Checking for available module operations");
        try {
            Operation op = persister.getNextOperation();
            if (op == null) {
                // no operations to be processed found -> return
                logger.debug("No module operations to be processed found");
            } else if ("open".equals(op.getState())) {
                // we can start the operation now
                logger.info("Found open module operation to be started: {}", op);
                long startTime = System.currentTimeMillis();
                startOperation(op);
                logger.info("Module operation {} processed in {} ms", op.getName(),
                        System.currentTimeMillis() - startTime);
                processed = true;
            } else {
                // schedule processing
                tryLater();
            }
        } catch (RepositoryException e) {
            throw new ModuleManagementException(e);
        }

        return processed;
    }

    /**
     * Starts the operation by changing its state and creating corresponding cluster node level operations.
     * 
     * @param op
     *            the operation to be started
     * @throws RepositoryException
     *             in case of errors
     */
    private void startOperation(final Operation op) throws RepositoryException {
        persister.doExecute(new OCMCallback<Void>() {
            @Override
            public Void doInOCM(ObjectContentManager ocm) throws RepositoryException {
                // update operation state to "processing"
                op.setState("processing");
                ocm.update(op);
                ocm.save();

                try {
                    // delegate operation to cluster nodes
                    createNodeOperations(op, ocm);
                } catch (Exception e) {
                    // change the state of the operation to failed, providing the failure cause
                    op.setState("failed");
                    op.setInfo("Cause: " + ExceptionUtils.getMessage(e) + "\nRoot cause: "
                            + ExceptionUtils.getRootCauseMessage(e) + "\n" + ExceptionUtils.getFullStackTrace(e));
                    ocm.update(op);

                    // archive the operation
                    ocm.move(op.getPath(), "/module-management/operationLog");
                    ocm.save();

                    // re-throw the cause
                    if (e instanceof RepositoryException) {
                        throw (RepositoryException) e;
                    } else if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw new RuntimeException(e);
                    }
                }

                return null;
            }

        });
    }
}
