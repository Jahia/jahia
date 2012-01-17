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
//import org.apache.jackrabbit.core.id.NodeId;
//import org.apache.jackrabbit.core.query.lucene.FieldNames;
//import org.apache.jackrabbit.core.query.lucene.JahiaNodeIndexer;
//import org.apache.jackrabbit.core.query.lucene.ScoreNode;
//import org.apache.jackrabbit.core.query.lucene.constraint.ChildNodeConstraint;
//import org.apache.jackrabbit.core.query.lucene.constraint.EvaluationContext;
//import org.apache.jackrabbit.spi.Name;
//import org.apache.jackrabbit.spi.commons.query.qom.ChildNodeImpl;
//import org.apache.jackrabbit.spi.commons.query.qom.SelectorImpl;
//import org.apache.lucene.document.Document;
//
//import javax.jcr.RepositoryException;
//import java.io.IOException;
//
///**
// * 
// * User: toto
// * Date: Aug 2, 2010
// * Time: 6:47:17 PM
// *
// */
//public class JahiaChildNodeConstraint extends ChildNodeConstraint {
//
//    public JahiaChildNodeConstraint(ChildNodeImpl constraint,
//                               SelectorImpl selector) throws RepositoryException {
//        super(constraint, selector);
//    }
//
//    @Override public boolean evaluate(ScoreNode[] row, Name[] selectorNames, EvaluationContext context)
//            throws IOException {
//        ScoreNode sn = row[getSelectorIndex(selectorNames)];
//        if (sn == null) {
//            return false;
//        }
//
//        sn.getDoc(context.getIndexReader());
//        Document doc = context.getIndexReader().document(sn.getDoc(context.getIndexReader()));
//        NodeId baseNode = getBaseNodeId(context);
//        if (baseNode == null) {
//            return false;
//        } else {
//            final String id = baseNode.toString();
//            String parentId = doc.get(FieldNames.PARENT);
//            String translatedNodeParentId = doc.get(JahiaNodeIndexer.TRANSLATED_NODE_PARENT);
//            return id.equals(translatedNodeParentId) || (id.equals(parentId) && translatedNodeParentId == null);
//        }
//    }
//}
