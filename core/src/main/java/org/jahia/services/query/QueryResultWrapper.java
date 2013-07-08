/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.query;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.RangeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.jackrabbit.core.query.FacetedQueryResult;
import org.apache.jackrabbit.core.query.JahiaSimpleQueryResult;
import org.apache.jackrabbit.value.StringValue;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.*;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for the JCR {@link QueryResult} adding facets and other methods.
 * 
 * @author Thomas Draier
 */
public interface QueryResultWrapper extends QueryResult {

    public List<FacetField> getFacetFields();

    public List<FacetField> getFacetDates();
    
    public List<RangeFacet> getRangeFacets();

    public FacetField getFacetField(String name);

    public FacetField getFacetDate(String name);
    
    public RangeFacet getRangeFacet(String name);

    public boolean isFacetFieldsEmpty(List<FacetField> facetFields);

    public Map<String, Long> getFacetQuery();

    public List<FacetField> getLimitingFacets();

    /**
     * Check if the queryResultWrapper contains any facet results
     * @return true is queryResultWrapper doesn't contains any facet results
     */
<<<<<<< .working
    public boolean isFacetResultsEmpty();
=======
    public boolean isFacetResultsEmpty(){
        return (this.getFacetFields() == null || isFacetFieldsEmpty(this.getFacetFields())) &&
                (this.getFacetDates() == null || isFacetFieldsEmpty(this.getFacetDates())) &&
                (this.getFacetQuery() == null || this.getFacetQuery().isEmpty());
    }
>>>>>>> .merge-right.r46638

    public long getApproxCount();
}