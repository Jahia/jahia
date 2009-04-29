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
package org.jahia.taglibs.template.url;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

/**
 * Generates a URL to the current page in the given cache mode
 *
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class ComposeUrlTag extends AbstractJahiaTag {

    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ComposeUrlTag.class);

    private int pageID;
    private String valueID;
    private boolean fullURL = false;
    private String page;

    public void setPageID(int pageID) {
        this.pageID = pageID;
    }

    public void setValueID(String valueID) {
        this.valueID = valueID;
    }

    public void setFullURL(boolean fullURL) {
        this.fullURL = fullURL;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public int doStartTag() {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();

        try {
            final StringBuilder buffer = new StringBuilder(128);
            if (fullURL) {
                buffer.append(request.getScheme());
                buffer.append("://");
                buffer.append(request.getServerName());
                buffer.append(":");
                buffer.append(request.getServerPort());
            }

            if (page != null && page.length() > 0) {
                if (page.equals("logout")) {
                    if (pageID > 0) {
                        buffer.append(jData.gui().html().drawLogoutLauncher(pageID));
                    } else {
                        buffer.append(jData.gui().html().drawLogoutLauncher());
                    }
                    
                } else if (page.equals("login")) {                     	
                	  final String popupLoginURL;
                    if (pageID > 0) {
                        popupLoginURL = jData.gui().drawPopupLoginUrl(pageID);
                    } else {
                        popupLoginURL = jData.gui().drawPopupLoginUrl();
                    }
                    final String params = "width=450,height=500,left=10,top=10,resizable=yes,scrollbars=no,status=no";
                    buffer.append("window.open('").append(popupLoginURL).append("','Login','").append(params).append("')");

                } else if (page.equals("mySettings")) {
                    buffer.append(jData.gui().html().drawMySettingsLauncher());
                    
                } else if (page.equals("administration")) {
                    buffer.append(jData.gui().html().drawAdministrationLauncher());
                    
                } else if (page.equals("siteMap")) {
                    buffer.append(jData.gui().html().drawSiteMapLauncher());
                    
                } else if (page.equals("search")) {
                    buffer.append(jData.gui().html().drawSearchLauncher());

                } else {
                    logger.warn("Invalid value for 'page' attribute: \"" + page + "\". Valid values are 'administration', 'login', 'logout', 'mySettings', 'siteMap', 'search'");
                    buffer.append(jParams.composePageUrl(jData.page()));
                }

            } else if (pageID > 0) {
                buffer.append(jParams.composePageUrl(pageID));

            } else {
                buffer.append(jParams.composePageUrl(jData.page()));
            }

            if (valueID != null && valueID.length() > 0) {
                pageContext.setAttribute(valueID, buffer.toString());
            } else {
                final JspWriter out = pageContext.getOut();
                out.print(buffer.toString());
            }
        } catch (final Exception e) {
            logger.error("Error in PageURLTag", e);
        }

        return SKIP_BODY;
    }

    public int doEndTag() {
        valueID = null;
        pageID = -1;
        fullURL = false;
        page = null;
        return EVAL_PAGE;
    }
}
