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

package org.jahia.taglibs.utility.i18n;

import org.jahia.taglibs.AbstractJahiaTag;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * User: ktlili
 * Date: 9 sept. 2008
 * Time: 17:23:57
 */
public class GWTResourceBundleTag extends AbstractJahiaTag {
    private static final transient Logger logger = Logger.getLogger(GWTResourceBundleTag.class);
    private String resourceName;
    private String aliasResourceName;


    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getAliasResourceName() {
        return aliasResourceName;
    }

    public void setAliasResourceName(String aliasResourceName) {
        this.aliasResourceName = aliasResourceName;
    }

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        // print output : example: pwd:"Password",
        try {
            // generate
            StringBuffer outBuf = new StringBuffer();
            boolean isFirstResource = pageContext.getRequest().getAttribute("org.jahia.ajax.gwt.rb") == null;
            if (!isFirstResource) {
                outBuf.append(",\n");
            }
            if (aliasResourceName != null && !"".equals(aliasResourceName.trim())) {
                outBuf.append("\"").append(aliasResourceName).append("\"");
            } else {
                outBuf.append("\"").append(resourceName).append("\"");
            }
            outBuf.append(":\"");
            GWTResourceBundleDictionaryTag parent = (GWTResourceBundleDictionaryTag) findAncestorWithClass(
                    this, GWTResourceBundleDictionaryTag.class);
            outBuf.append(getResourceValue(parent != null
                    && parent.getResourceNamePrefix() != null ? parent
                    .getResourceNamePrefix()
                    + resourceName : resourceName, getProcessingContext()));
            outBuf.append("\"");
            out.print(outBuf.toString());
            pageContext.getRequest().setAttribute("org.jahia.ajax.gwt.rb", Boolean.TRUE);
        } catch (IOException e) {
            logger.error(e, e);
        }
        return SKIP_BODY;
    }


    public int doEndTag() {
        resourceName = null;
        aliasResourceName = null;
        return EVAL_PAGE;
    }

}
