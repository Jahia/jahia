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
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.resourcebundle.*" %>
<%@ page import="org.jahia.services.version.RevisionEntrySet" %>
<%@ page import="org.jahia.services.version.RevisionEntry" %>
<%@ page import="org.displaytag.tags.*" %>
<%@ page import="org.displaytag.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@taglib uri="http://displaytag.sf.net" prefix="d"%>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%
  //get resource bundle
  JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

  String tableId = "versions_id";
  // Date
  String titleDateHref = "javascript:sortRevisions('"+RevisionEntrySetComparator.SORT_BY_DATE+"','"+pagesVersViewHelper.getInvertSortOrder()+"');";
  String titleDate = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.version.date",jData.getProcessingContext().getLocale())+"(Preview)";
  //String titleDate = "<a href=\""+titleDateHref+"\">"+titleDateLabel+"</a>";

  //state
  String titleState = "State";

  //Object title
  String titleObjectHref = "javascript:sortRevisions('"+RevisionEntrySetComparator.SORT_BY_TITLE+"','"+pagesVersViewHelper.getInvertSortOrder()+"');";
  String titleObject = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.version.objectTitle",jData.getProcessingContext().getLocale());
  //String titleObject = "<a href=\""+titleObjectHref+"\">"+titleObjectLabel+"</a>";

  //Object Type
  String titleObjectTypeHref = "javascript:sortRevisions('"+RevisionEntrySetComparator.SORT_BY_OBJECT+"','"+pagesVersViewHelper.getInvertSortOrder()+"');";
  String titleObjectType = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.version.objectType",jData.getProcessingContext().getLocale());
  //String titleObjectType = "<a href=\""+titleObjectTypeHref+"\">"+titleObjectTypeLabel+"</a>";

  //Description
  String titleDescriptionHref = "javascript:sortRevisions('"+RevisionEntrySetComparator.SORT_BY_DESCR+"','"+pagesVersViewHelper.getInvertSortOrder()+"');";
  String titleDescription = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.version.objectDescription",jData.getProcessingContext().getLocale());
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
      <img src='${pageContext.request.contextPath}<fmt:message key="infoIcon" />' width="11" height="11" border="0" alt="<%=revisionEntrySet.getVersionID()%>" /></a>
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
     <fmt:message key="<%=revisionTypeLabel%>"/></a>
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
