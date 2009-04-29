/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
                JCRNodeWrapper child = getProvider().getNodeWrapper(node, (JCRSessionWrapper) getSession());
                if (child.getException () == null) {
                    list.add (child);
                }
            }
            return list;
        } catch (RepositoryException e) {
            logger.debug("Cannot execute query",e);
        }
        return list;
    }


}
