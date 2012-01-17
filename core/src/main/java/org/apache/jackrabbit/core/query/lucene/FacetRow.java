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

package org.apache.jackrabbit.core.query.lucene;

import org.apache.solr.client.solrj.response.FacetField;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;
import java.util.List;
import java.util.Map;

/**
 * Fake result row, holding facet information
 */
public class FacetRow implements Row {
    private Map<String, Long> facetQuery = null;
    private List<FacetField> facetFields = null;
    private List<FacetField> limitingFacets = null;
    private List<FacetField> facetDates = null;

    public FacetRow() {
    }

    public Map<String, Long> getFacetQuery() {
        return facetQuery;
    }

    public void setFacetQuery(Map<String, Long> facetQuery) {
        this.facetQuery = facetQuery;
    }

    public List<FacetField> getFacetFields() {
        return facetFields;
    }

    public void setFacetFields(List<FacetField> facetFields) {
        this.facetFields = facetFields;
    }

    public List<FacetField> getLimitingFacets() {
        return limitingFacets;
    }

    public void setLimitingFacets(List<FacetField> limitingFacets) {
        this.limitingFacets = limitingFacets;
    }

    public List<FacetField> getFacetDates() {
        return facetDates;
    }

    public void setFacetDates(List<FacetField> facetDates) {
        this.facetDates = facetDates;
    }

    /**
     * get
     *
     * @param name the name of the
     * @return the FacetField by name or null if it does not exist
     */
    public FacetField getFacetField(String name) {
        if (facetFields == null)
            return null;
        for (FacetField f : facetFields) {
            if (f.getName().equals(name))
                return f;
        }
        return null;
    }

    public FacetField getFacetDate(String name) {
        if (facetDates == null)
            return null;
        for (FacetField f : facetDates)
            if (f.getName().equals(name))
                return f;
        return null;
    }

    public Value[] getValues() throws RepositoryException {
        return new Value[0];
    }

    public Value getValue(String columnName) throws ItemNotFoundException, RepositoryException {
        return null;
    }

    public Node getNode() throws RepositoryException {
        return null;
    }

    public Node getNode(String selectorName) throws RepositoryException {
        return null;
    }

    public String getPath() throws RepositoryException {
        return null;
    }

    public String getPath(String selectorName) throws RepositoryException {
        return null;
    }

    public double getScore() throws RepositoryException {
        return 0;
    }

    public double getScore(String selectorName) throws RepositoryException {
        return 0;
    }
}
