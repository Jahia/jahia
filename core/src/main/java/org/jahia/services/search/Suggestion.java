/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.search;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents a spell checker corrected query (suggestion) that can be used to
 * implement "Did you mean" kind of searches.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class Suggestion {

    /**
     * Represents a single term of the search query with the information about
     * suggested replacement.
     * 
     * @author Sergiy Shyrkov
     */
    public static class Term {

        private boolean modified;

        private String term;

        /**
         * Initializes an instance of this class.
         * 
         * @param newTerm the query term string
         * @param modified <code>true</code> if the original term was modified by
         *            the suggestion engine
         */
        public Term(String newTerm, boolean modified) {
            super();
            this.modified = modified;
            this.term = newTerm;
        }

        /**
         * Returns a query term either the original one or the suggested (in
         * this case the {@link #isModified()} returns true).
         * 
         * @return a query term either the original one or the suggested (in
         *         this case the {@link #isModified()} returns true)
         */
        public String getTerm() {
            return term;
        }

        /**
         * Returns <code>true</code> if this term was modified by a spell
         * checker and represent a suggestion
         * 
         * @return <code>true</code> if this term was modified by a spell
         *         checker and represent a suggestion
         */
        public boolean isModified() {
            return modified;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    private String originalQuery;

    private String suggestedQuery;

    private List<Term> terms;

    /**
     * Initializes an instance of this class.
     * 
     * @param originalQuery
     * @param suggestedQuery
     */
    public Suggestion(String originalQuery, String suggestedQuery) {
        super();
        this.originalQuery = originalQuery;
        this.suggestedQuery = suggestedQuery;
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param originalQuery
     * @param suggestedQuery
     * @param terms
     */
    public Suggestion(String originalQuery, String suggestedQuery, List<Term> terms) {
        super();
        this.originalQuery = originalQuery;
        this.suggestedQuery = suggestedQuery;
        this.terms = terms;
    }

    /**
     * Returns the original query string.
     * 
     * @return the original query string
     */
    public String getOriginalQuery() {
        return originalQuery;
    }

    /**
     * Returns the suggested query string.
     * 
     * @return the suggested query string
     */
    public String getSuggestedQuery() {
        return suggestedQuery;
    }

    /**
     * Returns a list of suggested query terms.
     * 
     * @return a list of suggested query terms
     */
    public List<Term> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
