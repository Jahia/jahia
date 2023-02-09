/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.slf4j.Logger;
import org.apache.jackrabbit.util.Text;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

/**
 *
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 6 juil. 2009
 */
public class JCRFrozenNode extends JCRNodeDecorator {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRFrozenNode.class);
    private JCRSiteNode site;

    public JCRFrozenNode(JCRNodeWrapper node) {
        super(node);
    }

    @Override
    public String getUrl() {
        String frozenPrimaryType = getPropertyAsString("jcr:frozenPrimaryType");
        if (frozenPrimaryType.equals(Constants.JAHIANT_FILE)) {
            try {
                return getProvider().getHttpPath()+"/"+getSession().getWorkspace().getName() + Text.escapePath(this.getPropertyAsString("j:fullpath") + "?v=" + getParent().getName());
            } catch (RepositoryException e) {
                logger.error("Error while retrieving fullpath property", e);
            }

        }
        return super.getUrl();
    }


    @Override
    public JCRSiteNode getResolveSite() throws RepositoryException {
        if (site != null) {
            return site;
        }

        try {
            String path = node.getProperty("j:fullpath").getValue().getString();
            if (path.startsWith("/sites/")) {
                return (site = new JCRSiteNode(getSession().getNode(path.substring(0, path.indexOf('/',7)))));
            }

            if (path.startsWith("/modules/")) {
                return (site = new JCRSiteNode(getSession().getNode(path.substring(0, path.indexOf('/',14)))));
            }
        } catch (ItemNotFoundException e) {
        }
        return null;
    }
}
