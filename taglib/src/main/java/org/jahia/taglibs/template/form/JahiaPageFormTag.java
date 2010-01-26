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
package org.jahia.taglibs.template.form;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.render.RenderContext;
import org.jahia.utils.JahiaConsole;


/**
 * Generate an html form which name is "jahiaform" and action is the
 * current page request with cache off
 *
 * @author Khue Nguyen <a href="mailto:knguyen@jahia.org">knguyen@jahia.org</a>
 */
@SuppressWarnings("serial")
public class JahiaPageFormTag extends TagSupport {

    private static final String CLASS_NAME = JahiaPageFormTag.class.getName();
    public static final String FORM_NAME = "jahiapageform";
    private String name = FORM_NAME;
    private String method = "post";

    public void setName(String name)
    {
        if ( name != null && !name.trim().equals("") )
        {
            this.name = name;
        }
    }

    public String getName()
    {
        return this.name;
    }

    public void setMethod(String method)
    {
        if ( method != null && method.equalsIgnoreCase("get") )
        {
            this.method = method;
        }
    }

    public String getMethod()
    {
        return this.method;
    }

    public int doStartTag() {
        ServletRequest request = pageContext.getRequest();
        RenderContext renderContext = (RenderContext) pageContext.findAttribute("renderContext");

        try {
            String pageUrl = renderContext.getURLGenerator().getCurrent();
            JspWriter out = pageContext.getOut();
            StringBuffer buff = new StringBuffer("<form name=\"");
            buff.append(this.name);
            buff.append("\"" );
            buff.append(" action=\"");
            buff.append(pageUrl);
            buff.append("\" method=\"");
            buff.append(this.method);
            buff.append("\">");
            out.print(buff.toString());
        } catch (IOException ioe) {
            JahiaConsole.println(CLASS_NAME+"doStartTag", ioe.toString());
        }

        /*
        try {
            String pageUrl = page.getUrl(jData.params());
            if ( pageUrl != null && pageUrl.indexOf("/cache/off") == -1 )
            {
                int pos = pageUrl.indexOf("?");
                if ( pos == -1 )
                {
                    pageUrl += "/cache/offonce";
                } else {
                    pageUrl = pageUrl.substring(0,pos-1) + "/cache/offonce" + pageUrl.substring(pos);
                }
            }

            JspWriter out = pageContext.getOut();
            StringBuffer buff = new StringBuffer("<form name=\"");
            buff.append(this.name);
            buff.append("\"" );
            buff.append(" action=\"");
            buff.append(pageUrl);
            buff.append("\" method=\"");
            buff.append(this.method);
            buff.append("\">");
            out.print(buff.toString());
        } catch (IOException ioe) {
            JahiaConsole.println(CLASS_NAME+"doStartTag", ioe.toString());
        } catch (JahiaException je) {
            JahiaConsole.println(CLASS_NAME+"doStartTag", je.toString());
        }*/

        return EVAL_BODY_INCLUDE;
    }

    public int doAfterBody(){
        try {
            JspWriter out = pageContext.getOut();
            out.print("</form>");
            return EVAL_PAGE;
        } catch (IOException ioe) {
            JahiaConsole.println(CLASS_NAME+"doStartTag", ioe.toString());
        }
        return EVAL_PAGE;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        name = FORM_NAME;
        method = "post";
        return EVAL_PAGE;
    }

}
