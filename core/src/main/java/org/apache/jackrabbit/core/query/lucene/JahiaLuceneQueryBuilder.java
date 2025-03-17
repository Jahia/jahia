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

import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.query.PropertyTypeRegistry;
import org.apache.jackrabbit.core.state.ItemStateManager;
import org.apache.jackrabbit.spi.commons.query.QueryRootNode;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;

/**
 * <code>JahiaLuceneQueryBuilder</code> extends the standard Lucene query builder
 * from Jackrabbit and adds Jahia-specific customizations, supporting dual analyzers for
 * internationalization (i18n) purposes.
 */
public class JahiaLuceneQueryBuilder extends LuceneQueryBuilder {

    private final Analyzer i18nAnalyzer;
    private final Analyzer analyzer;
    private final SynonymProvider synonymProvider;
    private final PerQueryCache cache;

    /**
     * Constructs a new <code>JahiaLuceneQueryBuilder</code> instance.
     *
     * @param root the root of the query tree
     * @param session the session
     * @param sharedItemMgr the shared item state manager
     * @param hmgr the hierarchy manager
     * @param nsMappings the namespace mappings
     * @param i18nAnalyzer the optional internationalization analyzer to use for parsing, can be null
     * @param analyzer the default analyzer to use for parsing
     * @param propReg the property type registry
     * @param synonymProvider the synonym provider
     * @param indexFormatVersion the index format version
     * @param cache the per-query cache
     */
    protected JahiaLuceneQueryBuilder(QueryRootNode root, SessionImpl session, ItemStateManager sharedItemMgr, HierarchyManager hmgr,
                                      NamespaceMappings nsMappings, Analyzer i18nAnalyzer, Analyzer analyzer, PropertyTypeRegistry propReg, SynonymProvider synonymProvider,
                                      IndexFormatVersion indexFormatVersion, PerQueryCache cache) {
        super(root, session, sharedItemMgr, hmgr, nsMappings, i18nAnalyzer != null ? i18nAnalyzer : analyzer, propReg, synonymProvider, indexFormatVersion, cache);
        this.i18nAnalyzer = i18nAnalyzer;
        this.analyzer = analyzer;
        this.synonymProvider = synonymProvider;
        this.cache = cache;
    }

    /**
     * Returns a custom query parser that supports dual analyzers for i18n.
     *
     * @param fieldName the default field for query terms
     * @return the custom query parser
     */
    @Override
    protected QueryParser getQueryParser(String fieldName) {
        return new JahiaDualAnalyzerQueryParser(fieldName, i18nAnalyzer, analyzer, synonymProvider, cache);
    }
}