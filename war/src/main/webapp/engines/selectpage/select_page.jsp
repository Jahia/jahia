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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" %>

<%@ page import="org.jahia.engines.selectpage.SelectPage_Engine" %>
<%@ page import="org.jahia.params.*" %>
<%@ page import="java.util.*"%>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.services.version.EntryLoadRequest"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>

<internal:gwtInit />
<internal:gwtImport module="org.jahia.ajax.gwt.module.pagepicker.PagePicker" />
<internal:gwtGenerateDictionary/>
<%

  final String selectedPageOperation = request.getParameter(SelectPage_Engine.OPERATION);

  final String pageIDStr = request.getParameter(SelectPage_Engine.PAGE_ID);
  final int pageID;
  if (pageIDStr == null || pageIDStr.length() == 0) {
    pageID = -1;
  } else {
    pageID = Integer.parseInt(request.getParameter(SelectPage_Engine.PAGE_ID));
  }

    String pagePath = "";
    int current = pageID;
    while (current>0) {
        ContentPage p = ContentPage.getPage(current);
        pagePath = current + "/" +pagePath;
        current = p.getParentID(EntryLoadRequest.STAGED);
    }
    pagePath = "/"+pagePath;

  final String parentPageIDStr = request.getParameter(SelectPage_Engine.PAGE_ID);
  final int parentPageID;
  if (parentPageIDStr == null || parentPageIDStr.length() == 0) {
    parentPageID = -1;
  } else {
    parentPageID = Integer.parseInt(request.getParameter(SelectPage_Engine.PARENT_PAGE_ID));
  }

    String parentPath = "";
    current = parentPageID;
    while (current>0) {
        ContentPage p = ContentPage.getPage(current);
        parentPath = current + "/" +parentPath;
        current = p.getParentID(EntryLoadRequest.STAGED);
    }
    parentPath = "/"+parentPath;

%>

<div id="mainContent">
  <div id="gwtpagepicker" pagePath="<%=pagePath%>" parentPath="<%=parentPath%>" selectPageOperation="<%=selectedPageOperation%>" siteID="${param['siteID']}" homePageID="${param['homepageID']}" callback="${param['callback']}">
  </div>
</div>
