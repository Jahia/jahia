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
<%@ page import="org.jahia.resourcebundle.*" %>
<%@ page import="org.jahia.utils.JahiaTools"%>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.engines.calendar.CalendarHandler" %>
<%@ page import="org.jahia.services.version.*" %>
<%@ page import="org.jahia.views.engines.JahiaEngineCommonData" %>
<%@ page import="org.jahia.views.engines.JahiaEngineViewHelper" %>
<%@ page import="org.jahia.views.engines.versioning.ContentVersioningViewHelper" %>
<%@ page import="java.util.*" %>

<jsp:useBean id="URL" class="java.lang.String" scope="request"/>

<%@ include file="/views/engines/common/taglibs.jsp" %>
<%
    final ContentVersioningViewHelper versViewHelper =
            (ContentVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
    final JahiaEngineCommonData engineCommonData =
            (JahiaEngineCommonData) request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
    final ProcessingContext jParams = engineCommonData.getParamBean();
    final String actionURL = (String) request.getAttribute("ContentVersioning.ActionURL");
    final String engineView = (String) request.getAttribute("engineView");

    final RevisionEntrySet selRevisionEntrySet = versViewHelper.getRevisionEntrySet();
    final int nbMaxOfRevisions = versViewHelper.getNbMaxOfRevisions().intValue();

    String rowStatus = "even";
    String rowClass;

    // Old engine system
    final Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
    String theScreen = (String) engineMap.get("screen");

    CalendarHandler fromCalHandler = versViewHelper.getFromRevisionDateCalendar();
    CalendarHandler toCalHandler = versViewHelper.getToRevisionDateCalendar();

    StringBuffer strBuffer = new StringBuffer("{\"revisions\":[");
    String[] selectedRevs = request.getParameterValues("revisionEntrySet");
    List selectedRevsList = new ArrayList();
    if ( selectedRevs != null ){
      selectedRevsList = Arrays.asList(selectedRevs);
      for ( int i=0; i<selectedRevs.length; i++ ){
        strBuffer.append("\"");
        strBuffer.append(selectedRevs[i]);
        strBuffer.append("\"");
        if ( i<selectedRevs.length ){
          strBuffer.append(",");
        }
      }
    }
    strBuffer.append("]}");

%>
<%@ include file="common-javascript.inc" %>
<script type="text/javascript">
    <!--
    var jsObj = <%=strBuffer.toString()%>;
    var containerCompareHandler = new ContainerVersionCompare(jsObj.revisions);
    jsObj = null;
    function sortRevisions(sortAttribute,sortOrder){
        document.mainForm.sortAttribute.value = sortAttribute;
        document.mainForm.sortOrder.value = sortOrder;
        sendForm('showRevisionsList','')
    }

    function restoreVersion(revisionEntrySet){
        document.mainForm.revisionEntrySetToUse.value = revisionEntrySet;
        sendForm('showConfirmRestore','')
    }

    function selectRevisionEntry(index){
        if ( document.mainForm.revisionEntrySet[index] != null ){
            document.mainForm.revisionEntrySet[index].checked = true;
        } else {
            // only one revision entry
            document.mainForm.revisionEntrySet.checked = true;
        }
    }

function handleRevisionSelect(theInput){
  containerCompareHandler._handleRevisionSelect(theInput);
}

function handleCompare(){
  if (containerCompareHandler._count < 2 ){
    alert('<%=JahiaTools.html2text(JahiaResourceBundle.getEngineResource("org.jahia.engines.version.atLeastTwoRevisionsMustBeSelected",
            jParams, jParams.getLocale(), "At least two revisions must be selected"))%>');
    return;
  }
  var actionURL = '<%=actionURL%>&engineview=<%=engineView%>&method=containerVersionCompare';
  actionURL+='&version1=' + containerCompareHandler._revisions[0] + '&version2=' + containerCompareHandler._revisions[1];
  OpenJahiaScrollableWindow(actionURL,'compareRevisions',800,680);
}

function handleRevisionDetails(version1){
  var actionURL = '<%=actionURL%>&engineview=<%=engineView%>&method=containerVersionCompare';
  actionURL+='&version1=' + version1;
  OpenJahiaScrollableWindow(actionURL,'compareRevisions',800,680);
}

    //-->
</script>
<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../../../../engines/tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title">
          <fmt:message key="org.jahia.engines.version.dateRange"/>
        </div>
      </div>
      <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
        <tr>
          <th align="left" width="100">
            <fmt:message key="org.jahia.engines.version.from"/>&nbsp;:&nbsp;
          </th>
          <td nowrap="nowrap">
            <%
            request.setAttribute("calendarHandler", fromCalHandler);
            String calURL = URL+"calendar/calendar.jsp";
            calURL = calURL.substring(request.getContextPath().length());
            %>
            <jsp:include page="<%=calURL%>" flush="true"/>
            <script type="text/javascript">
            // override calendar default
            function onCalCloseHandler<%=fromCalHandler.getIdentifier()%>(calendar) {
                sendForm('showRevisionsList','');
            }

            function onCalResetHandler<%=fromCalHandler.getIdentifier()%>() {
                sendForm('showRevisionsList');
            }
            </script>
          </td>
        </tr>
        <tr>
          <th align="left" nowrap>
            <fmt:message key="org.jahia.engines.version.to"/>&nbsp;:&nbsp;
          </th>
          <td nowrap="nowrap">
            <%
            request.setAttribute("calendarHandler", toCalHandler);
            %>
            <jsp:include page="<%=calURL%>" flush="true"/>
            <script type="text/javascript">
            // override calendar default
            function onCalCloseHandler<%=toCalHandler.getIdentifier()%>(calendar) {
                sendForm('showRevisionsList','');
            }

            function onCalResetHandler<%=toCalHandler.getIdentifier()%>() {
                sendForm('showRevisionsList');
            }
            </script>
          </td>
        </tr>
        <tr>
          <th nowrap align="left">
            Nb.&nbsp;Rev.&nbsp;Max&nbsp;:&nbsp;
          </th>
          <td>
            <select class="input" name="nbmax_rev" onChange="sendForm('showRevisionsList','');">
              <option value="-1"<logic:equal name="jahiaEngineViewHelper" property="nbMaxOfRevisionsAsStr" value="-1"> selected</logic:equal>>
                <fmt:message key="org.jahia.engines.version.allRevisions"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              </option>
              <option value="10"<logic:equal name="jahiaEngineViewHelper" property="nbMaxOfRevisionsAsStr" value="10"> selected</logic:equal>>10</option>
              <option value="20"<logic:equal name="jahiaEngineViewHelper" property="nbMaxOfRevisionsAsStr" value="20"> selected</logic:equal>>20</option>
              <option value="50"<logic:equal name="jahiaEngineViewHelper" property="nbMaxOfRevisionsAsStr" value="50"> selected</logic:equal>>50</option>
              <option value="100"<logic:equal name="jahiaEngineViewHelper" property="nbMaxOfRevisionsAsStr" value="100">selected</logic:equal>>100</option>
            </select>
          </td>
        </tr>
        <tr>
          <th nowrap align="left">
            <fmt:message key="org.jahia.engines.version.typeOfRevisions"/>&nbsp;:&nbsp;
          </th>
          <td>
            <select class="input" name="typeOfRevisions" onChange="sendForm('showRevisionsList','');">
              <option value="1"<logic:equal name="jahiaEngineViewHelper" property="contentOrMetadataRevisionsAsString" value="1"> selected</logic:equal>>
                <fmt:message key="org.jahia.engines.version.contentRevisions"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              </option>
              <option value="2"<logic:equal name="jahiaEngineViewHelper" property="contentOrMetadataRevisionsAsString" value="2"> selected</logic:equal>>
                <fmt:message key="org.jahia.engines.version.metadataRevisions"/>
              </option>
            </select>
          </td>
        </tr>
        <tr>
          <th align="left" nowrap colspan="2">
              <span class="dex-PushButton">
                <span class="first-child">
                  <a title="<fmt:message key='org.jahia.apply.button'/>" onclick="sendForm('showRevisionsList',''); return false;" href="#filter" class="ico-refresh"><fmt:message key="org.jahia.apply.button"/></a>
                </span>
              </span>
          </th>
        </tr>
      </table>
      <%
      List allRevisions = (List) request.getAttribute("revisions");
      int nbRevisions = allRevisions.size();
      if (nbMaxOfRevisions != -1 && nbRevisions > nbMaxOfRevisions) {
        nbRevisions = nbMaxOfRevisions;
        List revisions = new ArrayList();
        for ( int i=0; i<nbRevisions; i++ ){
          revisions.add(allRevisions.get(i));
        }
        request.setAttribute("revisions",revisions);
      }
      org.jahia.utils.displaytag.CaseInsensitiveComparator comparator = new org.jahia.utils.displaytag.CaseInsensitiveComparator(request.getLocale());
      String requestURI = actionURL + "&screen=" + theScreen + "&method=showRevisionsList";
      String mainActionURI = actionURL + "&screen=" + theScreen;
      boolean allowRestoreOperation = nbRevisions>1;
      int count=0;
      %>
      <div class="head">
        <div class="object-title">
          <fmt:message key="org.jahia.engines.version.nbOfRevisions"/>&nbsp;:&nbsp;<%=nbRevisions%>&nbsp;/&nbsp;<%=allRevisions.size()%>
        </div>
      </div>
      <%-- 
      <div id="operationMenu">
        <span class="dex-PushButton">
          <span class="first-child">
            <a href="javascript:handleCompare();" class="ico-compare" title="<fmt:message key="org.jahia.engines.version.compare"/>">
              <fmt:message key="org.jahia.engines.version.compare"/></a>
          </span>
        </span>
      </div>
      --%>
      <display:table name="revisions" class="evenOddTable" pagesize="20" requestURI="<%=requestURI%>" id="listItem" htmlId="listItem" style="width: 100%;" cellpadding="0" cellspacing="0" export="false">
        <display:setProperty name="basic.empty.showtable" value="false" />
        <display:setProperty name="paging.banner.one_item_found" value="" />
        <display:setProperty name="paging.banner.all_items_found" value="" />
        <display:setProperty name="css.tr.even" value="evenLine" />
        <display:setProperty name="css.tr.odd" value="oddLine" />
        <display:setProperty name="basic.msg.empty_list">
          <div class="content-body padded"><center><strong><fmt:message key="org.jahia.engines.importexport.contentpick.noresults.label"/></strong></center></div>
        </display:setProperty>

        <%
          count++;
          RevisionEntrySet revEntrySet = (RevisionEntrySet) pageContext.getAttribute("listItem");
        %>
        <display:column sortProperty="versionID" title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.engines.version.version", jParams, jParams.getLocale(),"Version")%>' sortable="true">
          <input id="<%=revEntrySet.toString()%>" class="input" type="checkbox" name="revisionEntrySet" value="<%=revEntrySet.toString()%>" onClick="handleRevisionSelect(this);" <%if ( selectedRevsList.contains(revEntrySet.toString()) ){%>checked<%}%> /> <% if ( false && count>1 ){%><a href="javascript:handleRevisionDetails('<%=revEntrySet.toString()%>')"><%=RevisionEntrySet.getVersionNumber(revEntrySet, jParams, jParams.getLocale())%></a><% } else { %><span class="aStyle"><%=RevisionEntrySet.getVersionNumber(revEntrySet, jParams, jParams.getLocale())%></span><% } %>
        </display:column>
        <display:column title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.engines.version.date", jParams, jParams.getLocale(),"Date")%>' sortable="true" sortProperty="versionID" comparator="<%= comparator %>">
          <%= fromCalHandler.getDateFormatter().format(revEntrySet.getVersionID() * 1000L) %>
        </display:column>
        <display:column property="lastContributor" title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.services.metadata.lastContributor", jParams, jParams.getLocale(),"Last contributor")%>' sortable="true" comparator="<%= comparator %>"/>
        <display:column title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.engines.version.operations", jParams, jParams.getLocale(),"Operations")%>' sortable="false" headerClass="lastCol" class="lastCol">
          <%if ( revEntrySet.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){%>
            <a href="javascript:sendForm('showConfirmContainerUndoStaging');"><fmt:message key="org.jahia.engines.version.undoStaging"/></a>
          <%} else if ( allowRestoreOperation && count > 1){%>
            <a href="javascript:restoreVersion('<%=revEntrySet.toString()%>');"><fmt:message key="org.jahia.engines.version.restore"/></a>
          <%}%>
        </display:column>
      </display:table>

      <script type="text/javascript">
      <!--
          addFlagSubmitToDisplayTagLinks('listItem','itemsListing');
      //-->
      </script>
    </div>
  </div>
</div>
<input type="hidden" name="lastscreen" value="<%=theScreen%>"/>
<input type="hidden" name="revisionEntrySetToUse" value=""/>
<input type="hidden" name="sortAttribute" value="<%=versViewHelper.getSortAttribute()%>"/>
<input type="hidden" name="sortOrder" value="<%=versViewHelper.getSortOrder()%>"/>