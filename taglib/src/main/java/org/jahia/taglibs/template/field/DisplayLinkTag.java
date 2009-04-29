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
@SuppressWarnings("serial")
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
                thePageBean = ((FieldValueBean<?>) bean).getPage();
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
