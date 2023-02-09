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
