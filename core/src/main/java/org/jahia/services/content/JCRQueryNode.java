/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.apache.log4j.Logger;

import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.NodeIterator;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 8, 2008
 * Time: 2:29:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRQueryNode extends JCRNodeDecorator {
    protected static final Logger logger = Logger.getLogger(JCRNodeWrapper.class);

    public JCRQueryNode(JCRNodeWrapper node) {
        super(node);
    }

    public List<JCRNodeWrapper> getChildren() {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();

        try {
            Query q = getSession().getWorkspace().getQueryManager().getQuery(getRealNode());
//                Query q = provider.getService().getQueryManager(user).getQuery(this);
            QueryResult qr = q.execute();
            NodeIterator ni = qr.getNodes();
            while (ni.hasNext()) {
                Node node = ni.nextNode();
                if (node instanceof JCRNodeWrapper) {
                    JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) node;
                    list.add(nodeWrapper);
                } else {
                    JCRNodeWrapper child = getProvider().getNodeWrapper(node, (JCRSessionWrapper) getSession());
                    if (child.getException() == null) {
                        list.add(child);
                    }
                }
            }
            return list;
        } catch (RepositoryException e) {
            logger.debug("Cannot execute query",e);
        }
        return list;
    }


}
