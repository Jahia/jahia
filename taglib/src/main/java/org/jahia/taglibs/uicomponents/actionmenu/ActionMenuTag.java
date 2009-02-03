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

package org.jahia.taglibs.uicomponents.actionmenu;

import org.apache.log4j.Logger;
import org.jahia.data.beans.RequestBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.taglibs.AbstractJahiaTag;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 22 janv. 2008 - 15:40:51
 */
public class ActionMenuTag extends AbstractJahiaTag {

    private static final transient Logger logger = Logger.getLogger(ActionMenuTag.class);

    private String contentObjectName;
    private String objectKey;
    private RequestBean requestBean;
    private String namePostFix;
    private String labelKey;
    private String iconStyle;

    private boolean displayActionMenu = true;

    public void setContentObjectName(String contentObjectName) {
        this.contentObjectName = contentObjectName;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public void setNamePostFix(String namePostFix) {
        this.namePostFix = namePostFix;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public void setIconStyle(String iconStyle) {
        this.iconStyle = iconStyle;
    }

    public int doStartTag() {
        requestBean = (RequestBean) pageContext.findAttribute("currentRequest");
        ProcessingContext jParams = requestBean.getProcessingContext();

        // if non edit mode, useless
        if (!requestBean.isEditMode()) {
            return EVAL_BODY_BUFFERED;
        }

        // sert default CSS
        if (cssClassName == null) {
            cssClassName = ActionMenuOutputter.DEFAULT_CSS;
        }

        // write output with enclosing div
        try {
            final StringBuffer buf = new StringBuffer();
            buf.append("<div class=\"").append(cssClassName).append("\">");
            final String actionMenu = new ActionMenuOutputter(jParams, pageContext, null, contentObjectName, objectKey,
                    getResourceBundle(), namePostFix, labelKey, iconStyle).getOutput();
            if (actionMenu == null || actionMenu.length() == 0) {
                displayActionMenu = false;
                return EVAL_BODY_BUFFERED;
            }
            buf.append(actionMenu);
            pageContext.getOut().println(buf.toString());
        } catch (IOException e) {
            logger.error("impossible to write using JSP writer", e);
        } catch (ClassNotFoundException e) {
            logger.error("error writing action menu for object " + objectKey, e);
        } catch (JahiaException e) {
            logger.error("error writing action menu for object " + objectKey, e);
        }
        // end tag
        return EVAL_BODY_BUFFERED;
    }

    // Body is evaluated one time, so just writes it on standard output
    public int doAfterBody() {
        try {
            bodyContent.writeOut(bodyContent.getEnclosingWriter());
        } catch (IOException ioe) {
            logger.error("Error:", ioe);
        }
        return SKIP_BODY;
    }

    // reset tag attributes (pool reuse)
    public int doEndTag() {
        // if non edit mode, useless
        if (requestBean.isEditMode() && displayActionMenu) {
            try {
                pageContext.getOut().println("</div>");
            } catch (IOException ioe) {
                logger.error("impossible to write using JSP writer", ioe);
            }
        }

        cssClassName = null;
        contentObjectName = null;
        objectKey = null;
        namePostFix = null;
        labelKey = null;
        displayActionMenu = true;
        super.resetState();
        
        return EVAL_PAGE;
    }

}
