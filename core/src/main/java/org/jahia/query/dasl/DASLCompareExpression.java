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

import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.search.CompareOperator;
import org.apache.webdav.lib.search.expressions.CompareExpression;


/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 16 mars 2006
 * Time: 11:48:46
 * To change this template use File | Settings | File Templates.
 */
public class DASLCompareExpression extends CompareExpression {

    private boolean isDate;

    public DASLCompareExpression(CompareOperator operator,
                                 PropertyName name,
                                 Object value,
                                 boolean isDate) {
        super(operator,name,value);
        this.isDate = isDate;
    }

    public boolean isDate() {
        return isDate;
    }

    public void setDate(boolean date) {
        isDate = date;
    }

}
