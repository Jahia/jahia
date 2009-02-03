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

import org.jahia.data.files.JahiaFileField;
import org.jahia.data.fields.JahiaField;
import org.jahia.utils.FileUtils;
import org.jahia.exceptions.JahiaException;

import javax.servlet.jsp.JspWriter;

/**
 * @author Xavier Lawrence
 */
public class DisplayFileTag extends AbstractFieldTag {
    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DisplayFileTag.class);

    private String file;
    private String title;
    private boolean useFilePictoAsCssClassName = true;
    private boolean displayDetails = true;
    private int maxChar = -1;
    private String containerName;

    public void setFile(String file) {
        this.file = file;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUsePictoAsCssClassName(boolean usePictoAsCssClassName) {
        this.useFilePictoAsCssClassName = usePictoAsCssClassName;
    }

    public void setUseFilePictoAsCssClassName(boolean useFilePictoAsCssClassName) {
        this.useFilePictoAsCssClassName = useFilePictoAsCssClassName;
    }

    public void setDisplayDetails(boolean displayDetails) {
        this.displayDetails = displayDetails;
    }

    public void setMaxChar(int maxChar) {
        this.maxChar = maxChar;
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

            if (theFile != null && theFile.isDownloadable()) {
                final StringBuffer buff = new StringBuffer();
                buff.append("<a");

                if (useFilePictoAsCssClassName) {
                    buff.append(" class=\"");
                    buff.append(FileUtils.getFileIcon(theFile.getFileFieldTitle()));
                    buff.append("\"");

                } else if (cssClassName != null && cssClassName.length() > 0) {
                    buff.append(" class=\"");
                    buff.append(extractDefaultValue(cssClassName));
                    buff.append("\"");
                }

                if (title != null && title.length() > 0) {
                    buff.append(" title=\"");
                    buff.append(extractDefaultValue(title));
                    buff.append("\"");

                } else {
                    buff.append(" title=\"");
                    buff.append(theFile.getFileFieldTitle());
                    buff.append("\"");
                }

                buff.append(" href=\"");
                buff.append(theFile.getDownloadUrl());
                buff.append("\">");

                final String title;
                if (maxChar > 0) {
                    final String tmp = theFile.getFileFieldTitle();
                    if (tmp != null && tmp.length() > maxChar) {
                        title = tmp.substring(0, maxChar - 3) + "...";
                    } else {
                        title = theFile.getFileFieldTitle();
                    }

                } else {
                    title = theFile.getFileFieldTitle();
                }
                buff.append(title);
                buff.append("</a>");

                if (displayDetails) {
                    buff.append("<span class=\"fileDetails\">");

                    buff.append("<span class=\"fileSize\"");
                    buff.append(theFile.getFormatedSize());
                    buff.append("</span>");

                    buff.append("<span class=\"fileLastModifDate\"");
                    buff.append(theFile.getFormatedLastModifDate());
                    buff.append("</span>");

                    buff.append("</span>");
                }

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
        useFilePictoAsCssClassName = true;
        displayDetails = true;
        title = null;
        maxChar = -1;
        containerName = null;
        return EVAL_PAGE;
    }
}
