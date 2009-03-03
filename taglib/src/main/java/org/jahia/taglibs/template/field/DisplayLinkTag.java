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

import org.jahia.data.beans.FieldValueBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.files.JahiaFileField;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.pages.PageInfoInterface;
import org.jahia.services.pages.JahiaPage;

import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Xavier Lawrence
 */
public class DisplayLinkTag extends AbstractFieldTag {
    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DisplayLinkTag.class);

    private String linkBody;
    private String page;
    private boolean openExternalLinkInNewWindow = true;
    private int maxChar = -1;
    private String image;
    private String title;
    private String alt;
    private String containerName;

    public void setLinkBody(String linkBody) {
        this.linkBody = linkBody;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setOpenExternalLinkInNewWindow(boolean openExternalLinkInNewWindow) {
        this.openExternalLinkInNewWindow = openExternalLinkInNewWindow;
    }

    public void setMaxChar(int maxChar) {
        this.maxChar = maxChar;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public int doStartTag() {
        try {
            final Object bean = pageContext.getAttribute(page);
            PageBean thePageBean = null;
            if (bean instanceof FieldValueBean) {
                thePageBean = ((FieldValueBean) bean).getPage();
            } else if (bean instanceof PageBean) {
                thePageBean = (PageBean) bean;

            } else if (bean instanceof JahiaPage) {
                final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
                final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
                thePageBean = new PageBean((JahiaPage) bean, jData.getProcessingContext());
            }

            if (thePageBean == null) {
                try {
                    final JahiaField theField = loadField(page, containerName);
                    if (theField != null) {
                        final JahiaPage thePage = (JahiaPage) theField.getObject();
                        if (thePage != null) {
                            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
                            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
                            thePageBean = new PageBean(thePage, jData.getProcessingContext());
                        }
                    }
                } catch (JahiaException je) {
                    logger.debug("Cannot load field", je);
                }
            }

            final String linkValue;
            if (image != null && image.length() > 0) {
                final JahiaFileField theFile = (JahiaFileField) pageContext.getAttribute(image);
                final StringBuffer image = new StringBuffer();
                image.append("<img");
                if (title != null && title.length() > 0) {
                    image.append(" title=\"");
                    image.append(title);
                    image.append("\"");
                }

                image.append(" src=\"");
                image.append(theFile.getDownloadUrl());
                image.append("\"");

                if (alt != null) {
                    image.append(" alt=\"");
                    image.append(alt);
                    image.append("\"");
                } else {
                    image.append(" alt=\"");
                    image.append(theFile.getFileFieldTitle());
                    image.append("\"");
                }

                image.append(" />");
                linkValue = image.toString();

            } else if (linkBody != null && linkBody.length() > 0) {
                if (maxChar > 0 && linkBody.length() > maxChar) {
                    linkValue = linkBody.substring(0, maxChar - 3) + "...";
                } else {
                    linkValue = linkBody;
                }

            } else if (thePageBean != null) {
                final String tmp = thePageBean.getHighLightDiffTitle();
                if (maxChar > 0 && tmp != null && tmp.length() > maxChar) {
                    linkValue = tmp.substring(0, maxChar - 3) + "...";
                } else {
                    linkValue = tmp;
                }

            } else {
                linkValue = "N/A";
            }

            final JspWriter out = pageContext.getOut();
            if (thePageBean != null) {
                final StringBuffer buff = new StringBuffer();
                if (thePageBean.getPageType() == PageInfoInterface.TYPE_URL) {
                    buff.append("<span class=\"externallink\">");
                } else if (thePageBean.getPageType() == PageInfoInterface.TYPE_LINK) {
                    buff.append("<span class=\"link\">");
                } else {
                    buff.append("<span class=\"page\">");
                }

                buff.append("<a href=\"");
                buff.append(thePageBean.getUrl());
                buff.append("\"");

                if (cssClassName != null && cssClassName.length() > 0) {
                    buff.append(" class=\"");
                    buff.append(cssClassName);
                    buff.append("\"");
                }

                if (openExternalLinkInNewWindow && thePageBean.getPageType() == PageInfoInterface.TYPE_URL) {
                    buff.append(" target=\"_blank\"");
                }
                buff.append(">");
                buff.append(linkValue);
                buff.append("</a></span>");
                out.print(buff.toString());

            } else {
                out.print(linkValue);
            }

        } catch (final Exception e) {
            logger.error("Error in DisplayLinkTag", e);
        }

        return SKIP_BODY;
    }

    public int doEndTag() {
        linkBody = null;
        page = null;
        cssClassName = null;
        openExternalLinkInNewWindow = true;
        maxChar = -1;
        image = null;
        title = null;
        alt = null;
        containerName = null;
        return EVAL_PAGE;
    }
}
