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

package org.jahia.taglibs.uicomponents.portlets;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.gui.HTMLToolBox;
import org.jahia.gui.GuiBean;
import org.jahia.data.beans.RequestBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRPortletNode;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.Node;
import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jul 10, 2009
 * Time: 10:46:15 AM
 */
public class PortletRenderTag extends AbstractJahiaTag {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PortletRenderTag.class);

    private Node mashupNode;
    private boolean ajaxRendering = true;
    private int windowId;

    private GuiBean guiBean = null;
    private ProcessingContext processingContext = null;

    private HTMLToolBox htmlToolBox = null;
    private RequestBean requestBean = null;


    public int doStartTag() {
        try {
            if(!(mashupNode instanceof JCRNodeWrapper)){
                logger.error("mashupNode must be an instance of JCRNodeWrapper");
                return SKIP_BODY;               
            }
            requestBean = (RequestBean) pageContext.findAttribute("currentRequest");
            processingContext = requestBean.getProcessingContext();
            guiBean = new GuiBean(processingContext);
            htmlToolBox = new HTMLToolBox(guiBean, processingContext);
            htmlToolBox.drawMashup(new JCRPortletNode((JCRNodeWrapper) mashupNode), ajaxRendering, windowId, pageContext.getOut());

        } catch (Exception e) {
            logger.error(e, e);
        }
        return SKIP_BODY;
    }


    @Override
    public int doEndTag() throws JspException {
        super.doEndTag();
        mashupNode = null;
        ajaxRendering = false;
        windowId=-1;
        processingContext = null;
        requestBean = null;
        guiBean = null;
        htmlToolBox = null;
        return EVAL_PAGE;
    }

    public Node getMashupNode() {
        return mashupNode;
    }

    public void setMashupNode(Node mashupNode) {
        this.mashupNode = mashupNode;
    }

    public boolean isAjaxRendering() {
        return ajaxRendering;
    }

    public void setAjaxRendering(boolean ajaxRendering) {
        this.ajaxRendering = ajaxRendering;
    }

    public int getWindowId() {
        return windowId;
    }

    public void setWindowId(int windowId) {
        this.windowId = windowId;
    }
}
