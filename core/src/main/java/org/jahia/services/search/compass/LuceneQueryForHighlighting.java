/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.search.compass;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.jahia.registries.ServicesRegistry;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 17 janv. 2007
 * Time: 15:19:12
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class LuceneQueryForHighlighting extends Query  {

    private Query query;
    private String fieldName;
    private boolean rewriteQueryWithFieldName;
    public LuceneQueryForHighlighting(Query query, String fieldName, boolean rewriteQueryWithFieldName){
        this.query = query;
        this.fieldName = fieldName;
        this.rewriteQueryWithFieldName = rewriteQueryWithFieldName;
    }

    /** Prints a query to a string, with <code>field</code> assumed to be the
    * default field and omitted.
    * <p>The representation used is one that is supposed to be readable
    * by {@link org.apache.lucene.queryParser.QueryParser QueryParser}. However,
    * there are the following limitations:
    * <ul>
    *  <li>If the query was created by the parser, the printed
    *  representation may not be exactly what was parsed. For example,
    *  characters that need to be escaped will be represented without
    *  the required backslash.</li>
    * <li>Some of the more complicated queries (e.g. span queries)
    *  don't have a representation that can be parsed by QueryParser.</li>
    * </ul>
    */
    public String toString(String field){
        return query.toString(field);
    }

    /**
     * Expert: adds all terms occuring in this query to the terms set. Only
     * works if this query is in its {@link #rewrite rewritten} form.
     *
     * @throws UnsupportedOperationException if this query is not yet rewritten
     */
    public void extractTerms(Set terms) {
        // needs to be implemented by query subclasses
        query.extractTerms(terms);
        if ( rewriteQueryWithFieldName ){
            List<Term> v = new ArrayList<Term>();
            for (Iterator<?> iter = terms.iterator(); iter.hasNext();)
            {
                Term term = (Term)iter.next();
                v.add(new Term(this.fieldName,term.text()));
            }
            terms.clear();
            terms.addAll(v);
        } else {
            Map<String, Set<String>> fieldsGrouping = ServicesRegistry.getInstance().getJahiaSearchService().getFieldsGrouping();
            if ( fieldsGrouping != null && this.fieldName != null ){
                Set<String> l = fieldsGrouping.get(fieldName);
                if (l != null && !l.isEmpty() ) {
                    Set<Term> termsSet = new HashSet<Term>();
                    for (Iterator<?> iter = terms.iterator(); iter.hasNext();)
                    {
                        Term term = (Term)iter.next();
                        if ( !l.contains(term.field()) && !this.fieldName.equals(term.field()) ){
                            termsSet.add(term);
                        }
                    }
                    for (Iterator<Term> iter = termsSet.iterator(); iter.hasNext();)
                    {
                        Term term = iter.next();
                        terms.remove(term);
                    }
                }
            }
        }
    }

}
