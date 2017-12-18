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
package org.jahia.services.content.decorator;

import org.slf4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;

/**
 * 
 * User: toto
 * Date: Dec 8, 2008
 * Time: 2:29:11 PM
 * 
 */
public class JCRQueryNode extends JCRNodeDecorator {
    protected static final Logger logger = org.slf4j.LoggerFactory.getLogger(JCRNodeWrapper.class);

    public JCRQueryNode(JCRNodeWrapper node) {
        super(node);
    }

    /*public NodeIterator getNodes() {
        try {
            final Query q = getSession().getWorkspace().getQueryManager().getQuery(getRealNode());
            final QueryResult qr = q.execute();
            final NodeIterator ni = qr.getNodes();
            if (ni != null) {
                // TODO try to do it with index aggregates in Jackrabbit's indexing configuration
                final List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
                while (ni.hasNext()) {
                    JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
                    if (Constants.JCR_CONTENT.equals(n.getName())) {
                        try {
                            n = n.getParent();
                        } catch (ItemNotFoundException e) {
                            // keep same node
                        }
                    }
                    list.add(n);
                }
                return new NodeIteratorImpl(list.iterator(), 0);
            }
        } catch (RepositoryException e) {
            logger.debug("Cannot execute query", e);
        }
        return new NodeIteratorImpl(new ArrayList<JCRNodeWrapper>().iterator(), 0);
    }*/


}
