/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.utility.i18n;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.i18n.JahiaTemplatesRBLoader;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.IOException;

/**
 * 
 * User: rincevent
 * Date: 19 févr. 2009
 * Time: 17:46:51
 * 
 */
@SuppressWarnings("serial")
public class DropDownFromBundleFileTag extends AbstractJahiaTag {
    private String bundleName;
    private String var;

    /**
     * Default processing of the start tag returning EVAL_BODY_BUFFERED.
     *
     * @return EVAL_BODY_BUFFERED
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.BodyTag#doStartTag
     */
    @Override
    public int doStartTag() throws JspException {
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle(bundleName, getRenderContext().getMainResourceLocale(), JahiaTemplatesRBLoader.getInstance(this.getClass().getClassLoader(), getRenderContext().getSite().getTemplatePackageName()));
        if (bundle != null) {
            SortedSet<String> values = new TreeSet<String>();
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                values.add(bundle.getString(key));
            }
            if (var == null || "".equals(var)) {
                final JspWriter writer = pageContext.getOut();
                try {
                    writer.println("<select>");
                    for (String value : values) {
                        writer.println("<option value=\""+value+"\">"+value+"</option>");
                    }
                    writer.println("</select>");
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            else {
                pageContext.setAttribute(var,values);
            }
        }
        return SKIP_BODY;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }
}