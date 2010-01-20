/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.query;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.jahia.taglibs.utility.ParamParent;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * This tag is used to set properties to the parent QueryDefinitionTag
 *
 * User: hollis
 * Date: 8 nov. 2007
 * Time: 13:08:23
 */
@SuppressWarnings("serial")
public class SetPropertyTag extends QueryDefinitionDependentTag implements ParamParent {

    private String name;
    private String value;
    private String operation = QueryDefinitionTag.SET_ACTION;

    public int doStartTag() throws JspException {
        QueryDefinitionTag queryModelDefTag = getQueryDefinitionTag();

        if ( name == null || name.trim().equals("") ){
            return SKIP_BODY;
        }
        if ( QueryDefinitionTag.REMOVE_ACTION.equalsIgnoreCase(operation) ){
            queryModelDefTag.getProperties().remove(name);
        } else if ( value != null ){
            addParam(value);
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
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

    public void addParam(Object value) {
        if (value != null) {
            try {
                if (QueryDefinitionTag.SET_ACTION.equalsIgnoreCase(operation)) {
                        getQueryDefinitionTag().getProperties().setProperty(name, (String)value);
                } else {
                    appendPropertyValue(getQueryDefinitionTag().getProperties(), name, (String)value);
                }
            } catch (JspTagException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static void appendPropertyValue(Properties properties, String propertyName, String value) {
        if (properties == null || StringUtils.isEmpty(value) || StringUtils.isEmpty(propertyName)) {
            return;
        }
        String propValue = properties.getProperty(propertyName);
        if (StringUtils.isEmpty(propValue)) {
            properties.setProperty(propertyName, value);
        } else {
            propValue += "," + value;
        }
}


}
