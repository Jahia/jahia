/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.utility.i18n;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.i18n.ResourceBundles;
import org.slf4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * User: rincevent
 * Date: 19 févr. 2009
 * Time: 17:46:51
 * 
 */
@SuppressWarnings("serial")
public class DropDownFromBundleFileTag extends AbstractJahiaTag {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DropDownFromBundleFileTag.class);
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
        ResourceBundle bundle = ResourceBundles.get(bundleName, getRenderContext().getMainResourceLocale());
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
                    logger.error(e.getMessage(), e);  //To change body of catch statement use File | Settings | File Templates.
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