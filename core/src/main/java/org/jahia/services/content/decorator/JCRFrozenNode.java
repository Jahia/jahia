/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
