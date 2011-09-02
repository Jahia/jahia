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
package org.jahia.modules.actions;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Background action that invalidates output caches for the node, having start/end date visibility condition.
 * 
 * @author rincevent
 * @since JAHIA 6.6
 * Created : 8/29/11
 */
public class FlushVisibilityConditionOnDeleteBackgroundAction implements BackgroundAction {
    private transient static Logger logger = LoggerFactory.getLogger(FlushVisibilityConditionOnDeleteBackgroundAction.class);
    private ModuleCacheProvider cacheProvider;

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public String getName() {
        return "flushVisibilityOnDelete";
    }

    public void executeBackgroundAction(JCRNodeWrapper node) {
        try {
            cacheProvider.invalidate(node.getParent().getPath());
            cacheProvider.invalidate(node.getParent().getParent().getPath());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
