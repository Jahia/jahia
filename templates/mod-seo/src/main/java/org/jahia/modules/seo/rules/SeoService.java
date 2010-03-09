/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.modules.seo.rules;

import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;
import org.drools.spi.KnowledgeHelper;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.rules.NodeWrapper;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlManager;

/**
 * SEO service class for manipulating content URL mappings from the
 * right-hand-side (consequences) of rules.
 * 
 * @author Sergiy Shyrkov
 */
public class SeoService {

    private static Logger logger = Logger.getLogger(SeoService.class);

    private VanityUrlManager urlManager;

    /**
     * Adds the URL mapping for the specified node and language.
     * 
     * @param node the node to remove mappings from
     * @param locale the language code to remove mappings for
     * @param url the URL for the mapping
     * @param isDefault set the new mapping as default one
     * @param drools the rule engine helper class
     * @throws RepositoryException in case of an error
     */
    public void addMapping(final NodeWrapper node, final String locale, final String url, final boolean isDefault,
            KnowledgeHelper drools) throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding URL mapping for node " + node.getPath() + " and locale '" + locale + "'");
        }
        final String path = node.getPath();
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                urlManager.saveVanityUrlMapping(session.getNode(path), new VanityUrl(url, JCRContentUtils
                        .getSiteKey(path), locale, isDefault, true), session);
                return true;
            }
        });
    }

    /**
     * Removes all URL mappings for the specified node and language.
     * 
     * @param node the node to remove mappings from
     * @param locale the language code to remove mappings for
     * @param drools the rule engine helper class
     * @throws RepositoryException in case of an error
     */
    public void removeMappings(final NodeWrapper node, final String locale, KnowledgeHelper drools)
            throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing URL mappings for locale '" + locale + "' from node " + node.getPath());
        }
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                urlManager.removeVanityUrlMappings(session.getNode(node.getPath()), locale, session);
                return true;
            }
        });
    }

    /**
     * Injects an instance of the {@link VanityUrlManager}.
     * 
     * @param urlManager an instance of the {@link VanityUrlManager}
     */
    public void setUrlManager(VanityUrlManager urlManager) {
        this.urlManager = urlManager;
    }

}