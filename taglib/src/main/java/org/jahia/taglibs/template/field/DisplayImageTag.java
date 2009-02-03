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

package org.jahia.taglibs.template.field;

import org.jahia.data.fields.JahiaField;
import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;

import javax.servlet.jsp.JspWriter;

/**
 * @author Xavier Lawrence
 */
public class DisplayImageTag extends AbstractFieldTag {
    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DisplayImageTag.class);

    private String file;
    private String align;
    private String title;
    private String alt;
    private String containerName;

    public void setFile(String file) {
        this.file = file;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public int doStartTag() {
        try {
            final Object bean = pageContext.getAttribute(file);
            JahiaFileField theFile = null;
            if (bean instanceof JahiaFileField) {
                theFile = (JahiaFileField) bean;     
            }

            if (theFile == null) {
                try {
                    final JahiaField theField = loadField(file, containerName);
                    if (theField != null) {
                        theFile = (JahiaFileField) theField.getObject();
                    }
                } catch (JahiaException je) {
                    logger.debug("Cannot load field", je);
                }
            }

            if (theFile != null && theFile.isImage() && theFile.isDownloadable()) {
                final StringBuffer buff = new StringBuffer();
                buff.append("<img");
                if (cssClassName != null && cssClassName.length() > 0) {
                    buff.append(" class=\"");
                    buff.append(extractDefaultValue(cssClassName));
                    buff.append("\"");
                }

                if (align != null && align.length() > 0) {
                    buff.append(" align=\"");
                    buff.append(extractDefaultValue(align));
                    buff.append("\"");
                }

                if (title != null && title.length() > 0) {
                    buff.append(" title=\"");
                    buff.append(title);
                    buff.append("\"");
                }

                buff.append(" src=\"");
                buff.append(theFile.getDownloadUrl());
                buff.append("\"");

                if (alt != null) {
                    buff.append(" alt=\"");
                    buff.append(alt);
                    buff.append("\"");
                } else {
                    buff.append(" alt=\"");
                    buff.append(theFile.getFileFieldTitle());
                    buff.append("\"");
                }

                buff.append(" />");

                final JspWriter out = pageContext.getOut();
                out.print(buff.toString());
            }

        } catch (Exception e) {
            logger.error(e, e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        file = null;
        align = null;
        title = null;
        alt = null;
        containerName = null;
        return EVAL_PAGE;
    }


}
