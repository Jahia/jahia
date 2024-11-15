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

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * TODO Comment me
 *
 * @author toto
 */
public class JCRReferenceNode extends JCRNodeDecorator {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRReferenceNode.class);

    public JCRReferenceNode(JCRNodeWrapper node) {
        super(node);
    }

    public Node getNode() throws RepositoryException {
        if (hasProperty(Constants.NODE)) {
            return getProperty(Constants.NODE).getNode();
        }
        return null;
    }

    public void setNode(JCRNodeWrapper node) throws RepositoryException {
        setProperty(Constants.NODE, node);
    }

    @Override
    public String getDisplayableName() {
        String name = super.getDisplayableName();
        try {
            if (getName().equals(name) && getNode() != null && !this.getIdentifier().equals(getNode().getIdentifier())) {
                name = ((JCRNodeWrapper) getNode()).getDisplayableName();
            }
        } catch (RepositoryException e) {
            logger.warn("JCRReferenceNode : error while trying to display reference " + this.getPath());
        }
        return name;
    }

    /**
     * return the referenced node, null if the reference cannot be resolved.
     * @return the referenced node
     * @throws RepositoryException in case of JCR-related errors
     */
    public Node getContextualizedNode() throws RepositoryException {
        if (hasProperty(Constants.NODE)) {
            try {
                return getProperty(Constants.NODE).getContextualizedNode();
            } catch (ItemNotFoundException e) {
                return null;
            }
        }
        return null;
    }
}
