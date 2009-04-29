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

import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerListPagination;
import org.jahia.params.ProcessingContext;
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
@SuppressWarnings("serial")
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
                buff.append(ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER);
                buff.append(containerListTag.getId() != null ? containerListTag.getId() + "_" : "");
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
