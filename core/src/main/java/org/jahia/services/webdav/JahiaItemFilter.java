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
package org.jahia.services.webdav;

import org.apache.jackrabbit.j2ee.JahiaResourceFactoryImpl;
import org.apache.jackrabbit.webdav.simple.DefaultItemFilter;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;

/**
 * WebDAV resource filter that disables directory listing completely if activated in <code>jahia.properties</code> or delegates to the
 * {@link DefaultItemFilter} otherwise.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaItemFilter extends DefaultItemFilter {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaItemFilter.class);

    private Boolean directoryListingDisabled;

    public JahiaItemFilter() {
        super();
        init();
    }

    private void init() {
        try {
            directoryListingDisabled = Boolean
                    .valueOf(SettingsBean.getInstance().getPropertiesFile()
                            .getProperty("repositoryDirectoryListingDisabled", "false"));
        } catch (Exception e) {
            logger.error("Error initializing Jahia WebDAV item filter", e);
        }
    }

    public boolean isFilteredItem(Item item) {
        if (directoryListingDisabled) {
            return true;
        }
        NodeType nt = null;
        try {
            if (item.isNode()) {
                nt = ((Node) item).getPrimaryNodeType();
            } else {
                nt = ((Property) item).getDefinition().getDeclaringNodeType();
            }
        } catch (RepositoryException re) {
            logger.warn("Error filtering item " + item.toString(), re);
        }
        if (nt == null) {
            return super.isFilteredItem(item);
        }
        if (JahiaResourceFactoryImpl.isAllowed(nt)) {
            return super.isFilteredItem(item);
        }
        return true;
    }

    public boolean isFilteredItem(String name, Session session) {
        if (directoryListingDisabled) {
            return true;
        }
        return super.isFilteredItem(name, session);
    }

}
