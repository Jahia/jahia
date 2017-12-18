/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import static org.jahia.api.Constants.JAHIAMIX_REFERENCES_IN_FIELD;
import static org.jahia.api.Constants.JAHIA_REFERENCE_IN_FIELD_PREFIX;

/**
 * Checks if the node is a jmix:referencesInField and add references to other content items (links in rich text
 * fields) as dependencies.
 *
 * @author cedric.mailleux@jahia.com
 * @since  JAHIA 7.0
 */
public class ReferencesToDependenciesFilter extends AbstractFilter {
    private static Logger logger = LoggerFactory.getLogger(ReferencesToDependenciesFilter.class);

    /**
     * @param renderContext The render context
     * @param resource      The resource to render
     * @param chain         The render chain
     * @return Content to stop the chain, or null to continue
     * @throws Exception
     */
    @Override
    public String prepare(RenderContext renderContext, final Resource resource, RenderChain chain) throws Exception {
        if (resource.getNode().isNodeType(JAHIAMIX_REFERENCES_IN_FIELD)) {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, resource.getNode().getSession().getWorkspace().getName(), null, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    NodeIterator ni = session.getNodeByIdentifier(resource.getNode().getIdentifier()).getNodes(JAHIA_REFERENCE_IN_FIELD_PREFIX);
                    while (ni.hasNext()) {
                        JCRNodeWrapper ref = (JCRNodeWrapper) ni.nextNode();
                        try {
                            resource.getDependencies().add(ref.getProperty("j:reference").getNode().getPath());
                        } catch (PathNotFoundException e) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("j:reference property is not found on node {}", ref.getCanonicalPath());
                            }
                        } catch (RepositoryException e) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("referenced node does not exist anymore {}", ref.getCanonicalPath());
                            }
                        } catch (Exception e) {
                            logger.warn("Error adding dependency to node " + resource.getNode().getCanonicalPath(), e);
                        }
                    }
                    return null;
                }
            });
        }
        return null;
    }
}
