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

import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerListPagination;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.containerlist.ContainerListTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;


/**
 * Return the scrolling value of the current page returned from the enclosing ContainerListTag.
 *
 * If the attribute valueOnly = "true" ( default value ), returns :
 * <pre>
 *		windowSize + "_" + windowOffset, i.e. : '5_10'
 * <pre>
 *
 * Else if valueOnly = "false", generate a hidden input :
 * <pre>
 *		<input type="hidden" name="ctnscroll_contentList1" value="5_10">
 *
 *		Where : 'contentList1' is the name of the container list
 *				'5_10 : the combination of windowSize = 5, windowOffset = 10 regarding to the values of the enclosing container list.
 *
 * </pre>
 *
 * @author  NK
 *
 * @jsp:tag name="cListPaginationCurrentPageScrollingValue" body-content="empty"
* description="Returns the scrolling value of the form ( windowSize + "_" + windowOffset ) for the current displayed page.
*
 *
* <p><attriInfo>This tag is used to track the container scrolling value of the currently displayed Page. It is used
 * to populate the container scrolling value of the currently displayed Page as a hidden input to a form.
 *
 * <p>This Tag needs to be enclosed inside a containerList Tag and a Jahia Page Form e.g.:
 *
 * <p>
 * &lt;content:jahiaPageForm name=\"jahiapageform\"&gt;  <br>
&lt;content:containerList name=\"directoryPeopleContainer\"&gt; <br>
            ... <br>
            &lt;content:cListPaginationCurrentPageScrollingValue valueOnly=\"false\" /&gt; <br>
            ... <br>
&lt;/content:containerList&gt; <br>
&lt;/content:jahiaPageForm&gt; <br>
 *
 * <p>It will generate a hidden input used by Jahia to keep track of the current position when
 * scrolling through the list of Containers e.g.:
 *
 * <p>
 * &lt;input type='hidden' name='ctnscroll_directoryPeopleContainer' value='5_15'&gt;
 *
* </attriInfo>"

 */
public class CListPaginationCurrentPageScrollingValue extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(CListPaginationCurrentPageScrollingValue.class);

    private JahiaContainerList containerList = null;
    private JahiaContainerListPagination cPagination = null;

    private String valueOnly = "true";


    /**
     * @jsp:attribute name="valueOnly" required="false" rtexprvalue="true" type="Boolean"
     * description="
     *
     * <p><attriInfo>If true, this tag only returns the following string:
     * <br> windowSize + "_" + windowOffset, i.e. : '5_10'
     *
     * <p>Else if false, it generates a hidden input of the form:
     * <br> &lt;input type=\"hidden\" name=\"ctnscroll_contentList1\" value=\"5_10\"&gt;
     * <br>
     * 		Where :
     * <br> 'contentList1' is the name of the container list
     * <br>	'5_10 : the combination of windowSize = 5, windowOffset = 10 regarding to the values of the enclosing container list.
     *
     * <p>Default is 'true'.
     * </attriInfo>"
     */
    public void setValueOnly(String value) {
        if ( value != null ){
            this.valueOnly = value.trim().toLowerCase();
        }
    }

    public String getValueOnly() {
        return this.valueOnly;
    }


    public int doStartTag() {

        //JahiaConsole.println("CListPaginationCurrentPageScrollingValue: doStartTag", "Started");

        // gets the enclosing tag ContainerListTag
        ContainerListTag containerListTag = (ContainerListTag) findAncestorWithClass(this, ContainerListTag.class);
        if (containerListTag == null) {
            return SKIP_BODY;
        }

        containerList = containerListTag.getContainerList();
        if ( containerList == null ){
            return SKIP_BODY;
        }

        cPagination = containerList.getCtnListPagination();
        if ( cPagination == null ){
            return SKIP_BODY;
        }

        String value = cPagination.getScrollingValue(cPagination.getCurrentPageIndex());
        if ( value == null ){
            return SKIP_BODY;
        }

        if ( this.valueOnly.equals("false") )
        {
            try {
                StringBuffer buff = new StringBuffer("<input type='hidden' name='");
                buff.append("ctnscroll_");
                buff.append(containerList.getDefinition().getName());
                buff.append("' value='");
                buff.append(value);
                buff.append("'>\n");
                buff.append("<input type='hidden' name='");
                buff.append("ctnlistpagination_");
                buff.append(containerList.getDefinition().getName());
                buff.append("' value='false' />");
                value = buff.toString();
            } catch ( Exception e ) {
                logger.error(e.getMessage(), e);
                return SKIP_BODY;
            }
        }

        try {
            JspWriter out = pageContext.getOut();
            out.print(value);
        } catch (IOException ioe) {
            logger.error(ioe.toString(), ioe);
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        containerList = null;
        cPagination = null;

        valueOnly = "true";
        return EVAL_PAGE;
    }

}
