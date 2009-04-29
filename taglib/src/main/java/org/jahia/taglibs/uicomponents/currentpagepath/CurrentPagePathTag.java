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
package org.jahia.taglibs.uicomponents.currentpagepath;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.gui.GuiBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.settings.SettingsBean;

/**
 * Class CurrentPagePathTag : returns the formated path of the current page
 *
 * @author rfelden
 *         <p/>
 *         <p/>
 *         jsp:tag name="currentPagePath" body-content="empty"
 *         description="Returns the formatted path of the current page.
 *         <p/>
 *         <p><attriInfo>Basically, the output is a list of hyperlinked page titles.
 *         <p/>
 *         </attriInfo></p>"
 */
@SuppressWarnings("serial")
public class CurrentPagePathTag extends AbstractJahiaTag {

    private static final transient Logger logger = Logger.getLogger(CurrentPagePathTag.class);

    private int maxchar = 0;
    private String unreadablePages = "ellipsis";
    private int hideBeginPages = 0;
    private int hideEndPages = 0;
    private boolean showHomePage = false;
    private String separator;


    /**
     * jsp:attribute name="maxchar" required="false" rtexprvalue="true"
     * description="maximum length of each displayed page title.
     * <p><attriInfo>Cuts and prepares the page title string for display to a specified length, by
     * appending "..." characters at the end and encoding the string for
     * HTML output (by replacing all non ISO-8859-1 characters with \&#XX;
     * encoding).
     * </attriInfo></p>"
     */
    public void setMaxchar(int maxChar) {
        this.maxchar = maxChar;
    }

    /**
     * jsp:attribute name="unreadablePages" required="false" rtexprvalue="true"
     * description="what to display when a page is unreachable
     * <p><attriInfo>"ellipsis" (default) means that a sequel of unreachable pages will be replaced
     * by a single "...", "display" means that everything will be displayed and if the title is not
     * available there will be "n.d." instead. Any other value will result in no display at all.
     * </attriInfo></p>"
     */
    public void setUnreadablePages(String unreadableDisplay) {
        this.unreadablePages = unreadableDisplay;
    }

    public void setHideBeginPages(int hideBeginPages) {
        this.hideBeginPages = hideBeginPages;
    }

    public void setHideEndPages(int hideEndPages) {
        this.hideEndPages = hideEndPages;
    }

    public void setShowHomePage(boolean show) {
        this.showHomePage = show;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public int doStartTag() {
        if (cssClassName == null) {
            cssClassName = "default_pagePath";
        }

        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        try {
            ProcessingContext jParams = jData.getProcessingContext();

            List<ContentPage> vPath = ServicesRegistry.getInstance().getJahiaPageService().
                    getContentPagePath(jData.page().getID(), jParams.getEntryLoadRequest(),
                            jParams.getOperationMode(), jParams.getUser());

            ContentPage homePage = null;
            if (hideBeginPages > 0 && showHomePage) {
                homePage = vPath.get(0);
            }

            // remove unwanted entries
            for (int i = 0; i < hideBeginPages; i++) {
                try {
                    vPath.remove(0);
                } catch (ArrayIndexOutOfBoundsException e) {
                    break; // List is empty
                }
            }
            for (int i = 0; i < hideEndPages; i++) {
                try {
                    vPath.remove(vPath.size() - 1);
                } catch (ArrayIndexOutOfBoundsException e) {
                    break; // List is empty
                }
            }

            if (homePage != null) {
                vPath.add(0, homePage);
            }
            StringBuffer path = new StringBuffer("<ul class=\"").append(cssClassName).append("\">");
            boolean isAccessible = true;
            boolean unreachablePageDisplayed = false;
            // check page status
            Integer pageState = null;

            boolean checkAcl = SettingsBean.getInstance().isCheckAclInPagePath();

            for (ContentPage thePage : vPath) {
                if (thePage != null) {
                    if (checkAcl) {
                        if (!thePage.isAclSameAsParent()) {
                            isAccessible = thePage.checkReadAccess(jParams.getUser());
                            unreachablePageDisplayed = false;
                        }
                    }
                    Integer currentPageState = thePage.getLanguagesStates().get(jParams.getLocale().toString());
                    if (pageState == null || !pageState.equals(currentPageState)) {
                        pageState = currentPageState;
                        unreachablePageDisplayed = false;
                    }
                    // Check current page to remove link on title.
                    boolean isCurrentPage = false;
                    if (thePage.equals(jParams.getThePage().getContentPage())) {
                        isCurrentPage = true;
                    }

                    final String title = thePage.getTitle(jParams);
                    if (title != null && isAccessible) {
                        path.append("<li>");
                        if (!isCurrentPage) {
                            path.append("<a");
                            path.append(" href=\"");
                            try {
                                path.append(thePage.getURL(jData.getProcessingContext()));
                            } catch (JahiaException e) {
                                logger.error(e.getMessage(), e);
                            }
                            path.append("\">");
                        }
                        if (this.maxchar == 0) {
                            path.append(title);
                        } else {
                            path.append(GuiBean.glueTitle(title, this.maxchar));
                        }
                        if (!isCurrentPage) {
                            path.append("</a>");
                        }
                        if (separator != null) {
                            path.append(separator);
                        }
                        path.append("</li>\n");
                    } else {
                        if (unreadablePages.equals("display")) {
                            path.append("<li>");
                            if (!isCurrentPage) {
                                path.append("<a>");
                            }
                            path.append(title != null ? title : "n.d.");
                            if (!isCurrentPage) {
                                path.append("</a>");
                            }
                            path.append("</li>");
                        } else if (unreadablePages.equals("ellipsis") && !unreachablePageDisplayed) {
                            path.append("<li><a>...</a></li>");
                            unreachablePageDisplayed = true;
                        } else {
                            // don't display anything
                        }
                    }

                }
            }
            JspWriter out = pageContext.getOut();
            path.append("</ul>\n");
            out.print(path.toString());

        } catch (IOException ioe) {
            logger.error("CurrentPagePathTag: doStartTag ", ioe);
        } catch (JahiaException je) {
            logger.error("CurrentPagePathTag: doStartTag ", je);
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        cssClassName = null;
        separator = null;
        maxchar = 0;
        unreadablePages = "ellipsis";
        hideBeginPages = 0;
        hideEndPages = 0;
        return EVAL_PAGE;
    }


}