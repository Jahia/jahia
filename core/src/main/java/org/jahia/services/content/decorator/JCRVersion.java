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
package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.version.Version;
import java.util.Calendar;

/**
 *
 * User: toto
 * Date: Mar 16, 2009
 * Time: 3:51:32 PM
 *
 */
public class JCRVersion extends JCRNodeDecorator implements Version {
    public JCRVersion(JCRNodeWrapper node) {
        super(node);
    }

    public Version getRealNode() {
        return (Version) super.getRealNode();
    }

    public JCRVersionHistory getContainingHistory() throws RepositoryException {
        return (JCRVersionHistory) getProvider().getNodeWrapper(getRealNode().getContainingHistory(), getSession());
    }

    public Calendar getCreated() throws RepositoryException {
        return getRealNode().getCreated();
    }

    public JCRVersion[] getSuccessors() throws RepositoryException {
        Version[] versions = getRealNode().getSuccessors();
        JCRVersion[] jcrversions = new JCRVersion[versions.length];
        for (int i = 0; i < versions.length; i++) {
            jcrversions[i] = (JCRVersion) getProvider().getNodeWrapper(versions[i], getSession());
        }
        return jcrversions;
    }

    public JCRVersion[] getPredecessors() throws RepositoryException {
        Version[] versions = getRealNode().getPredecessors();
        JCRVersion[] jcrversions = new JCRVersion[versions.length];
        for (int i = 0; i < versions.length; i++) {
            jcrversions[i] = (JCRVersion) getProvider().getNodeWrapper(versions[i], getSession());
        }
        return jcrversions;
    }

    public JCRNodeWrapper getFrozenNode() throws RepositoryException {
        return getProvider().getNodeWrapper(getRealNode().getFrozenNode(), getSession());
    }

    public JCRVersion getLinearSuccessor() throws RepositoryException {
        Version linearSuccessor = getRealNode().getLinearSuccessor();
        return linearSuccessor != null ? (JCRVersion) getProvider().getNodeWrapper(linearSuccessor, (JCRSessionWrapper) getSession()) : null;
    }

    public JCRVersion getLinearPredecessor() throws RepositoryException {
        final Version linearPredecessor = getRealNode().getLinearPredecessor();
        return linearPredecessor != null ? (JCRVersion) getProvider().getNodeWrapper(linearPredecessor, getSession()) : null;
    }

    @Override
    public Lock getLock() throws RepositoryException {
        throw new LockException("Version node are not locakble");
    }

    @Override
    public void checkout() throws RepositoryException {
        throw new UnsupportedRepositoryOperationException("Version node could not be checkout");
    }
}
