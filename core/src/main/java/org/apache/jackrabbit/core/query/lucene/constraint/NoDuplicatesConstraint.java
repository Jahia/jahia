/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.query.lucene.constraint;

import org.apache.jackrabbit.core.query.lucene.FieldNames;
import org.apache.jackrabbit.core.query.lucene.JahiaNodeIndexer;
import org.apache.jackrabbit.core.query.lucene.ScoreNode;
import org.apache.jackrabbit.spi.Name;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
* Filters out all duplicate results, including multiple translation of the same node
*/
public class NoDuplicatesConstraint implements Constraint {
    private Set<String> ids = new HashSet<String>();

    public boolean evaluate(ScoreNode[] row, Name[] selectorNames, EvaluationContext context)
            throws IOException {
        StringBuilder idBuilder = new StringBuilder(1024);
        for (ScoreNode sn : row) {
            if (sn == null) {
                idBuilder.append("null");
            } else {
                int docNb = sn.getDoc(context.getIndexReader());
                Document doc = context.getIndexReader().document(docNb);
                if (doc.getField(JahiaNodeIndexer.TRANSLATED_NODE_PARENT) != null) {
                    idBuilder.append(doc.getField(FieldNames.PARENT).stringValue());
                } else {
                    idBuilder.append(sn.getNodeId().toString());
                }
            }
        }

        final String id = idBuilder.toString();
        return ids.add(id);
    }
}
