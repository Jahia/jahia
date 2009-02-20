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

<%@ page language="java" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.resourcebundle.*" %>
<%@ page import="org.jahia.services.version.RevisionEntrySet" %>
<%@ page import="org.jahia.services.version.RevisionEntry" %>
<%@ page import="org.displaytag.tags.*" %>
<%@ page import="org.displaytag.util.*" %>
<%@ page import="java.util.*" %>
<%@taglib uri="http://displaytag.sf.net" prefix="d"%>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%
  //get resource bundle
  JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

  String tableId = "versions_id";
  // Date
  String titleDateHref = "javascript:sortRevisions('"+RevisionEntrySetComparator.SORT_BY_DATE+"','"+pagesVersViewHelper.getInvertSortOrder()+"');";
  String titleDate = JahiaResourceBundle.getEngineResource("org.jahia.engines.version.date",jData.getProcessingContext(),jData.getProcessingContext().getLocale())+"(Preview)";
  //String titleDate = "<a href=\""+titleDateHref+"\">"+titleDateLabel+"</a>";

  //state
  String titleState = "State";

  //Object title
  String titleObjectHref = "javascript:sortRevisions('"+RevisionEntrySetComparator.SORT_BY_TITLE+"','"+pagesVersViewHelper.getInvertSortOrder()+"');";
  String titleObject = JahiaResourceBundle.getEngineResource("org.jahia.engines.version.objectTitle",jData.getProcessingContext(),jData.getProcessingContext().getLocale());
  //String titleObject = "<a href=\""+titleObjectHref+"\">"+titleObjectLabel+"</a>";

  //Object Type
  String titleObjectTypeHref = "javascript:sortRevisions('"+RevisionEntrySetComparator.SORT_BY_OBJECT+"','"+pagesVersViewHelper.getInvertSortOrder()+"');";
  String titleObjectType = JahiaResourceBundle.getEngineResource("org.jahia.engines.version.objectType",jData.getProcessingContext(),jData.getProcessingContext().getLocale());
  //String titleObjectType = "<a href=\""+titleObjectTypeHref+"\">"+titleObjectTypeLabel+"</a>";

  //Description
  String titleDescriptionHref = "javascript:sortRevisions('"+RevisionEntrySetComparator.SORT_BY_DESCR+"','"+pagesVersViewHelper.getInvertSortOrder()+"');";
  String titleDescription = JahiaResourceBundle.getEngineResource("org.jahia.engines.version.objectDescription",jData.getProcessingContext(),jData.getProcessingContext().getLocale());
  //String titleDescription = "<a href=\""+titleDescriptionHref+"\">"+titleDescriptionLabel+"</a>";

  // display tag parameter
  String mainActionURI = actionURL + "&screen=" + theScreen;
  String requestURI = mainActionURI + "&method=showRevisionsList";

  // max number
  List allRevisions = (List) request.getAttribute("revisions");
  if (nbMaxOfRevisions != -1 && nbRevisions > nbMaxOfRevisions) {
    nbRevisions = nbMaxOfRevisions;

    List revisionsToDisplay = new ArrayList();
    for ( int i=0; i<nbRevisions; i++ ){
        revisionsToDisplay.add(allRevisions.get(i));
    }
    request.setAttribute("revisions",revisionsToDisplay);
  }

%>
<%/* hidden value for diplay-tag */%>
<d:table class="evenOddTable" id="<%=tableId%>" htmlId="<%=tableId%>" requestURI="<%=requestURI%>" name="revisions"  export="false" pagesize="5" style="width: 100%;" cellpadding="0" cellspacing="0">
  <d:setProperty name="basic.empty.showtable" value="false" />
  <d:setProperty name="paging.banner.one_item_found" value="" />
  <d:setProperty name="paging.banner.all_items_found" value="" />
  <d:setProperty name="css.tr.even" value="evenLine" />
  <d:setProperty name="css.tr.odd" value="oddLine" />
  <%
  // Current RevisionEntrySet
  RevisionEntrySet revisionEntrySet = (RevisionEntrySet)pageContext.getAttribute(tableId);
  if ( revisionEntrySet != null ){
  // compute date
  final java.util.Date d = new java.util.Date(revisionEntrySet.getVersionID() * 1000L);
  final String dateStr = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.LONG, java.text.DateFormat.MEDIUM, jParams.getLocale()).format(d);
  ContentPage currentPage = pageContent;
  final ContentObject revContentObject = (ContentObject) JahiaObject.getInstance(revisionEntrySet.getObjectKey());
  if (revContentObject instanceof ContentPage) {
    currentPage = (ContentPage) revContentObject;
  }

  // compute revison Type Label
  final String revisionTypeLabel = "org.jahia.engines.version.pagerevisiontype." + revisionEntrySet.getRevisionType();
  %>
  <%/* Date */%>
  <d:column sortable="true" title="<%=titleDate%>">
    <% revisionEntryIndex += 1; %>
    <input type="radio" name="revisionEntrySet" value="<%=revisionEntrySet.toString()%>"<% if ( revisionEntrySet.equals(selRevisionEntrySet) ){ %> checked<% } %>/>
    <a href="<%=currentPage.getURL(jParams,jParams.getLocale().toString())%>/op/compare/entrystate/<%=revisionEntrySet.getVersionID()%>/showrevdiff/s?pageid=<%=currentPage.getID()%>" target="_blank" onClick="selectRevisionEntry(<%=(revisionEntryIndex-1)%>)"><%=dateStr%>
      <img src='${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="infoIcon" />' width="11" height="11" border="0" alt="<%=revisionEntrySet.getVersionID()%>" /></a>
  </d:column>

  <%/* Object state : unused */%>

  <%/* Object title */%>
  <d:column title="<%=titleObject%>" sortable="true" sortName="page">
    <%=(String) revisionEntrySet.getProperty(RevisionEntry.REVISION_TITLE)%>
  </d:column>

  <%/* Type */%>
  <d:column title="<%=titleObjectType%>" sortable="true" sortName="page">
    <%=revisionEntrySet.getObjectKey().getType()%>[<%=revisionEntrySet.getObjectKey().getIDInType()%>]
  </d:column>

  <%/* Description */%>
  <d:column  title="<%=titleDescription%>" sortable="true" headerClass="lastCol" sortName="page">
    <a href="javascript:OpenJahiaScrollableWindow('<%=actionURL%>&engineview=<%=engineView%>&method=revisionsDetail&revisionEntrySet=<%=revisionEntrySet.toString()%>&useRevisionEntry=yes&sortAttribute=<%=pagesVersViewHelper.getSortAttribute()%>&sortOrder=<%=pagesVersViewHelper.getSortOrder()%>','revisions_detail',950, 600)" onClick="selectRevisionEntry(<%=(revisionEntryIndex-1)%>)">
     <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="<%=revisionTypeLabel%>"/></a>
  </d:column>
    <% } else { %>
      <d:column title="<%=titleDate%>" />
      <d:column title="<%=titleObject%>" />
      <d:column title="<%=titleObjectType%>" />
      <d:column title="<%=titleDescription%>" headerClass="lastCol" />
    <% } %>
</d:table>
<script type="text/javascript">
<!--
    addFlagSubmitToDisplayTagLinks('<%=tableId%>','itemsListing');
//-->
</script>
