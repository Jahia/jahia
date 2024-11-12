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
