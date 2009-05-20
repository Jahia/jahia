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

import org.apache.webdav.lib.search.SearchExpression;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 16 mars 2006
 * Time: 12:00:04
 * To change this template use File | Settings | File Templates.
 */
public class ExpressionType {

    protected String name;
    protected SearchExpression searchExpression;

    public ExpressionType(String name, SearchExpression searchExpression){
        this.name = name;
        this.searchExpression = searchExpression;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SearchExpression getSearchExpression() {
        return searchExpression;
    }

    public void setSearchExpression(SearchExpression searchExpression) {
        this.searchExpression = searchExpression;
    }

}
