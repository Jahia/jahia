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

import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.beans.PaginationBean;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerListPagination;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.containerlist.ContainerListTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import java.io.IOException;
import java.util.Iterator;

/**
 * Lookup for the ContainerListPagination instance of the enclosing
 * ContainerListTag and iterate through the list of paginated pages.
 * <p/>
 * <pre>
 * 		<<next    -->1 2 3<--     next>>
 * </pre>
 *
 * @author NK
 * @version 1.0
 * @jsp:tag name="cListPagination" body-content="JSP"
 * description="Iterates through the list of paginated page numbers from the enclosing container list
 * in the current range of pages.
 * <p/>
 * <p><attriInfo>The cListPagination tag doesn't display anything by itself. It loops over itself for every
 * paginated page and executes other enclosed pagination tags. It must be enclosed within a container list.
 * <p/>
 * <p><b>Background:</b> This tag is part of the Container List Pagination Tag set i.e. all content:cListPagination* tags. These tags
 * facilitate displaying navigation widgets for paginated container lists, or what we refer to as Quick Page Access Buttons.
 * These tags displays break up the a container list into a smaller sets  (a.k.a. ranges/pagination windows) of pages.
 * Below is an example generated menu :
 * <p/>
 * <p>
 * <a href=\"javascript:changePage(document.jahiapageform,document.jahiapageform.ctnscroll_directoryPeopleContainer,'5_0');\">
 * &lt;&lt;Previous results</a>&nbsp;
 * <a href=\"javascript:changePage(document.jahiapageform,document.jahiapageform.ctnscroll_directoryPeopleContainer,'5_0');\">
 * 1</a>&nbsp; <b>
 * <a href=\"javascript:changePage(document.jahiapageform,document.jahiapageform.ctnscroll_directoryPeopleContainer,'5_5');\">
 * 2</a>&nbsp; </b>
 * <a href=\"javascript:changePage(document.jahiapageform,document.jahiapageform.ctnscroll_directoryPeopleContainer,'5_10');\">
 * 3</a>&nbsp;
 * <a href=\"javascript:changePage(document.jahiapageform,document.jahiapageform.ctnscroll_directoryPeopleContainer,'5_10');\">
 * Next results&gt;&gt;</a> </p><br>
 * <p/>
 * <p>FYI here is its associated HTML code :
 * <p/>
 * &lt;a href=\"javascript:changePage(document.jahiapageform,document.jahiapageform.ctnscroll_directoryPeopleContainer,'5_0');\"&gt;
 * \&lt;\&lt;Previous results&lt;/a&gt;\&nbsp;<br>
 * &lt;a href=\"javascript:changePage(document.jahiapageform,document.jahiapageform.ctnscroll_directoryPeopleContainer,'5_0');\"&gt;
 * 1&lt;/a&gt;\&nbsp; &lt;b&gt;<br>
 * &lt;a href=\"javascript:changePage(document.jahiapageform,document.jahiapageform.ctnscroll_directoryPeopleContainer,'5_5');\"&gt;
 * 2&lt;/a&gt;\&nbsp; &lt;/b&gt;<br>
 * &lt;a href=\"javascript:changePage(document.jahiapageform,document.jahiapageform.ctnscroll_directoryPeopleContainer,'5_10');\"&gt;
 * 3&lt;/a&gt;\&nbsp;<br>
 * &lt;a href=\"javascript:changePage(document.jahiapageform,document.jahiapageform.ctnscroll_directoryPeopleContainer,'5_10');\"&gt;
 * Next results\&gt;\&gt;&lt;/a&gt; &lt;/p&gt;<br>
 * <br>
 * <p/>
 * <p/>
 * <p><b>Example :</b>
 * <p/>
 * <p/>
 * &lt;content:jahiaPageForm name=\"jahiapageform\"&gt;	 <br>
 * &lt;content:containerList name=\"directoryProjectContainer\" title=\"Project container\" windowSize=\"5\"&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;content:cListPaginationCurrentPageScrollingValue valueOnly=\"false\" /&gt; <br>
 * <br>
 * &lt;table border=\"0\"&gt; <br>
 * &lt;tr&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;  &lt;td align=\"left\" valign=\"top\"&gt;&lt;b&gt;&#160;&#160;Search&#160;:&#160;&lt;/b&gt;&lt;/td&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;td valign=\"top\" align=\"left\"&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;input type=\"text\" name=\"&lt;content:ctnListSQueryInputName/&gt;\" value=\"&lt;content:ctnListSQueryInputValue/&gt;\" size=\"30\"/&gt;&#160;&#160;&lt;a href=\"javascript:document.jahiapageform.submit()\"&gt;Go&lt;/a&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;/td&gt; <br>
 * &lt;/tr&gt; <br>
 * &lt;tr&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;td align=\"left\" valign=\"top\"&gt;&lt;b&gt;&#160;&#160;Filter&#160;by&#160;:&#160;&lt;/b&gt;&lt;/td&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;td&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;select name=\"directoryProjectCategory_filter\" onChange=\"javascript:document.jahiapageform.submit()\"&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;% <br>
 * Iterator projectCategoriesEnum= projectCategoriesList.iterator(); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; while (projectCategoriesEnum.hasNext()) { <br>
 * String myCat = (String) projectCategoriesEnum.next(); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; %&gt;&lt;option value=\"&lt;%=myCat%&gt;\" &lt;% if (selProjectCategory.equals(myCat)){%&gt;selected&lt;% }%&gt;&gt;&lt;%=myCat%&gt;&lt;/option&gt;&lt;%<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; } <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; %&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/select&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;/td&gt; <br>
 * &lt;/tr&gt; <br>
 * &lt;tr&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;td align=\"left\" valign=\"top\"&gt;&lt;b&gt;&#160;&#160;Result&#160;:&#160;&lt;/b&gt;&lt;/td&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;td width=\"100%\" align=\"left\" valign=\"top\"&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;tr&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;td align=\"left\" valign=\"top\" nowrap&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;select class=\"text\" name=\"directoryProjectContainer_windowsize\" onChange=\"javascript:document.jahiapageform.submit()\"&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     &lt;option value=\"5\" &lt;% if (selProjectWindowSize.equals(\"5\")){%&gt;selected&lt;% }%&gt;&gt;5&lt;/option&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     &lt;option value=\"10\" &lt;% if (selProjectWindowSize.equals(\"10\")){%&gt;selected&lt;% }%&gt;&gt;10&lt;/option&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     &lt;option value=\"20\" &lt;% if (selProjectWindowSize.equals(\"20\")){%&gt;selected&lt;% }%&gt;&gt;20&lt;/option&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/select&gt;&#160;(Items/Page)&#160;&#160;&#160;&lt;font size=\"2\"&gt;[&lt;content:cListPaginationFirstItemIndex /&gt;&#160;-&#160;&lt;content:cListPaginationLastItemIndex /&gt;]&#160;of&#160;&lt;content:cListPaginationTotalSize /&gt;&lt;/font&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/td&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;td align=\"right\" valign=\"top\" width=\"100%\"&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;content:previousWindowButton title=\"&lt;&lt;Prev\" method=\"post\" formName=\"jahiapageform\" /&gt;&#160;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br>
 * &lt;b&gt; &lt;content:cListPagination nbStepPerPage=\"10\" &gt; &lt;/b&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     &lt;content:cListPaginationPreviousRangeOfPages method=\"post\" formName=\"jahiapageform\" title=\"&#160;..&#160;\"/&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     &lt;content:ifCListPaginationCurrentPage&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;             &lt;b&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     &lt;/content:ifCListPaginationCurrentPage&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     &lt;content:cListPaginationPageUrl method=\"post\" formName=\"jahiapageform\" /&gt;&#160; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     &lt;content:ifCListPaginationCurrentPage&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;             &lt;/b&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     &lt;/content:ifCListPaginationCurrentPage&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     &lt;content:cListPaginationNextRangeOfPages method=\"post\" formName=\"jahiapageform\" title=\"&#160;..&#160;\"/&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/content:cListPagination&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;content:nextWindowButton title=\"Next&gt;&gt;\" method=\"post\" formName=\"jahiapageform\" /&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/td&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/tr&gt;   <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/table&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;/td&gt; <br>
 * &lt;/tr&gt; <br>
 * &lt;/table&gt; <br>
 * <br>
 * &lt;input type=\"hidden\" name=\"directoryProjectContainer_sort\" value=\"&lt;%=projectSort%&gt;\"/&gt; <br>
 * &lt;input type=\"hidden\" name=\"directoryProjectContainer_sort_order\" value=\"&lt;%=projectSortOrder%&gt;\"/&gt; <br>
 * &lt;/content:containerList&gt; <br>
 * &lt;/content:jahiaPageForm&gt; <br>
 * <p/>
 * </attriInfo>"
 */

@SuppressWarnings("serial")
public class CListPaginationTag extends AbstractJahiaTag {

    private JahiaContainerListPagination cPagination = null;
    private int pageNumber = 0;
    private boolean isCurrentPage = false;
    private Iterator<?> iterator;

    private int nbStepPerPage = -1; // by default, display all page step.
    private boolean skipOnePageOnly = true; // by default skip displaying pagination if there is only one page

    private int startPageIndex = 0;
    private int stopPageIndex = 0;
    private String name = null;

    /**
     * Return the number of step per displayed page.
     */
    public int getNbStepPerPage() {
        return this.nbStepPerPage;
    }

    /**
     * Set the number of step per displayed page.
     *
     * @jsp:attribute name="nbStepPerPage" required="false" rtexprvalue="true"
     * description="the max number of Quick Page Access buttons to display at a time in the navigation bar.
     * <p/>
     * <p><attriInfo>In other words, this is the size of the pagination window.
     * <p>Default is -1 which displays all pages (i.e. the pagination window includes all pagination pages).
     * </attriInfo>"
     */
    public void setNbStepPerPage(int aNbStepPerPage) {
        this.nbStepPerPage = aNbStepPerPage;
    }

    /**
     * Return the value of the status skipOnePageOnly.
     */
    public boolean skipOnePageOnly() {
        return this.skipOnePageOnly;
    }

    /**
     * Set the value of the status skipOnePageOnly ( true/false ).
     *
     * @jsp:attribute name="skipOnePageOnly" required="false" rtexprvalue="true" type="Boolean"
     * description="skip displaying pagination if there is only one page.
     * <p/>
     * <p><attriInfo>Set to \"false\" if you want to force displaying the Quick Page Access
     * Buttons even though there is only one page available
     * (and it is the currently displayed pages).
     * <p>Default is 'true'.
     * </attriInfo>"
     */
    public void setSkipOnePageOnly(boolean val) {
        skipOnePageOnly = val;
    }

    /**
     * The next iterated page number from the list of paginated pages.
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    /**
     * Return true if the pageIndex is equals to the current displayed page.
     */
    public boolean isCurrentPage() {
        return isCurrentPage;
    }

    public int getCurrentPageIndex() {
        if (cPagination != null && cPagination.isValid())
            return cPagination.getCurrentPageIndex();
        else
            return -1;
    }

    public int getNbPages() {
        if (cPagination != null && cPagination.isValid())
            return cPagination.getNbPages();
        else
            return -1;
    }

    /**
     * Return the stop page index.
     */
    public int getStopPageIndex() {
        return this.stopPageIndex;
    }

    /**
     * Return the start page index.
     */
    public int getStartPageIndex() {
        return this.startPageIndex;
    }

    public String getName() {
        return name;
    }

    /**
     * @jsp:attribute name="99999999" required="false" rtexprvalue="true"
     * description="
     * <p/>
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setName(String aName) {
        this.name = aName;
    }

    public int doStartTag() {

        JahiaContainerList cList = null;
        if (getName() != null) {
            ContainerListBean containerListBean = (ContainerListBean)
                    pageContext.findAttribute(
                            getName());
            cList = containerListBean.getJahiaContainerList();
        } else {
            // gets the enclosing tag ContainerListTag
            ContainerListTag containerListTag = (ContainerListTag)
                    findAncestorWithClass(this,
                            ContainerListTag.class);
            if (containerListTag != null) {
                cList = containerListTag.getContainerList();
            }
        }
        if (cList != null) {
            cPagination = cList.getCtnListPagination();
            if (cPagination != null && cPagination.isValid()) {
                if (cPagination.getNbPages() == 1 && skipOnePageOnly ) {
                    return SKIP_BODY;
                }
                if (this.nbStepPerPage <= 0) {
                    this.startPageIndex = 1;
                    this.stopPageIndex = cPagination.getNbPages();
                    this.pageNumber = 1;
                } else {
                    if (cPagination.getCurrentPageIndex() <= this.nbStepPerPage) {
                        this.startPageIndex = 1;
                        this.stopPageIndex = this.nbStepPerPage;
                    } else {
                        if ((cPagination.getCurrentPageIndex() %
                                this.nbStepPerPage) == 0) {
                            this.startPageIndex = (cPagination.
                                    getCurrentPageIndex() - this.nbStepPerPage) + 1;
                        } else {
                            this.startPageIndex = cPagination.
                                    getCurrentPageIndex() -
                                    (cPagination.
                                            getCurrentPageIndex() % this.nbStepPerPage) + 1;
                        }
                        this.stopPageIndex = this.startPageIndex +
                                this.nbStepPerPage - 1;
                    }
                }
                iterator = cPagination.getPages().iterator();
                while (iterator.hasNext() &&
                        (this.pageNumber < this.startPageIndex - 1)) {
                    iterator.next();
                    this.pageNumber += 1;
                }
                if (iterator.hasNext()) {
                    this.pageNumber = (Integer) iterator.next();
                    this.isCurrentPage = (this.pageNumber ==
                            cPagination.getCurrentPageIndex());
                    if (getId() != null) {
                        if (getPaginationBean() != null) {
                            pageContext.setAttribute(getId(), getPaginationBean());
                        } else {
                            pageContext.removeAttribute(getId(), PageContext.PAGE_SCOPE);
                        }
                    }
                    return EVAL_BODY_BUFFERED;
                } else {
                    return SKIP_BODY;
                }
            }
        }
        return SKIP_BODY;
    }

    public int doAfterBody()
            throws JspTagException {
        BodyContent body = getBodyContent();
        try {
            body.writeOut(getPreviousOut());
        } catch (IOException e) {
            throw new JspTagException("CListPaginationTag.doAfterBody : " +
                    e.getMessage());
        }

        // clear up so the next time the body content is empty
        body.clearBody();
        if (iterator.hasNext() && (this.pageNumber < this.stopPageIndex)) {
            this.pageNumber = (Integer) iterator.next();
            this.isCurrentPage = (this.pageNumber ==
                    cPagination.getCurrentPageIndex());
            if (getId() != null) {
                pageContext.setAttribute(getId(), getPaginationBean());
            }
            return EVAL_BODY_BUFFERED;
        }
        return SKIP_BODY;
    }

    public int doEndTag()
            throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        cPagination = null;
        pageNumber = 0;
        isCurrentPage = false;

        nbStepPerPage = -1; // by default, display all page step.
        skipOnePageOnly = true; // by default skip displaying pagination if there is only one page

        startPageIndex = 0;
        stopPageIndex = 0;

        name = null;
        return EVAL_PAGE;
    }

    public PaginationBean getPaginationBean() {
        return new PaginationBean(this.pageNumber, this.isCurrentPage);
    }
}
