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
