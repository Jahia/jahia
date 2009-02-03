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

package org.jahia.taglibs.uicomponents.windowSizeSelector;

import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletRequest;
import java.util.StringTokenizer;

/**
 * @author Xavier Lawrence
 */
public class DisplayWindowSizeComboBoxTag extends AbstractJahiaTag {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DisplayWindowSizeComboBoxTag.class);

    private String form;
    private String listName;
    private String values = "3,5,10,15,20,50";
    private int defaultWindowSize = 10;

    public void setForm(String form) {
        this.form = form;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public void setDefaultWindowSize(int defaultWindowSize) {
        this.defaultWindowSize = defaultWindowSize;
    }

    public int doStartTag() {
        try {
            final ServletRequest request = pageContext.getRequest();
            final String windowSizeParam = request.getParameter(listName + "_windowsize");

            final int listWindowSize;
            if (windowSizeParam == null) {
                listWindowSize = defaultWindowSize;
            } else {
                listWindowSize = Integer.parseInt(windowSizeParam);
            }

            final StringBuffer buff = new StringBuffer();
            buff.append("<select name=\"");
            buff.append(listName);
            buff.append("_windowsize");
            buff.append("\" onchange=\"document.");
            buff.append(form);
            buff.append(".submit()\"");
            if (cssClassName != null && cssClassName.length() > 0) {
                buff.append(" class=\"");
                buff.append(cssClassName);
                buff.append("\"");
            }
            buff.append(">");

            final int[] intValues = getIntValues(values);
            for (final int theValue : intValues) {
                buff.append("<option value=\"");
                buff.append(theValue);
                buff.append("\"");
                if (listWindowSize == theValue) {
                    buff.append(" selected=\"selected\"");
                }
                buff.append(">");
                buff.append(theValue);
                buff.append("</option>\n");
            }
            buff.append("</select>");

            final JspWriter out = pageContext.getOut();
            out.print(buff.toString());
        } catch (Exception e) {
            logger.error(e, e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        form = null;
        listName = null;
        defaultWindowSize = 10;
        values = "3,5,10,15,20,50";
        return EVAL_PAGE;
    }

    protected int[] getIntValues(final String values) {
        final StringTokenizer tokenizer = new StringTokenizer(values, ",");
        final int[] result = new int[tokenizer.countTokens()];
        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
            final String token = tokenizer.nextToken();
            final int value = Integer.parseInt(token.trim());
            result[i] = value;
        }
        return result;
    }

}
