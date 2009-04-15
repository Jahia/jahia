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
