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
package org.jahia.taglibs.template.metadata;

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.ContentBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * @author Cédric Mailleux
 */
@SuppressWarnings("serial")
public class MetadataTag extends AbstractJahiaTag {

    private static final transient Logger logger = Logger.getLogger(MetadataTag.class);
    private ContentBean contentBean;
    private String metadataName;
    private boolean asDate = false;
    private String var;

    @Override
    public int doStartTag() throws JspException {
        try {
            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final ProcessingContext jParams = jData.getProcessingContext();
            if (var != null) {
                if (asDate) {
                    final Date date = contentBean.getContentObject().getMetadataAsDate(metadataName, jParams);
                    pageContext.setAttribute(var, date);
                }
                else {
                     pageContext.setAttribute(var, contentBean.getContentObject().getMetadataValue(metadataName, jParams, ""));
                }
            } else {
                if (asDate) {
                     pageContext.getOut().write(SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, jData.getProcessingContext().getLocale()).format(contentBean.getContentObject().getMetadataAsDate(metadataName, jParams)));
                }
                else {
                    pageContext.getOut().write(contentBean.getContentObject().getMetadataValue(metadataName, jParams, ""));
                }
            }
        } catch (JahiaException e) {
            logger.error("Error in MetadataTag", e);
        } catch (IOException e) {
            logger.error("Error in MetadataTag", e);
        }
        return SKIP_BODY;
    }

    public void setContentBean(ContentBean contentBean) {
        this.contentBean = contentBean;
    }

    public void setMetadataName(String metadataName) {
        this.metadataName = metadataName;
    }

    public void setAsDate(boolean asDate) {
        this.asDate = asDate;
    }

    public void setVar (String var) {
        this.var = var;
    }

    @Override
    public int doEndTag() throws JspException {
        contentBean = null;
        metadataName = null;
        asDate = false;
        var = null;
        return EVAL_PAGE;
    }
}
