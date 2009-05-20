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
package org.jahia.taglibs.internal.pagination;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.JahiaConsole;


/**
 * This tag evaluates its body only when the enclosing ContainerListPagination.isCurrentpage() return true.
 *
 * @author NK
 *
 * @jsp:tag name="ifCListPaginationCurrentPage" body-content="JSP"
 * description="Evaluate its body only if the current iteration of the enclosing cListPagination tag is
 * for the currently display page.
 *
 * <p><attriInfo>i.e. if the enclosing CListPaginationTag.isCurrentPage() returns true.
 * <p>When iterating through the Quick Page Access Buttons list, this Tag can be used to check
 * if the current Quick Page Access Button to display refers to the currently Displayed page.
 * If so, we can highlight this button using Bold characters, etc...
 *
 * <p>See <a href='cListPagination.html' target='tagFrame'>content:cListPagination</a> for more details.
 *
 * <p><b>Example :</b>
 * <p>
 *
 &lt;content:cListPagination nbStepPerPage=\"3\"&gt; <br>
 &nbsp;&lt;content:cListPaginationPreviousRangeOfPages method=\"post\"<br>
 &nbsp;&nbsp;&nbsp;&nbsp;formName=\"jahiapageform\" title=\"&#160;..&#160;\"/&gt;<br>
 <br>
 &nbsp;&nbsp;&lt;content:ifCListPaginationCurrentPage&gt;&lt;b&gt;<br>
 &nbsp;&nbsp;&lt;/content:ifCListPaginationCurrentPage&gt;<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&lt;content:cListPaginationPageUrl method=\"post\"<br>
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;formName=\"jahiapageform\" /&gt;&#160;<br>
 &nbsp;&nbsp;&lt;content:ifCListPaginationCurrentPage&gt;&lt;/b&gt;<br>
 &nbsp;&nbsp;&lt;/content:ifCListPaginationCurrentPage&gt;<br>
 <br>
 &nbsp;&lt;content:cListPaginationNextRangeOfPages method=\"post\"<br>
 &nbsp;&nbsp;&nbsp;&nbsp;formName=\"jahiapageform\" title=\"&#160;..&#160;\"/&gt;<br>
&nbsp;&nbsp;&lt;/content:cListPagination&gt;<br>
 *
 *
 * </attriInfo>"
 */
@SuppressWarnings("serial")
public class IfCListPaginationCurrentPageTag extends AbstractJahiaTag {

    private boolean doEvaluateBody = false;

    public int doStartTag() {
        // gets the enclosing tag ContainerListTag
        CListPaginationTag containerListPaginationTag = (CListPaginationTag) findAncestorWithClass(this, CListPaginationTag.class);
        if (containerListPaginationTag == null) {
            JahiaConsole.println("IfCListPaginationCurrentPageTag: doStartTag", "No container list pagination tag found !!");
            return SKIP_BODY;
        }
        if (containerListPaginationTag.isCurrentPage()) {
            //JahiaConsole.println("IfCListPaginationCurrentPageTag: doStartTag", "Current page : " + containerListPaginationTag.getPageNumber());
            this.doEvaluateBody = true;
            return EVAL_BODY_BUFFERED;
        }
        return SKIP_BODY;
    }

    public int doAfterBody() {
        if ( doEvaluateBody )
        {
            try {
                BodyContent body = getBodyContent();
                JspWriter out = body.getEnclosingWriter();
                out.print(body.getString());
            } catch (IOException ioe) {
                JahiaConsole.println("IfCListPaginationCurrentPageTag: doAfterBody ",ioe.toString());
            }
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        doEvaluateBody = false;
        return EVAL_PAGE;
    }

}
