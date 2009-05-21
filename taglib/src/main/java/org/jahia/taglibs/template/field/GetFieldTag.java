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
package org.jahia.taglibs.template.field;

import org.jahia.taglibs.ValueJahiaTag;
import org.jahia.services.fields.ContentField;
import org.jahia.data.beans.FieldBean;
import org.jahia.params.ProcessingContext;
import org.apache.log4j.Logger;

/**
 * Allow to get any field from its ID.
 */
@SuppressWarnings("serial")
public class GetFieldTag extends ValueJahiaTag {
    private static transient final Logger logger = Logger.getLogger(GetFieldTag.class);

    private int fieldID;

    public void setFieldID(int fieldID) {
        this.fieldID = fieldID;
    }

    public int doStartTag() {
        try {
            final ContentField cf = ContentField.getField(fieldID);
            if (cf == null) return SKIP_BODY;

            final ProcessingContext jParams = getProcessingContext();

            final FieldBean cfBean = new FieldBean(cf.getJahiaField(jParams.getEntryLoadRequest()),
                    jParams);
            if (getVar() != null) {
                pageContext.setAttribute(getVar(), cfBean);
            }
            if (getValueID() != null) {
                pageContext.setAttribute(getValueID(), cfBean);
            }
        } catch (Exception e) {
            logger.error("Error in GetFieldTag", e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        resetState();
        return EVAL_PAGE;
    }
    
    @Override
    protected void resetState() {
        super.resetState();
        fieldID = -1;
    }
}