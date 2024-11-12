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
package org.jahia.services.content;

import javax.jcr.Node;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Jahia's wrapper of the JCR <code>javax.jcr.NodeIterator</code>.
 *
 * @author toto
 */
public class NodeIteratorImpl extends RangeIteratorImpl implements JCRNodeIteratorWrapper {

    public static final NodeIteratorImpl EMPTY = new NodeIteratorImpl(new LinkedList<JCRNodeWrapper>().iterator(), 0);

    public NodeIteratorImpl(Iterator<JCRNodeWrapper> iterator, long size) {
        super(iterator, size);
    }

    public Node nextNode() {
        return (Node) next();
    }

    @Override
    public Iterator<JCRNodeWrapper> iterator() {
        return new Iterator<JCRNodeWrapper>() {
            @Override
            public boolean hasNext() {
                return NodeIteratorImpl.this.hasNext();
            }

            @Override
            public JCRNodeWrapper next() {
                return (JCRNodeWrapper) NodeIteratorImpl.this.next();
            }

            @Override
            public void remove() {
                NodeIteratorImpl.this.remove();
            }
        };
    }
}
