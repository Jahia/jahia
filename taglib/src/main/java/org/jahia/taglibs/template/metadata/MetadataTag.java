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
