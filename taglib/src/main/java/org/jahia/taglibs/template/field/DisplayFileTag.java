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
package org.jahia.taglibs.template.field;

import org.jahia.data.beans.FieldValueBean;
import org.jahia.data.files.JahiaFileField;
import org.jahia.data.fields.JahiaField;
import org.jahia.utils.FileUtils;
import org.jahia.exceptions.JahiaException;

import javax.servlet.jsp.JspWriter;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
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
            if (bean instanceof FieldValueBean) {
                theFile = ((FieldValueBean<?>) bean).getFile();
            } else if (bean instanceof JahiaFileField) {
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
                final StringBuilder buff = new StringBuilder();
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
