/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.services.content;

import org.jahia.api.Constants;
import org.slf4j.Logger;

import javax.jcr.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Invocation handler to decorate the {@link javax.jcr.NodeIterator} instance of the query result in order to wrap each {@link javax.jcr.Node} into
 * {@link org.jahia.services.content.JCRNodeWrapper}.
 *
 * @author Sergiy Shyrkov
 */
public class NodeIteratorWrapper implements JCRNodeIteratorWrapper {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(NodeIteratorWrapper.class);

    private NodeIterator ni;
    private JCRNodeWrapper nextNode = null;
    private JCRSessionWrapper session;
    private JCRStoreProvider provider;

    public NodeIteratorWrapper(NodeIterator ni, final JCRSessionWrapper session, final JCRStoreProvider provider) {
        this.ni = ni;
        this.session = session;
        this.provider = provider;
        prefetchNext();
    }

    private void prefetchNext() {
        do {
            try {
                Node n = null;
                while (ni.hasNext() && (n == null)) {
                    n = ni.nextNode();
                    if (session.getVersionDate() != null || session.getVersionLabel() != null) {
                        try {
                            n = session.getNode(n.getPath());
                        } catch (PathNotFoundException e) {
                            n = null;
                        }
                    }
                }
                if (n != null) {
                    if (session.getLocale() != null && n.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                        try {
                            n = n.getParent();
                        } catch (ItemNotFoundException e) {
                            // keep same node
                        }
                    }
                    try {
                        nextNode = provider.getNodeWrapper(n, session);
                        break;
                    } catch (PathNotFoundException e) {
                        // continue
                    }
                } else {
                    nextNode = null;
                    break;
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                // continue
            }
        } while (true);
    }

    public void skip(long skipNum) {
        for (int i = 0; i< skipNum; i++) {
            prefetchNext();
        }
        if (nextNode == null) {
            throw new NoSuchElementException();
        }
    }

    public long getSize() {
        return ni.getSize();
    }

    public long getPosition() {
        return ni.getPosition() - (nextNode != null ? 1 : 0);
    }

    public Node nextNode() {
        return (Node) next();
    }

    public boolean hasNext() {
        return (nextNode != null);
    }

    public Object next() {
        return wrappedNext();
    }

    private JCRNodeWrapper wrappedNext() {
        final JCRNodeWrapper res = nextNode;
        if (res == null) {
            throw new NoSuchElementException();
        }
        prefetchNext();
        return res;
    }

    public void remove() {
        ni.remove();
    }

    @Override
    public Iterator<JCRNodeWrapper> iterator() {
        return new Iterator<JCRNodeWrapper>() {
            @Override
            public boolean hasNext() {
                return NodeIteratorWrapper.this.hasNext();
            }

            @Override
            public JCRNodeWrapper next() {
                return NodeIteratorWrapper.this.wrappedNext();
            }

            @Override
            public void remove() {
                NodeIteratorWrapper.this.remove();
            }
        };
    }
}
