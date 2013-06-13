/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
 * @author : cedric.mailleux@jahia.com
 * @since  : JAHIA 6.7
 * Created : 13/06/13
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
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, resource.getNode().getSession().getWorkspace().getName(), null, new JCRCallback<Object>() {
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
