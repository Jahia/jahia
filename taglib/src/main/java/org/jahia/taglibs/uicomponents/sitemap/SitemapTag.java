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
package org.jahia.taglibs.uicomponents.sitemap;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.core.JahiaType;
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
 * @version 2 march 2009
 */
@SuppressWarnings("serial")
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
                out.print("<div class=\"sitemap\">\n") ;

                if (startPid < 1) {
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
