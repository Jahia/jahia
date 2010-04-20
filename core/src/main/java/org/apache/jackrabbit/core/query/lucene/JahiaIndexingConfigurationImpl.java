package org.apache.jackrabbit.core.query.lucene;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;

public class JahiaIndexingConfigurationImpl extends IndexingConfigurationImpl {

    public JahiaIndexingConfigurationImpl() {
        super();
    }
    
    private final Analyzer facetAnalyzer = new KeywordAnalyzer();
    
    private static final String FACET_EXPRESSION = ":" + JahiaNodeIndexer.FACET_PREFIX;
    
    /**
     * Returns the analyzer configured for the property with this fieldName
     * (the string representation ,JCR-style name, of the given <code>Name</code>
     * prefixed with <code>FieldNames.FULLTEXT_PREFIX</code>)),
     * and <code>null</code> if none is configured, or the configured analyzer
     * cannot be found. If <code>null</code> is returned, the default Analyzer
     * is used.
     *
     * @param fieldName the string representation ,JCR-style name, of the given <code>Name</code>
     * prefixed with <code>FieldNames.FULLTEXT_PREFIX</code>))
     * @return the <code>analyzer</code> to use for indexing this property
     */
    public Analyzer getPropertyAnalyzer(String fieldName) {
        Analyzer analyzer = StringUtils.contains(fieldName, FACET_EXPRESSION) ? facetAnalyzer
                : super.getPropertyAnalyzer(fieldName); 
        return analyzer;
    }

}
