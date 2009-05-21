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
