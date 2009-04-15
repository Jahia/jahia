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

package org.jahia.taglibs.jcr.file;

import org.apache.struts.taglib.TagUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 18 déc. 2007
 * Time: 17:56:01
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class FileTag extends AbstractJahiaTag {
    private String name;
    private String property;
    private String scope;

    private JCRNodeWrapper file;

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

    public JCRNodeWrapper getFile() {
        return file;
    }

    public int doStartTag() throws JspException {
        file = (JCRNodeWrapper) TagUtils.getInstance().lookup(pageContext, name, property, scope);

        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        name = null;
        property = null;
        scope = null;

        file = null;

        return EVAL_PAGE;
    }
}
