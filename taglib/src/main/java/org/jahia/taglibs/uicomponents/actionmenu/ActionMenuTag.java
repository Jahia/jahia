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
@SuppressWarnings("serial")
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
