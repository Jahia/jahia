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
package org.apache.jackrabbit.core.query.lucene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.query.qom.ColumnImpl;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
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
        Field aclUuidField = indexReader.document(node.getDoc(indexReader), ACL_FIELD_SELECTOR)
                .getField(JahiaNodeIndexer.ACL_UUID);
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
                checkedAcls = new HashMap<String, Boolean>();
            }
            return checkGranted(nodes, ((IndexReaderAware) hits).getReader());
        } else {
            return super.isAccessGranted(nodes, hits);
        }
    }
}
