/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.query.lucene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.query.qom.ColumnImpl;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DX specific query result with a single column implementation.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaSingleColumnQueryResult extends SingleColumnQueryResult {

    private static final FieldSelector ACL_FIELD_SELECTOR = new FieldSelector() {
        private static final long serialVersionUID = 2021640917456446047L;

        public FieldSelectorResult accept(String fieldName) {
            return JahiaNodeIndexer.ACL_UUID == fieldName ? FieldSelectorResult.LOAD : FieldSelectorResult.NO_LOAD;
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(JahiaSingleColumnQueryResult.class);

    private Map<String, Boolean> checkedAcls;

    public JahiaSingleColumnQueryResult(SearchIndex index, SessionContext sessionContext, AbstractQueryImpl queryImpl,
            Query query, SpellSuggestion spellSuggestion, ColumnImpl[] columns, Path[] orderProps, boolean[] orderSpecs,
            String[] orderFuncs, boolean documentOrder, long offset, long limit) throws RepositoryException {
        super(index, sessionContext, queryImpl, query, spellSuggestion, columns, orderProps, orderSpecs, orderFuncs,
                documentOrder, offset, limit);
    }

    private boolean canRead(ScoreNode node, IndexReader indexReader) throws RepositoryException, IOException {
        Fieldable aclUuidField = indexReader.document(node.getDoc(indexReader), ACL_FIELD_SELECTOR).getFieldable(JahiaNodeIndexer.ACL_UUID);
        String aclUuid = aclUuidField != null ? aclUuidField.stringValue() : null;
        if (aclUuid != null && aclUuid.length() == 0) {
            logger.warn("Empty ACL UUIDs in index for node: {}", node.getNodeId());
            aclUuid = null;
        }
        return JahiaLuceneQueryFactoryImpl.checkIndexedAcl(checkedAcls, aclUuid, sessionContext.getSessionImpl());
    }

    private boolean checkGranted(ScoreNode[] nodes, IndexReader indexReader) throws RepositoryException {
        for (ScoreNode node : nodes) {
            try {
                if (node != null && !canRead(node, indexReader)) {
                    return false;
                }
            } catch (ItemNotFoundException e) {
                // node deleted while query was executed
            } catch (IOException e) {
                logger.warn("Unable to read nodes's ACL UUID from index. Cause: " + e.getMessage(), e);
            }
        }
        return true;
    }

    @Override
    protected boolean isAccessGranted(ScoreNode[] nodes, MultiColumnQueryHits hits) throws RepositoryException {
        if (hits instanceof IndexReaderAware) {
            if (checkedAcls == null) {
                checkedAcls = new HashMap<>();
            }
            return checkGranted(nodes, ((IndexReaderAware) hits).getReader());
        } else {
            return super.isAccessGranted(nodes, hits);
        }
    }

}
