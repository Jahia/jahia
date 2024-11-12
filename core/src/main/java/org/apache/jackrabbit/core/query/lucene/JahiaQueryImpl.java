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
package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.core.query.PropertyTypeRegistry;
import org.apache.jackrabbit.core.query.lucene.constraint.Constraint;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.query.QueryNodeFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.jahia.api.Constants;
import org.jahia.utils.LuceneUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;

public class JahiaQueryImpl extends QueryImpl {
    public static boolean checkAclUuidInIndex = Boolean
            .valueOf(System.getProperty("jahia.jackrabbit.query.xpath.checkAclUuidInIndex", "true"));

    private Constraint constraint = null;
    private String statement = null;

    public JahiaQueryImpl(SessionContext sessionContext, SearchIndex index,
                          PropertyTypeRegistry propReg, String statement, String language,
                          QueryNodeFactory factory) throws InvalidQueryException {
        super(sessionContext, index, propReg, statement, language, factory);
        this.statement = statement;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

    @Override
    public boolean needsSystemTree() {
        return statement.contains(Constants.JCR_SYSTEM);
    }

    @Override
    protected Analyzer getTextAnalyzer() {
        final String lang = LuceneUtils.extractLanguageOrNullFromStatement(statement);

        if (lang != null) {
            final Analyzer analyzer = index.getAnalyzerRegistry().getAnalyzer(lang);
            return analyzer != null ? analyzer : super.getTextAnalyzer();
        }

        return super.getTextAnalyzer();
    }

    @Override
    protected QueryResult createQueryResult(long offset, long limit, Query query, Path[] orderProperties,
            boolean[] ascSpecs, String[] orderFuncs) throws RepositoryException {
        if (checkAclUuidInIndex && JahiaSearchIndex.isAclUuidInIndex(index)) {
            // if ACLs are indexed, we use our own implementation of the query result to benefit from "fast" ACL checks
            return new JahiaSingleColumnQueryResult(
                    index, sessionContext, this, query,
                    new SpellSuggestion(index.getSpellChecker(), root),
                    getColumns(), orderProperties, ascSpecs, orderFuncs,
                    orderProperties.length == 0 && getRespectDocumentOrder(),
                    offset, limit);
        } else {
            return super.createQueryResult(offset, limit, query, orderProperties, ascSpecs, orderFuncs);
        }
    }
}
