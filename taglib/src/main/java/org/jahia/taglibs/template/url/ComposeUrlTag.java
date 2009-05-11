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

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.taglibs.ValueJahiaTag;

import javax.servlet.http.HttpServletRequest;

/**
 * Generates a URL to the current page in the given cache mode.
 *
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class ComposeUrlTag extends ValueJahiaTag {

    private static final transient Logger logger = Logger.getLogger(ComposeUrlTag.class);

    private int pageID;
    private boolean fullURL = false;
    private String page;

    public void setPageID(int pageID) {
        this.pageID = pageID;
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
                    buffer.append("javascript:").append(pageID > 0 ? jData.gui().html()
                        .drawLoginLauncher(pageID) : jData.gui().html()
                        .drawLoginLauncher());
                    
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

            if (getVar() != null || getValueID() != null) {
                if (getVar() != null) {
                    pageContext.setAttribute(getVar(), buffer.toString());
                }
                if (getValueID() != null) {
                    pageContext.setAttribute(getValueID(), buffer.toString());
                }
            } else {
                pageContext.getOut().print(buffer.toString());
            }
        } catch (final Exception e) {
            logger.error("Error in PageURLTag", e);
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
        pageID = -1;
        fullURL = false;
        page = null;
    }
}
