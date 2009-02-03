<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page import="org.jahia.services.sitemap.JahiaSiteMapService" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.data.viewhelper.sitemap.TreeSiteMapViewHelper" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.exceptions.JahiaException" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.engines.shared.BigText_Field" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="java.util.HashMap" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%!
    /*
    * This is a model for a generic tree that can be used for several purposes.
    * For example, it may be used to display the tree representing the sitemap
    *
    * This JSP generates the necessary paramters to feed in the javascript tree definition
    * contained in dtree.js
    */

    private static final org.apache.log4j.Logger treeLogger =
            org.apache.log4j.Logger.getLogger("jsp.jahia.components.tree");

    private final static ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private final static JahiaSiteMapService siteMapService =
            servicesRegistry.getJahiaSiteMapService();

    public static final int PAGES = 0;
    public static final int FILES = 1;

    /**
     * Returns a String containing the parameters to initialize the dtree javascript object.
     *
     * @param jData JahiaData instance
     * @param currentLanguageCode Language of the node name
     * @param displayLevel The starting depth of the tree
     * @param checkboxes TRUE will display checkboxes in front of the nodes
     * @param pageState TRUE will generate extra parameters showing the page states (used by FCK editor)
     * @param nodeType What are the node showing ?  (PAGES or FILES)
     * @param canExpandAll TRUE will add an expandAll and a collapseAll button
     *
     * @throws JahiaException If something goes wrong while getting the tree information
     */
    public String getTree(final JahiaData jData,
                          final String currentLanguageCode,
                          final int displayLevel,
                          final boolean checkboxes,
                          final boolean pageState,
                          final int nodeType,
                          final boolean canExpandAll) throws JahiaException {
        treeLogger.debug("Getting tree with params: displayLevel = " + displayLevel + ", checkboxes = " +
                checkboxes + ", pageState = " + pageState);

        final StringBuffer result = new StringBuffer(128);
        result.append("[[0,0,\'root\','/'],");
        final ProcessingContext processingContext = jData.getProcessingContext();

        final int pageInfosFlag;
        if (jData.gui().isNormalMode()) {
            pageInfosFlag = ContentPage.ACTIVE_PAGE_INFOS;
        } else {
            pageInfosFlag = ContentPage.ACTIVE_PAGE_INFOS | ContentPage.STAGING_PAGE_INFOS;
        }

        try {
            final ContentPage theContentPage = servicesRegistry.getJahiaPageService().
                    lookupContentPage(processingContext.getSite().getHomePageID(), false);

            final TreeSiteMapViewHelper treeJahiaSiteMap = (TreeSiteMapViewHelper) siteMapService.
                    getTreeSiteMapViewHelper(processingContext.getUser(), theContentPage,
                            processingContext.getSessionID(), pageInfosFlag,
                            currentLanguageCode, displayLevel - 1 );

            for (int i = 0; i < treeJahiaSiteMap.size(); i++) {
                final ContentPage contentPage = treeJahiaSiteMap.getContentPage(i);
                if (contentPage == null) {
                    continue;
                }

                final int pid = contentPage.getID();
                String pageTitle = treeJahiaSiteMap.getPageTitle(i, currentLanguageCode);

                if (pageTitle == null || pageTitle.length() == 0) {
                    pageTitle = new StringBuffer().append("[pid = ").append(pid).append("]").toString();
                }

                final StringBuffer pageUrl = new StringBuffer();
                pageUrl.append(((ParamBean) processingContext).getRequest().getContextPath()).
                        append("/lang/").append(currentLanguageCode).append("/pid/").append(pid);

                addParamsAsString(result, treeJahiaSiteMap, i, pageTitle, pageUrl.toString(),
                        pid, currentLanguageCode, null, processingContext, null, pageState);

            }
            result.deleteCharAt(result.length() - 1);
            result.append("]");

        } catch (JahiaException je) {
            throw je;
        }

        treeLogger.debug("Result: " + result);
        return result.toString();
    }

    /**
     * calls getTree with the given parameters + pageState = false & nodeType = PAGES
     */
    public String getSiteMap(final JahiaData jData,
                             final String currentLanguageCode,
                             final int displayLevel,
                             final boolean checkboxes,
                             final boolean canExpandAll) throws JahiaException {

        return getTree(jData, currentLanguageCode, displayLevel, checkboxes, false, PAGES, canExpandAll);
    }

    /**
     * calls getTree with the given parameters + nodeType = PAGES
     */
    public String getSiteMap(final JahiaData jData,
                             final String currentLanguageCode,
                             final int displayLevel,
                             final boolean checkboxes,
                             final boolean pageState,
                             final boolean canExpandAll) throws JahiaException {

        return getTree (jData, currentLanguageCode, displayLevel, checkboxes, pageState, PAGES, canExpandAll);
    }

    /**
     * Adds a String containing the javascript parameters to the Result StringBuffer
     */
    private void addParamsAsString(final StringBuffer buff,
                                   final TreeSiteMapViewHelper treeJahiaSiteMap,
                                   final int i,
                                   final String pageTitle,
                                   final String pageUrl,
                                   final int pageId,
                                   final String currentLanguageCode,
                                   final JahiaField theField,
                                   final ProcessingContext jParams,
                                   final Map engineMap,
                                   final boolean showPageState) throws JahiaException {

        buff.append("[").append((treeJahiaSiteMap.getPageLevel(i) + 1)).
                append(",").append((i + 1)).append(",\'").
                append(pageTitle.replaceAll("'", "\\\\'")).
                append("\','").append(pageUrl.toString().replaceAll("'", "\\\\'")).append("\',");

        if (showPageState) {
            buff.append("\'").
                    append(BigText_Field.getInstance().getPageState(pageId, currentLanguageCode)).
                    append("\',");
        }
        if (theField != null && engineMap != null) {
            buff.append("\'").
                    append(BigText_Field.getInstance().getFieldGroupsNotHavingAccessOnPageAsString(
                            pageId, theField, jParams, engineMap)).append("\'");
        }
        buff.append("],");
    }

%>