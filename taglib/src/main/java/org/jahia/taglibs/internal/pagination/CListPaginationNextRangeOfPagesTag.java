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

import javax.servlet.jsp.JspException;

import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerListPagination;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.taglibs.internal.uicomponents.AbstractButtonTag;
import org.jahia.taglibs.template.containerlist.ContainerListTag;

/**
 * <p>Title: This tag generates a next range of page step button for scrollable container lists</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author NK
 * @version 1.0
 *
 * @jsp:tag name="cListPaginationNextRangeOfPages" body-content="empty"
 * description="Displays a button (full link) to jump to the next range of pagination window.
 *
 * <p><attriInfo>See <a href='cListPagination.html' target='tagFrame'>content:cListPagination</a> for more details.
 *
 *
 *
 * <p><b>Example :</b>
 * <p>
 *
 *
 * &lt;content:cListPagination nbStepPerPage=\"3\"&gt; <br>
 *  &lt;content:cListPaginationPreviousRangeOfPages method=\"post\"  <>
 *  formName=\"jahiapageform\" title=\"&amp;#160;..&amp;#160;\"/&gt; <br>
 *      ... <br>
 *     <br>
 *  &lt;content:cListPaginationNextRangeOfPages method=\"post\"  <br>
 * formName=\"jahiapageform\" title=\"&amp;#160;..&amp;#160;\"/&gt; <br>
 * &lt;/content:cListPagination&gt; <br>
 *
 * </attriInfo>"

 */

@SuppressWarnings("serial")
public class CListPaginationNextRangeOfPagesTag extends AbstractButtonTag {

    private JahiaContainerList containerList = null;
    private JahiaContainerListPagination cPagination = null;
    private CListPaginationTag cPaginationTag = null;
    private ContainerListTag parent = null;

    private String title = "Next&gt;&gt;";
    private String style = "";
    private String method = "get";
    private String formName = "";

    /**
     * @jsp:attribute name="title" required="false" rtexprvalue="true"
     * description="The title of the button.
     *
     * <p><attriInfo>Default is \"Next&amp;gt;&amp;gt;\".
     * </attriInfo>"
     */
    public void setTitle (String title) {
        this.title = title;
    }

    /**
     *
     * @jsp:attribute name="style" required="false" rtexprvalue="true"
     * description="the CSS class to use for the style of the button.
     * <p><attriInfo>Default ia no CSS style at all.
     *  </attriInfo>"
     */
    public void setStyle (String style) {
        this.style = style;
    }

    /**
     * @jsp:attribute name="method" required="false" rtexprvalue="true"
     * description="If you want to implement a Post ( form submission) request version, you need to set this attribute
     * to \"post\".
     * <p><attriInfo>By default, the method is \"get\".
     * </attriInfo>"
     */

    public void setMethod (String method) {
        if (method != null) {
            this.method = method;
        }
    }

    /**
     * @jsp:attribute name="formName" required="false" rtexprvalue="true"
     * description="the form name needed to generate the form submit Javascript code.
     *
     * <p><attriInfo>The value must refers to the corrent enclosing Jahia Page Form Name.
     * It is mandatory when the method attribute is set to \"post\".
     * </attriInfo>"
     */
    public void setFormName (String formName) {
        if (formName != null) {
            this.formName = formName.trim();
        }
    }

    public String getTitle () {
        return this.title;
    }

    public String getStyle () {
        return this.style;
    }

    public String getMethod () {
        return this.method;
    }

    public String getFormName () {
        return this.formName;
    }

    public boolean testRights (JahiaData jData) {

        cPaginationTag = (CListPaginationTag) findAncestorWithClass(this,
            CListPaginationTag.class);
        if (cPaginationTag == null) {
            return false;
        }
        if (cPaginationTag.getNbStepPerPage() <= 0) {
            return false;
        }
        parent = (ContainerListTag) findAncestorWithClass(this,
            ContainerListTag.class);
        if (parent != null) {
            this.containerList = parent.getContainerList();
            if (this.containerList != null) {
                this.cPagination = this.containerList.getCtnListPagination();
                if (this.cPagination != null && this.cPagination.isValid()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getLauncher (JahiaData jData)
        throws JahiaException {

        String value = null;
        //JahiaConsole.println("CListPaginationNextRangeOfPagesTag.getLauncher","Started ---------------------------------------");
        //JahiaConsole.println("CListPaginationNextRangeOfPagesTag.getLauncher","Page Number :" + cPaginationTag.getPageNumber());
        //JahiaConsole.println("CListPaginationNextRangeOfPagesTag.getLauncher","Stop Page :" + cPaginationTag.getStopPageIndex());
        //JahiaConsole.println("CListPaginationNextRangeOfPagesTag.getLauncher","Total Pages :" + this.cPagination.getNbPages());

        if ( cPaginationTag.getStopPageIndex() < this.cPagination.getNbPages()) {

            //JahiaConsole.println("CListPaginationNextRangeOfPagesTag.getLauncher","Yes starting generate next[X] button");

            if (cPaginationTag.getStartPageIndex() > 1) {
                value = jData.gui().drawContainerListNextWindowPageURL(
                    containerList,
                    cPaginationTag.getStopPageIndex() + 1 -
                    cPagination.getCurrentPageIndex(),
                    this.cPagination.getWindowSize(), this.method.equals("post"), parent.getId());
            } else {
                value = jData.gui().drawContainerListNextWindowPageURL(
                    containerList,
                    cPaginationTag.getNbStepPerPage() + 1 -
                    cPagination.getCurrentPageIndex(),
                    this.cPagination.getWindowSize(), this.method.equals("post"), parent.getId());
            }

            //JahiaConsole.println("CListPaginationNextRangeOfPagesTag.getLauncher","Values is :" + value);

            if (value != null && this.method.equals("post")) {
                StringBuffer buff = new StringBuffer(
                    "javascript:changePage(document.");
                buff.append(getFormName());
                buff.append(",document.");
                buff.append(getFormName());
                buff.append(".").append(ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER);
                buff.append(parent.getId() != null ? parent.getId() + "_" : "");                
                buff.append(containerList.getDefinition().getName());
                buff.append(",'");
                buff.append(value);
                buff.append("');");
                value = buff.toString();
            }
        }
        return value;
    }

    public int doEndTag ()
        throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        containerList = null;
        parent = null;
        cPagination = null;
        cPaginationTag = null;

        title = "Next&gt;&gt;";
        style = "";
        method = "get";
        formName = "";
        return EVAL_PAGE;
    }

}
