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
