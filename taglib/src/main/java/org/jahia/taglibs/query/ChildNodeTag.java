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

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.ChildNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import javax.servlet.jsp.JspException;

/**
 * Tag used to create a ChildNode Constraint
 * 
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:33:24
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class ChildNodeTag extends ConstraintTag  {

    private ChildNode childNode;
    private String selectorName;
    private String path;

    public ChildNodeTag(){
    }

    public int doEndTag() throws JspException {
        int eval = super.doEndTag();
        childNode = null;
        selectorName = null;
        path = null;
        return eval;
    }

    public String getSelectorName() {
        return selectorName;
    }

    public void setSelectorName(String selectorName) {
        this.selectorName = selectorName;
    }

    public ChildNode getChildNode() {
        return childNode;
    }

    public void setChildNode(ChildNode childNode) {
        this.childNode = childNode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Constraint getConstraint() throws Exception {
        if ( childNode != null ){
            return childNode;
        }
        if ("".equals(path.trim())){
            return null;
        }
        if (selectorName==null || "".equals(selectorName.trim())){
            childNode = this.getQueryFactory().childNode(path);
        } else {
            childNode = this.getQueryFactory().childNode(selectorName.trim(),path);
        }
        return childNode;
    }

}