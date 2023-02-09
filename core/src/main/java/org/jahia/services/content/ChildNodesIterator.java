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
package org.jahia.services.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator used to filter translation and mount nodes
 * wrapping nodes on demand with a lazy size
 */
public class ChildNodesIterator implements JCRNodeIteratorWrapper {

    private static final Logger logger = LoggerFactory.getLogger(ChildNodesIterator.class);

    private NodeIterator ni;
    private List<String> mountNames;
    private List<JCRNodeWrapper> mountNodes;
    private JCRNodeWrapperImpl parent;
    private JCRSessionWrapper session;
    private JCRStoreProvider provider;
    private JCRNodeWrapper nextNode;
    private long position;
    private List<JCRNodeWrapper> cache = new ArrayList<>();

    public ChildNodesIterator(NodeIterator ni, List<String> mountNames, List<JCRNodeWrapper> mountNodes,
                              JCRNodeWrapperImpl parent, JCRSessionWrapper session, JCRStoreProvider provider) {
        this.ni = ni;
        this.mountNames = mountNames;
        this.mountNodes = mountNodes;
        this.parent = parent;
        this.session = session;
        this.provider = provider;
        this.position = 0;
        prefetchNext();
    }

    private JCRNodeWrapper loadNextInCache() {
        do {
            try {
                if (mountNodes != null && !mountNodes.isEmpty()) {
                    JCRNodeWrapper wrapper = mountNodes.remove(0);
                    cache.add(wrapper);
                    return wrapper;
                }
                Node n = null;
                while (n == null && ni.hasNext()) {
                    n = ni.nextNode();
                    if ((session.getLocale() != null && n.getName().startsWith("j:translation_")) ||
                            (mountNames != null && mountNames.contains(n.getName()))) {
                        n = null;
                    }
                }
                if (n != null) {
                    try {
                        JCRNodeWrapper wrapper = provider.getNodeWrapper(n, parent.buildSubnodePath(n.getName()), parent, session);
                        cache.add(wrapper);
                        return wrapper;
                    } catch (PathNotFoundException e) {
                        // continue
                    }
                } else {
                    return null;
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                // continue
            }
        } while (true);
    }

    private void prefetchNext() {
        while (position >= cache.size() && loadNextInCache() != null) {}
        if (position < cache.size()) {
            nextNode = cache.get((int) position);
            position++;
        } else {
            nextNode = null;
        }
    }

    @Override
    public void skip(long skipNum) {
        for (int i = 0; i< skipNum; i++) {
            if (nextNode == null) {
                throw new NoSuchElementException();
            }
            prefetchNext();
        }
    }

    @Override
    public long getSize() {
        while (loadNextInCache() != null) {}
        return cache.size();
    }

    @Override
    public long getPosition() {
        return hasNext() ? (position - 1) : position;
    }

    @Override
    public Node nextNode() {
        return (Node) next();
    }

    @Override
    public boolean hasNext() {
        return (nextNode != null);
    }

    @Override
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

    @Override
    public void remove() {
        if (mountNodes != null && !mountNodes.isEmpty()) {
            mountNodes.remove(0);
        } else {
            ni.remove();
        }
    }

    @Override
    public Iterator<JCRNodeWrapper> iterator() {
        return new Iterator<JCRNodeWrapper>() {
            @Override
            public boolean hasNext() {
                return ChildNodesIterator.this.hasNext();
            }

            @Override
            public JCRNodeWrapper next() {
                return ChildNodesIterator.this.wrappedNext();
            }

            @Override
            public void remove() {
                ChildNodesIterator.this.remove();
            }
        };
    }
}
