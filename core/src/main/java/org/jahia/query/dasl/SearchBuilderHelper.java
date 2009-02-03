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
