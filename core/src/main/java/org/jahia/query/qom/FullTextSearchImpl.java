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
