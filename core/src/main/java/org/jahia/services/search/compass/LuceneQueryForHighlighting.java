/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
            List v = new ArrayList();
            for (Iterator iter = terms.iterator(); iter.hasNext();)
            {
                Term term = (Term) iter.next();
                v.add(new Term(this.fieldName,term.text()));
            }
            terms.clear();
            terms.addAll(v);
        } else {
            Map fieldsGrouping = ServicesRegistry.getInstance().getJahiaSearchService().getFieldsGrouping();
            if ( fieldsGrouping != null && this.fieldName != null ){
                Set l = (Set)fieldsGrouping.get(fieldName);
                if (l != null && !l.isEmpty() ) {
                    Set termsSet = new HashSet();
                    for (Iterator iter = terms.iterator(); iter.hasNext();)
                    {
                        Term term = (Term) iter.next();
                        if ( !l.contains(term.field()) && !this.fieldName.equals(term.field()) ){
                            termsSet.add(term);
                        }
                    }
                    for (Iterator iter = termsSet.iterator(); iter.hasNext();)
                    {
                        Term term = (Term) iter.next();
                        terms.remove(term);
                    }
                }
            }
        }
    }

}
