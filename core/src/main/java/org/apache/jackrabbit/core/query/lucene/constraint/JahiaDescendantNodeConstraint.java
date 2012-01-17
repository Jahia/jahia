/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

//package org.apache.jackrabbit.core.query.lucene.constraint;
//
//import java.io.IOException;
//
//import javax.jcr.RepositoryException;
//
//import org.apache.jackrabbit.core.NodeImpl;
//import org.apache.jackrabbit.core.SessionImpl;
//import org.apache.jackrabbit.core.id.NodeId;
//import org.apache.jackrabbit.core.query.lucene.ScoreNode;
//import org.apache.jackrabbit.core.query.lucene.constraint.DescendantNodeConstraint;
//import org.apache.jackrabbit.core.query.lucene.constraint.EvaluationContext;
//import org.apache.jackrabbit.spi.Name;
//import org.apache.jackrabbit.spi.commons.query.qom.DescendantNodeImpl;
//import org.apache.jackrabbit.spi.commons.query.qom.SelectorImpl;
//import org.jahia.api.Constants;
//
//public class JahiaDescendantNodeConstraint extends DescendantNodeConstraint {
//
//    public JahiaDescendantNodeConstraint(DescendantNodeImpl constraint, SelectorImpl selector) {
//        super(constraint, selector);
//    }
//
//    @Override
//    public boolean evaluate(ScoreNode[] row, Name[] selectorNames, EvaluationContext context)
//            throws IOException {
//        NodeId baseId = getBaseNodeId(context);
//        if (baseId == null) {
//            return false;
//        }
//        ScoreNode sn = row[getSelectorIndex(selectorNames)];
//        if (sn == null) {
//            return false;
//        }
//        NodeId id = sn.getNodeId();
//        SessionImpl session = context.getSession();
//        try {
//            NodeImpl foundNode = session.getNodeById(id);
//            NodeImpl parent = foundNode;
//            boolean firstIteration = true;
//            for (;;) {
//                // throws exception if there is no parent
//                parent = (NodeImpl) parent.getParent();
//                if (parent.getId().equals(baseId)) {
//                    if (firstIteration && foundNode.isNodeType(Constants.JAHIANT_TRANSLATION)) {
//                        return false;
//                    } else {
//                        return true;
//                    }
//                }
//                firstIteration = false;
//            }
//        } catch (RepositoryException e) {
//            return false;
//        }
//    }
//}
