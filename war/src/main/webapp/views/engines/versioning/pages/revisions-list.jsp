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

<%@ page import="org.jahia.content.ContentObject" %>
<%@ page import="org.jahia.content.JahiaObject" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.engines.calendar.CalendarHandler" %>
<%@ page import="org.jahia.services.version.RevisionEntry" %>
<%@ page import="org.jahia.services.version.RevisionEntrySet" %>
<%@ page import="org.jahia.services.version.RevisionEntrySetComparator" %>
<%@ page import="org.jahia.views.engines.JahiaEngineCommonData" %>
<%@ page import="org.jahia.views.engines.JahiaEngineViewHelper" %>
<%@ page import="org.jahia.views.engines.versioning.pages.PagesVersioningViewHelper" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Properties" %>

<jsp:useBean id="URL" class="java.lang.String" scope="request"/>

<%@ include file="/views/engines/common/taglibs.jsp" %>
<%
    final PagesVersioningViewHelper pagesVersViewHelper =
            (PagesVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
    final JahiaEngineCommonData engineCommonData =
            (JahiaEngineCommonData) request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
    final ProcessingContext jParams = engineCommonData.getParamBean();
    final ContentPage pageContent = pagesVersViewHelper.getPage();
    final String actionURL = (String) request.getAttribute("ContentVersioning.ActionURL");
    final String engineView = (String) request.getAttribute("engineView");

    final RevisionEntrySet selRevisionEntrySet = pagesVersViewHelper.getRevisionEntrySet();
    final int nbMaxOfRevisions = pagesVersViewHelper.getNbMaxOfRevisions().intValue();

    String rowStatus = "even";
    String rowClass;

    // Old engine system
    final Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
    String theScreen = (String) engineMap.get("screen");

    CalendarHandler fromCalHandler = pagesVersViewHelper.getFromRevisionDateCalendar();
    CalendarHandler toCalHandler = pagesVersViewHelper.getToRevisionDateCalendar();

%>
<!-- versioning/pages/revisions-list.jsp (start) -->
<%@ include file="common-javascript.inc" %>
<script type="text/javascript">
    <!--
    function sortRevisions(sortAttribute,sortOrder){
        document.mainForm.sortAttribute.value = sortAttribute;
        document.mainForm.sortOrder.value = sortOrder;
        sendForm('showRevisionsList','')
    }

    function setUseRevisionEntry(){
        document.mainForm.useRevisionEntry.value = "yes";
        sendForm('showSiteMap','')
    }

    function selectRevisionEntry(index){
        if ( document.mainForm.revisionEntrySet[index] != null ){
            document.mainForm.revisionEntrySet[index].checked = true;
        } else {
            // only one revision entry
            document.mainForm.revisionEntrySet.checked = true;
        }
    }

    //-->
</script>
<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../../../../jsp/jahia/engines/tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title">
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.selectAPreciseDateFromRevisions"/>
        </div>
      </div>
      <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
        <tr>
          <th valign="top" width="120">
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.dateRange"/>&nbsp;:
          </th>
          <td>
            <table cellpadding="0" cellspacing="0" border="0">
              <tr>
                <td align="left">
                  <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.from"/>&nbsp;:&nbsp;
                </td>
                <td nowrap="nowrap">
                  <%
                  request.setAttribute("calendarHandler", fromCalHandler);
                  String calURL = URL+"calendar/calendar.jsp";
                  calURL = calURL.substring(request.getContextPath().length());
                  %>
                  <jsp:include page="<%=calURL%>" flush="true"/>
                  <script type="text/javascript">
                    <!--
                    // override calendar default
                    function onCalCloseHandler<%=fromCalHandler.getIdentifier()%>(calendar) {
                        sendForm('showRevisionsList','');
                    }

                    function onCalResetHandler<%=fromCalHandler.getIdentifier()%>() {
                        sendForm('showRevisionsList');
                    }
                    -->
                  </script>
                </td>
              </tr>
              <tr>
                <td align="left">
                  <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.to"/>&nbsp;:&nbsp;
                </td>
                <td nowrap="nowrap">
                  <%
                      request.setAttribute("calendarHandler", toCalHandler);
                      %>
                  <jsp:include page="<%=calURL%>" flush="true"/>
                  <script type="text/javascript">
                  <!--
                  // override calendar default
                  function onCalCloseHandler<%=toCalHandler.getIdentifier()%>(calendar) {
                      sendForm('showRevisionsList','');
                  }

                  function onCalResetHandler<%=toCalHandler.getIdentifier()%>() {
                      sendForm('showRevisionsList');
                  }
                  -->
                  </script>
                </td>
              </tr>
              <tr>
                <td nowrap="nowrap" align="left">
                  Nb.&nbsp;Rev.&nbsp;Max&nbsp;:&nbsp;
                </td>
                <td>
                  <select class="input" name="nbmax_rev" onChange="sendForm('showRevisionsList','');">
                    <option value="-1"<logic:equal name="jahiaEngineViewHelper" property="nbMaxOfRevisionsAsStr" value="-1"> selected</logic:equal>>
                      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.allRevisions"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    </option>
                    <option value="10"<logic:equal name="jahiaEngineViewHelper" property="nbMaxOfRevisionsAsStr" value="10"> selected</logic:equal>>10</option>
                    <option value="20"<logic:equal name="jahiaEngineViewHelper" property="nbMaxOfRevisionsAsStr" value="20"> selected</logic:equal> >20</option>
                    <option value="50"<logic:equal name="jahiaEngineViewHelper" property="nbMaxOfRevisionsAsStr" value="50"> selected</logic:equal> >50</option>
                    <option value="100"<logic:equal name="jahiaEngineViewHelper" property="nbMaxOfRevisionsAsStr" value="100"> selected</logic:equal>>100</option>
                  </select>
                </td>
              </tr>
              <tr>
                <td nowrap align="left">
                  <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.typeOfRevisions" defaultValue="Type of revisions"/>&nbsp;:&nbsp;
                </td>
                <td>
                  <select class="input" name="typeOfRevisions" onChange="sendForm('showRevisionsList','');">
                    <option value="1"<logic:equal name="jahiaEngineViewHelper" property="contentOrMetadataRevisionsAsString" value="1"> selected</logic:equal>>
                      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.contentRevisions" defaultValue="Content revisions"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    </option>
                    <option value="2"<logic:equal name="jahiaEngineViewHelper" property="contentOrMetadataRevisionsAsString" value="2"> selected</logic:equal>>
                      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.metadataRevisions" defaultValue="Metadata revisions"/>
                    </option>
                  </select>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        <tr>
          <th valign="top">
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.showRevisionsFor"/>&nbsp;:&nbsp;
          </th>
          <td valign="top" nowrap="nowrap" align="left">
            <select class="input" name="level" onChange="sendForm('showRevisionsList','');">
              <c:forEach var="level" begin="1" end="3">
                <option value="${level}"<c:if test="${jahiaEngineViewHelper.pageLevelAsStr == level}"> selected</c:if>>
                  ${level}&nbsp;&nbsp;
                  <c:choose>
                    <c:when test="${level == 1}">
                      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.level"/> (<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.currentPageOnly"/>)
                    </c:when>
                    <c:otherwise>
                      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.levels"/>
                    </c:otherwise>
                  </c:choose>
                </option>
              </c:forEach>
            </select>
          </td>
        </tr>
      </table>

      <%
      final Collection revisions = (Collection) request.getAttribute("revisions");
      if (revisions != null) {
        int nbRevisions = revisions.size();
        if (nbMaxOfRevisions != -1 && nbRevisions > nbMaxOfRevisions) {
            nbRevisions = nbMaxOfRevisions;
        }%>
        <div class="head">
          <div class="object-title">
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.nbOfRevisions"/>&nbsp;&nbsp;(&nbsp;<%=nbRevisions%>&nbsp;/&nbsp;<%=revisions.size()%>&nbsp;)
          </div>
        </div>
        <% if (nbRevisions > 0) { %>
          <%int revisionEntryIndex = 0;%>
          <%@ include file="/views/engines/versioning/pages/revisions-list-displaytag.inc" %>
        <% } else { %>
          <div class="content-body padded"><center><strong><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.contentpick.noresults.label"/></strong></center></div>
        <% } %>
      <% } %>
    </div>
  </div>
</div>

<input type="hidden" name="lastscreen" value="<%=theScreen%>"/>
<input type="hidden" name="useRevisionEntry" value="no"/>
<input type="hidden" name="sortAttribute" value="<%=pagesVersViewHelper.getSortAttribute()%>"/>
<input type="hidden" name="sortOrder" value="<%=pagesVersViewHelper.getSortOrder()%>"/>

<!-- versioning/pages/revisions-list.jsp (end) -->