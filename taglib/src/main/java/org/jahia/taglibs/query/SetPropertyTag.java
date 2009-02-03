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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.query;

import java.util.Properties;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.jahia.query.qom.QueryModelTools;

/**
 * This tag is used to set properties to the parent QueryDefinitionTag
 *
 * User: hollis
 * Date: 8 nov. 2007
 * Time: 13:08:23
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class SetPropertyTag extends BodyTagSupport {

    private QueryDefinitionTag queryModelDefTag = null;

    private String name;
    private String value;
    private String operation = QueryDefinitionTag.SET_ACTION;

    public int doStartTag() throws JspException {
        queryModelDefTag = (QueryDefinitionTag) findAncestorWithClass(this, QueryDefinitionTag.class);
        if (queryModelDefTag == null) {
            return SKIP_BODY;
        }

        if ( name == null || name.trim().equals("") ){
            return SKIP_BODY;
        }
        Properties properties = queryModelDefTag.getProperties();
        if (properties==null){
            properties = new Properties();
            queryModelDefTag.setProperties(properties);
        }
        if ( QueryDefinitionTag.REMOVE_ACTION.equalsIgnoreCase(operation) ){
            properties.remove(name);
        } else if ( value != null ){
            setProperty(value);
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        queryModelDefTag = null;
        name = null;
        value = null;
        operation = QueryDefinitionTag.SET_ACTION;
        return EVAL_PAGE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setProperty(String value) {
        if (value != null) {
            if (QueryDefinitionTag.SET_ACTION.equalsIgnoreCase(operation)) {
                queryModelDefTag.getProperties().setProperty(name, value);
            } else {
                QueryModelTools.appendPropertyValue(queryModelDefTag
                        .getProperties(), name, value);
            }
        }
    }


}
