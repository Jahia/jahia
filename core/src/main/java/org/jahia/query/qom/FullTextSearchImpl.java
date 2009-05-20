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
package org.jahia.query.qom;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.FullTextSearch;
import org.jahia.exceptions.JahiaException;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 10:42:55
 * To change this template use File | Settings | File Templates.
 */
public class FullTextSearchImpl extends ConstraintImpl implements FullTextSearch, Constraint {

    private String fullTextSearchExpression;
    private String propertyName;
    private String[] aliasNames;    
    private String selectorName;

    private boolean isMetadata;

    public FullTextSearchImpl(String selectorName, String propertyName,
            String fullTextSearchExpression, String[] aliasNames) {
        this.fullTextSearchExpression = fullTextSearchExpression;
        this.propertyName = propertyName;
        this.selectorName = selectorName;
        this.aliasNames = aliasNames;
    }    
    
    public FullTextSearchImpl(String selectorName,
                              String propertyName,String fullTextSearchExpression) {
        this.fullTextSearchExpression = fullTextSearchExpression;
        this.propertyName = propertyName;
        this.selectorName = selectorName;
    }

    public FullTextSearchImpl(String propertyName,String fullTextSearchExpression) {
        this.fullTextSearchExpression = fullTextSearchExpression;
        this.propertyName = propertyName;
    }
    
    public FullTextSearchImpl(String propertyName,String fullTextSearchExpression, String[] aliasNames) {
        this.fullTextSearchExpression = fullTextSearchExpression;
        this.propertyName = propertyName;
        this.aliasNames = aliasNames;        
    }  

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public boolean isMetadata() {
        return isMetadata;
    }

    public void setMetadata(boolean metadata) {
        isMetadata = metadata;
    }

    public String getFullTextSearchExpression() {
        return fullTextSearchExpression;
    }

    public void setFullTextSearchExpression(String fullTextSearchExpression) {
        this.fullTextSearchExpression = fullTextSearchExpression;
    }

    /**
     * Gets the name of the selector against which to apply this constraint.
     *
     * @return the selector name; non-null
     */
    public String getSelectorName() {
        return this.selectorName;
    }

    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        interpreter.accept(this);
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer(FullTextSearchImpl.class.getName());
        buffer.append("propertyName=").append(propertyName).append(" searchExpression=")
                .append(this.fullTextSearchExpression);
        return buffer.toString();
    }
    
    public String[] getAliasNames() {
        return aliasNames;
    }

    public void setAliasNames(String[] aliasNames) {
        this.aliasNames = aliasNames;
    }

}
