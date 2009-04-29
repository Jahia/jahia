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
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 19 févr. 2009
 * Time: 17:46:51
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class DropDownFromBundleFileTag extends AbstractJahiaTag {
    private String bundleName;

    /**
     * Default processing of the start tag returning EVAL_BODY_BUFFERED.
     *
     * @return EVAL_BODY_BUFFERED
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.BodyTag#doStartTag
     */
    @Override
    public int doStartTag() throws JspException {
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle(bundleName, getProcessingContext().getCurrentLocale(), JahiaTemplatesRBLoader.getInstance(this.getClass().getClassLoader(), getProcessingContext().getSite().getTemplatePackageName()));
        if (bundle != null) {
            SortedSet<String> values = new TreeSet<String>();
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                values.add(bundle.getString(key));
            }
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
        return SKIP_BODY;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }
}