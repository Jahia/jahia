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
package org.jahia.taglibs.query;

import org.jahia.query.qom.QueryModelTools;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.utility.ParamParent;

import javax.servlet.jsp.JspException;
import java.util.Properties;

/**
 * This tag is used to set properties to the parent QueryDefinitionTag
 *
 * User: hollis
 * Date: 8 nov. 2007
 * Time: 13:08:23
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class SetPropertyTag extends AbstractJahiaTag implements ParamParent {

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
            addParam(value);
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

    public void addParam(Object value) {
        if (value != null) {
            if (QueryDefinitionTag.SET_ACTION.equalsIgnoreCase(operation)) {
                queryModelDefTag.getProperties().setProperty(name, (String)value);
            } else {
                QueryModelTools.appendPropertyValue(queryModelDefTag
                        .getProperties(), name, (String)value);
            }
        }
    }


}
