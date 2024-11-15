/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.UpdatableItemStateManager;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.core.version.*;
import org.apache.jackrabbit.spi.commons.name.NameConstants;

import javax.jcr.*;
import javax.jcr.version.Version;

/**
 * Jahia implementation for version manager
 */
public class JahiaVersionManagerImpl extends VersionManagerImpl {

    public JahiaVersionManagerImpl(SessionContext context, UpdatableItemStateManager stateMgr, HierarchyManager hierMgr) {
        super(context, stateMgr, hierMgr);
    }

    public void addPredecessor(String absPath, Version version)
            throws RepositoryException {

        NodeStateEx state = getNodeState((NodeImpl) session.getNode(absPath),
                ItemValidator.CHECK_LOCK | ItemValidator.CHECK_PENDING_CHANGES_ON_NODE | ItemValidator.CHECK_HOLD,
                Permission.VERSION_MNGMT);

        // check versionable
        if (!checkVersionable(state)) {
            throw new UnsupportedRepositoryOperationException("Node not full versionable: " + safeGetJCRPath(state));
        }

        NodeId versionId = ((VersionImpl) version).getNodeId();

        WriteOperation ops = startWriteOperation();
        try {

            // add version to jcr:predecessors list
            InternalValue[] vals = state.getPropertyValues(NameConstants.JCR_PREDECESSORS);
            InternalValue[] v = new InternalValue[vals.length + 1];
            for (int i = 0; i < vals.length; i++) {
                v[i] = InternalValue.create(vals[i].getNodeId());
            }
            v[vals.length] = InternalValue.create(versionId);
            state.setPropertyValues(NameConstants.JCR_PREDECESSORS, PropertyType.REFERENCE, v, true);

            state.store(false);
            ops.save();
        } catch (ItemStateException e) {
            throw new RepositoryException(e);
        } finally {
            ops.close();
        }
    }

    private NodeStateEx getNodeState(NodeImpl node, int options, int permissions)
            throws RepositoryException {
        try {
            if (options > 0 || permissions > 0) {
                context.getItemValidator().checkModify(node, options, permissions);
            }
            return new NodeStateEx(
                    stateMgr,
                    ntReg,
                    (NodeState) stateMgr.getItemState(node.getNodeId()),
                    node.getQName());
        } catch (ItemStateException e) {
            throw new RepositoryException(e);
        }
    }

}
