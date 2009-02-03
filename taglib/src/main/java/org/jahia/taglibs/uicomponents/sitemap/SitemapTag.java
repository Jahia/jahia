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

package org.jahia.taglibs.uicomponents.sitemap;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaType;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.RequestBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.PageLoadFlags;
import org.jahia.services.pages.PageInfoInterface;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.fields.ContentField;
import org.jahia.taglibs.AbstractJahiaTag;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 8 janv. 2008
 */
public class SitemapTag extends AbstractJahiaTag {

    private static final transient Logger logger = Logger.getLogger(SitemapTag.class);

    private int startPid = -1 ;
    private int maxDepth = 3 ;
    private boolean ajax = false ;
    private boolean showRoot = false ;
    private boolean enableDescription = true ;

    public void setStartPid(int startPid) {
        this.startPid = startPid;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setShowRoot(boolean showRoot) {
        this.showRoot = showRoot;
    }

    public void setAjax(boolean ajax) {
        this.ajax = ajax ;
    }

    public void setEnableDescription(boolean enable) {
        enableDescription = enable ;
    }

    public int doStartTag() {
        JspWriter out = pageContext.getOut();
        if (ajax) {
            final RequestBean requestBean = (RequestBean) pageContext.findAttribute("currentRequest");
            ajax = requestBean.isLogged() ;
        }
        if (ajax) {
            // use the gwt module
            try {
                StringBuffer div = new StringBuffer("<div id=\"default_sitemap\" class=\"sitemap\" ") ;
                div.append(JahiaType.JAHIA_TYPE).append("=\"").append(JahiaType.SITEMAP).append("\"></div>") ;
                out.print(div.toString()) ;
            } catch (IOException e) {
                logger.error("Could not write to output", e);
            }

        } else {
            // use the static way
            ServletRequest request = pageContext.getRequest();
            JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            ProcessingContext jParams = jData.getProcessingContext() ;

            JahiaPage startPage ;

            int depth = 1 ;

            try {
                out.print("<div id=\"default_sitemap\" class=\"sitemap\">\n") ;

                if (startPid == -1) {
                    startPage = jParams.getSite().getHomePage() ;
                    startPid = startPage.getID() ;
                } else {
                    startPage = ServicesRegistry.getInstance().getJahiaPageService().lookupPage(startPid, jParams) ;
                }

                boolean rootShown = showRoot && (startPage.getPageType() != PageInfoInterface.TYPE_URL && startPage.getPageType() != PageInfoInterface.TYPE_LINK)
                        && (startPage.hasActiveEntries() || startPage.checkWriteAccess(jParams.getUser()));
                if (rootShown)  {
                    out.print("<ul>\n") ;
                    out.print("<li>") ;
                    out.print(new StringBuffer().append("<a href=\"").append(startPage.getURL(jParams)).append("\">").append(startPage.getTitle()).append("</a>").toString()) ;
                }

                printSubpages(jData, startPage.getID(), depth);

                if (rootShown) {
                    out.print("</li>\n<ul>\n") ;
                }
                out.print("</div>\n") ;
            } catch (JahiaException e) {
                logger.error("Hierarchy could not be browsed", e);
            } catch (IOException e) {
                logger.error("Could not write to output", e);
            }
        }

        return SKIP_BODY ;
    }

        /**
     * Recursive method to go through pages hierarchy using a specific container list (attribute containerListName).
     * @param jData JahiaData
     * @param pageId the page id where to get the container list
     * @param depth the current depth
     * @throws JahiaException exception retrieving cache or containers
     * @throws IOException JSP writer exception
     */
    private void printSubpages(JahiaData jData, int pageId, int depth) throws JahiaException, IOException {

        if (pageId < 1) {
            logger.error("Incorrect page ID: " + pageId);
            throw new IllegalArgumentException("attribute pageID cannot be < 1 (is " + pageId + ")");
        }

        if (maxDepth != -1 && depth++ > maxDepth) {
            return ;
        }

        JspWriter out = pageContext.getOut();

        ProcessingContext jParams = jData.getProcessingContext();

        List<JahiaPage> childs = ServicesRegistry.getInstance().getJahiaPageService().getPageChilds(pageId, PageLoadFlags.ALL, jParams.getUser()) ;

        if (childs.isEmpty()) {
            return ;
        }

        boolean begin = true ;

        // sort children
        for (JahiaPage page: childs) {
            if (page.getPageType() != PageInfoInterface.TYPE_URL && page.getPageType() != PageInfoInterface.TYPE_LINK) {
                if (page.hasActiveEntries() || page.checkWriteAccess(jParams.getUser())) {

                    // print page
                    if (begin) {
                        out.print("<ul>\n") ;
                        begin = false ;
                    }

                    out.print("<li>") ;

                    String desc = "" ;
                    if (enableDescription) {
                        ContentField descField = page.getContentPage().getMetadata(CoreMetadataConstant.DESCRIPTION) ;
                        if (descField != null) {
                            desc = descField.getValue(jParams) ;
                        }
                        if (desc == null) {
                            desc = "" ;
                        }
                    }
                    out.print(new StringBuffer().append("<a href=\"").append(page.getURL(jParams)).append("\">").append(page.getTitle()).append("</a><span class=\"page_description\">").append(desc).append("</span>").toString()) ;
                    printSubpages(jData, page.getID(), depth);
                    out.print("</li>\n") ;
                }
            }
        }

        if (!begin) {
            out.print("</ul>\n") ;
        }
    }


    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        startPid = -1 ;
        showRoot = false ;
        maxDepth = -1;
        ajax = false ;
        return EVAL_PAGE;
    }

}
