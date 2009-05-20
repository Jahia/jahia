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
import org.jahia.data.fields.JahiaField;
import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;

import javax.servlet.jsp.JspWriter;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
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
