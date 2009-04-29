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
package org.jahia.taglibs.template.form;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.pages.JahiaPage;
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
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if ( jData == null )
            return EVAL_BODY_INCLUDE;

        JahiaPage page = jData.getProcessingContext().getPage();

        try {
            String pageUrl = page.getURL(jData.getProcessingContext());
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
