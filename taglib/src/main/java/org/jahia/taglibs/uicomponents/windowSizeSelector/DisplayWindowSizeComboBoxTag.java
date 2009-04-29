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
package org.jahia.taglibs.uicomponents.windowSizeSelector;

import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletRequest;
import java.util.StringTokenizer;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
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
            final String windowSizeParam = request.getParameter(getWindowSizeKey());

            final int listWindowSize;
            if (windowSizeParam == null) {
                listWindowSize = defaultWindowSize;
            } else {
                listWindowSize = Integer.parseInt(windowSizeParam);
            }

            final StringBuffer buff = new StringBuffer();
            buff.append("<select name=\"");
            buff.append(getWindowSizeKey());
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
    
    public String getWindowSizeKey () {
        return (getId() != null && getId().length() > 0 ? getId() + "_": "") + getName() + "_windowsize";
    }
    
    public String getName() {
        return listName;
    }
}
