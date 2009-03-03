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

import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.jahia.taglibs.internal.uicomponents.AbstractButtonTag;
import org.jahia.taglibs.template.containerlist.ContainerListTag;

/**
 * <p>Title: This tag generates a next window button for scrollable container lists</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 *          <p/>
 *          05.05.2002 NK : Added Post method support
 * @jsp:tag name="nextWindowButton" body-content="empty"
 * description="Displays a button (full link) for scrolling to the next window page of a scrollable container list.
 * <p><attriInfo>This tag is part of the Container List Pagination functionality that basically partitions a long list
 * of containers, contained within a container list, into more manageable shorter lists displayed on a per page basis.<br>
 * <p>Use the step parameter to specify how many pages to step (default 1) and the windowSize to change
 * the default size specified in the template.
 * <p/>
 * <p><b>Example 1 :</b>
 * <p/>
 * &lt;content:nextWindowButton title=\"Next\&ampgt;\&ampgt;\" method=\"post\" formName=\"jahiapageform\" /&gt;
 * <p/>
 * <p>The above Tag will generate the following URL:
 * <p/>
 * <p><i>&lt;a href=\"javascript:changePage(document.jahiapageform,
 * document.jahiapageform.ctnscroll_directoryPeopleContainer,'5_10');\"&gt; windowSize_windowStep
 * Next&gt;&gt;
 * &lt;/a&gt;</i>
 * <p/>
 * <p>The javascript changePage method changes the form's ctnscroll_directoryPeopleContainer to '5_10' and submits the form.
 * <p/>
 * <p><b>Example 2 :</b>
 * <p/>
 * <p>You want to use this Tag to generate a simple URL when not using forms (FYI we also include the containerlist
 * tag) :
 * <p/>
 * <br>&lt;content:containerList name=\"contentList1\" title=\"Content List\" <b>windowSize=\"5\"</b> &gt;
 * <br>&nbsp;&nbsp;&nbsp;...
 * <br>&nbsp;&nbsp;&nbsp;&lt;content:nextWindowButton title=\"Next\&gt;\&gt;\" /&gt;
 * <br>&nbsp;&nbsp;&nbsp;...
 * <br>&lt;/content:containerList&gt;
 * <p/>
 * <p>The above Tag will generate the following URL:
 * <p/>
 * <p><i>&lt;a href=\"http://localhost:8080/jahia/Jahia/cache/offonce/pid/7/ctnscroll_directoryPeopleContainer/5_10\"&gt;Next&gt;&gt;&lt;/a&gt;</i>
 * <p/>
 * </attriInfo>"
 */

@SuppressWarnings("serial")
public class NextWindowButtonTag extends AbstractButtonTag {
    private ContainerListTag parent = null;
    private JahiaContainerList containerList = null;
    private String title = "Next&gt;&gt;";
    private String style = "";
    private String method = "get";
    private String formName = "";

    private String titleKey;
    private String bundleKey;

    private int windowStepInt = 1;
    private int windowSizeInt = -1;

    // these are necessary since we must be JavaBean compliant.
    private String windowStep;
    private String windowSize;

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public void setBundleKey(String bundleKey) {
        this.bundleKey = bundleKey;
    }

    /**
     * @jsp:attribute name="title" required="false" rtexprvalue="true"
     * description="the caption to display to scroll to the next window page.
     * <p><attriInfo>Defaulted to \"Next\&gt;\&gt;\".
     * </attriInfo>"
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @jsp:attribute name="style" required="false" rtexprvalue="true"
     * description="button's CSS style name to use.
     * <p><attriInfo>Defaulted to no CSS style at all.
     * </attriInfo>"
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * @jsp:attribute name="method" required="false" rtexprvalue="true"
     * description="HTTP method to request next window page.
     * <p><attriInfo>Jahia supports both the HTTP POST and HTTP GET methods for pagination.
     * If you want to implement a Post (form submission) request version, you need to set this attribute to \"post\".
     * Defaulted to \"get\".
     * </attriInfo>"
     */
    public void setMethod(String method) {
        if (method != null) {
            this.method = method;
        }
    }

    /**
     * @jsp:attribute name="formName" required="false" rtexprvalue="true"
     * description="The form name needed to generate the form submit Javascript code.
     * <p/>
     * <p><attriInfo>The value must refer to the current enclosing Jahia Page Form Name. It is mandatory when
     * the method attribute is set to \"post\". In the example below, the form name jahiapageform is used to
     * generate the following HTML:
     * <p/>
     * <i>&lt;a href=\"javascript:changePage(document.<b>jahiapageform</b>,
     * document.<b>jahiapageform</b>.ctnscroll_directoryPeopleContainer,'5_10');\"&gt; windowSize_windowStep
     * Next&gt;&gt;
     * &lt;/a&gt;</i>
     * <p/>
     * </attriInfo>"
     */
    public void setFormName(String formName) {
        if (formName != null) {
            this.formName = formName.trim();
        }
    }

    /**
     * @jsp:attribute name="windowStep" required="false" rtexprvalue="true"  type="Integer"
     * description="the pagination window step/offset to use.
     * <p><attriInfo>Use the step parameter to specify how many pages to step. Defaulted to \"1\".
     * </attriInfo>"
     */
    public void setWindowStep(String windowStep) {
        this.windowStep = windowStep;
        try {
            windowStepInt = Integer.parseInt(windowStep);
            if (windowStepInt < 0) {
                windowStepInt = 0;
            }
        } catch (NumberFormatException nfe) {
            windowStepInt = 0;
        }
    }

    /**
     * @jsp:attribute name="windowSize" required="false" rtexprvalue="true" type="Integer"
     * description="the pagination window size to use.
     * <p><attriInfo>This redefines the default number of items per page set by the template the enclosing container list tag e.g.
     * <p>&lt;content:containerList name=\"contentList1\" title=\"Content List\" <b>windowSize=\"5\"</b> &gt;
     * </attriInfo>"
     */
    public void setWindowSize(String windowSize) {
        this.windowSize = windowSize;
        try {
            windowSizeInt = Integer.parseInt(windowSize);
            if (windowSizeInt < 1) {
                windowSizeInt = -1;
            }
        } catch (NumberFormatException nfe) {
            windowSizeInt = -1;
        }
    }

    public String getTitle() {
        try {
            return resolveTitle();
        } catch (final JahiaException e) {
            // ignore
        }
        return this.title;
    }

    public String getStyle() {
        return this.style;
    }

    public String getMethod() {
        return this.method;
    }

    public String getFormName() {
        return this.formName;
    }

    public int getWindowStepInt() {
        return this.windowStepInt;
    }

    public int getWindowSizeInt() {
        return this.windowSizeInt;
    }

    public String getWindowStep() {
        return this.windowStep;
    }

    public String getWindowSize() {
        return this.windowSize;
    }

    public boolean testRights(JahiaData jData) {
        // retrieves the current container list
        parent = (ContainerListTag) findAncestorWithClass(this,
                ContainerListTag.class);
        if (parent != null) {
            containerList = parent.getContainerList();
        }
        if (containerList != null) {
            return (containerList.getContainers().hasNext());
        }
        return false;
    }

    public String getLauncher(JahiaData jData)
            throws JahiaException {
        String value = jData.gui().drawContainerListNextWindowPageURL(
                containerList, windowStepInt, windowSizeInt,
                this.method.equals("post"), parent.getId());
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

        return value;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        containerList = null;
        parent = null;
        title = "Next&gt;&gt;";
        style = "";
        method = "get";
        formName = "";

        windowStepInt = 1;
        windowSizeInt = -1;
        windowStep = null;
        windowSize = null;
        return EVAL_PAGE;
    }

    protected String resolveTitle() throws JahiaException {
        if ((titleKey != null) && (bundleKey != null)) {
            final ResourceBundleMarker marker = new ResourceBundleMarker(bundleKey, titleKey, title);
            final ProcessingContext jParams = Jahia.getThreadParamBean();
            return marker.getValue(jParams.getLocale());

        } else {
            return title;
        }
    }

}
