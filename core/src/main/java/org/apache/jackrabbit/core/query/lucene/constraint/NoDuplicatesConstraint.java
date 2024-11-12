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
