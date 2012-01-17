/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.rules;

import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.rules.BaseBackgroundAction;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background action that invalidates output caches for the node or its parents.
 * 
 * @author Sergiy Shyrkov
 * @since JAHIA 6.6
 */
public class FlushCacheOnNodeBackgroundAction extends BaseBackgroundAction {

    private static Logger logger = LoggerFactory.getLogger(FlushCacheOnNodeBackgroundAction.class);

    private ModuleCacheProvider cacheProvider;

    private int startLevel;

    private int levelsUp;

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void executeBackgroundAction(JCRNodeWrapper node) {
        try {
            JCRNodeWrapper currentNode = node;
            for (int level = 0; level <= (startLevel + levelsUp); level++) {
                if (level >= startLevel) {
                    cacheProvider.invalidate(currentNode.getPath());
                }
                currentNode = currentNode.getParent();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
    }

    public void setLevelsUp(int endLevel) {
        this.levelsUp = endLevel;
    }
}
