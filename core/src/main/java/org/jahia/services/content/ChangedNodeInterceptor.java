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
package org.jahia.services.content;

import org.jahia.services.content.interceptor.BaseInterceptor;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

public class ChangedNodeInterceptor extends BaseInterceptor {

    @Override
    public Value beforeSetValue(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition,
                                Value originalValue) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        JCRSessionWrapper session = node.getSession();
        JCRNodeWrapper changedNode = session.getNodeByIdentifier(node.getIdentifier()); // re-getting the node to eventually have the decorator
        session.getCache().putNode(changedNode.getPath(), changedNode, true);
        return super.beforeSetValue(node, name, definition, originalValue);
    }

    @Override
    public Value[] beforeSetValues(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition,
                                   Value[] originalValues) throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        JCRSessionWrapper session = node.getSession();
        JCRNodeWrapper changedNode = session.getNodeByIdentifier(node.getIdentifier()); // re-getting the node to eventually have the decorator
        session.getCache().putNode(changedNode.getPath(), changedNode, true);
        return super.beforeSetValues(node, name, definition, originalValues);
    }

    @Override
    public void beforeRemove(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition)
            throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        JCRSessionWrapper session = node.getSession();
        JCRNodeWrapper changedNode = session.getNodeByIdentifier(node.getIdentifier()); // re-getting the node to eventually have the decorator
        session.getCache().putNode(changedNode.getPath(), changedNode, true);
        super.beforeRemove(node, name, definition);
    }
}
