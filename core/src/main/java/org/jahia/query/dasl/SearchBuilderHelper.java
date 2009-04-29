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
 package org.jahia.query.dasl;

import org.apache.webdav.lib.search.BasicSearchBuilder;
import org.apache.webdav.lib.search.CompareOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 16 mars 2006
 * Time: 11:09:03
 * To change this template use File | Settings | File Templates.
 */
public class SearchBuilderHelper {

    BasicSearchBuilder basicSearchBuilder;

    // list of namespaces
    protected List<String> namespaces = new ArrayList<String>();

    // list of expression types
    protected List<ExpressionType> ExpressionTypes = new ArrayList<ExpressionType>();

    public SearchBuilderHelper(){
        basicSearchBuilder = new BasicSearchBuilder();
        namespaces.add(Constants.DAV);
        namespaces.add(Constants.JAHIA_SLIDE);
    }

    public BasicSearchBuilder getBasicSearchBuilder() {
        return basicSearchBuilder;
    }

    public void setBasicSearchBuilder(BasicSearchBuilder basicSearchBuilder) {
        this.basicSearchBuilder = basicSearchBuilder;
    }

    public List<String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Add default expressions
     */
    public void addDefaultExpressionTypes(){
        this.ExpressionTypes.add(SearchBuilderTools.getExpressionType("Author","Author",Constants.JAHIA_SLIDE,CompareOperator.LIKE,"",false));
        this.ExpressionTypes.add(SearchBuilderTools.getExpressionType("Title","Title",Constants.JAHIA_SLIDE,CompareOperator.LIKE,"",false));
        this.ExpressionTypes.add(SearchBuilderTools.getExpressionType("Subject","Subject",Constants.JAHIA_SLIDE,CompareOperator.LIKE,"",false));
        this.ExpressionTypes.add(SearchBuilderTools.getExpressionType("Comment","Comment",Constants.JAHIA_SLIDE,CompareOperator.LIKE,"",false));
        this.ExpressionTypes.add(SearchBuilderTools.getExpressionType("Creation Date","CreationDate",Constants.JAHIA_SLIDE,CompareOperator.GTE,"",true));
        this.ExpressionTypes.add(SearchBuilderTools.getExpressionType("Last Modification Date","LastModifDate",Constants.JAHIA_SLIDE,CompareOperator.LTE,"",true));
    }

}
