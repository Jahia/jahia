<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

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
