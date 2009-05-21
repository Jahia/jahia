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
package org.jahia.taglibs.jcr.file;

import org.apache.struts.taglib.TagUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 8, 2008
 * Time: 5:20:36 PM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class IsNodeTypeTag extends AbstractJahiaTag {
    private String name = "file";
    private String property;
    private String scope;

    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int doStartTag() throws JspException {
        JCRNodeWrapper file = null;

        FileTag ft = (FileTag) findAncestorWithClass(this, FileTag.class);
        if (ft != null) {
            file = ft.getFile();
        } else {
            file = (JCRNodeWrapper) TagUtils.getInstance().lookup(pageContext, name, property, scope);
        }

        try {
            if (file != null && file.isNodeType(type)) {
                return EVAL_BODY_INCLUDE;
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        name = "file";
        property = null;
        scope = null;
        type = null;

        return EVAL_PAGE;
    }
}
