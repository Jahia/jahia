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
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.services.version.*" %>
<%@ page import="org.jahia.views.engines.versioning.*" %>
<%@ page import="org.jahia.views.engines.versioning.actions.*" %>
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

    // Old engine system
    final Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
    String theScreen = (String) engineMap.get("screen");

%>
<%@ include file="../container/common-javascript.inc" %>
<script type="text/javascript">
<!--
function handleRevisionDetails(containerId){
  var actionURL = '<%=actionURL%>&engineview=<%=engineView%>&method=containerVersionDetail';
  actionURL+='&containerId=' + containerId;
  OpenJahiaScrollableWindow(actionURL,'containerVersionDetail',800,680);
}
//-->
</script>

<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../../../../jsp/jahia/engines/tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title">
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.ContainerListVersioning.label"/>
        </div>
      </div>
      <div class="content-body padded">
        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.containerListVersioningExplanation.label" defaultValue="This interface only let you restore DELETED Containers." />
      </div>
      <%
      org.jahia.utils.displaytag.CaseInsensitiveComparator comparator = new org.jahia.utils.displaytag.CaseInsensitiveComparator(request.getLocale());
      String mainActionURI = actionURL + "&screen=" + theScreen;
      String requestURI = mainActionURI + "&method=showContainersList";
      %>
      <div class="headtop">
        <div class="object-title">
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.search.searchHitType.4"/>
        </div>
      </div>
      <display:table name="containersList" class="evenOddTable" pagesize="20" requestURI="<%=requestURI%>" excludedParams="*" id="listItem" htmlId="listItem" style="width: 100%;" cellpadding="0" cellspacing="0" export="false">
        <display:setProperty name="basic.empty.showtable" value="false" />
        <display:setProperty name="paging.banner.one_item_found" value="" />
        <display:setProperty name="paging.banner.all_items_found" value="" />
        <display:setProperty name="css.tr.even" value="evenLine" />
        <display:setProperty name="css.tr.odd" value="oddLine" />
        <display:setProperty name="basic.msg.empty_list">
          <div class="content-body padded"><center><strong><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.contentpick.noresults.label"/></strong></center></div>
        </display:setProperty>
        <%
          ContainerVersioningBean ctnVB = (ContainerVersioningBean) pageContext.getAttribute("listItem");
        %>
        <display:column sortProperty="id" title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.engines.container", jParams, jParams.getLocale(),"Container")%>' sortable="true">
<%--
          <a href="javascript:handleRevisionDetails('<%=ctnVB.getId()%>')"><%=JahiaResourceBundle.getEngineResource( "org.jahia.engines.container", jParams, jParams.getLocale(),"Container") + "&nbsp;" + ctnVB.getId()%></a>
--%>
          <%=JahiaResourceBundle.getEngineResource( "org.jahia.engines.container", jParams, jParams.getLocale(),"Container") + "&nbsp;" + ctnVB.getId()%>
        </display:column>
        <!--display:column title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.services.metadata.creator", jParams, jParams.getLocale(),"Creator")%>' sortable="true" sortProperty="creator" comparator="<%= comparator %>" headerClass="itemsListingHeader"-->
        <!--%=ctnVB.getCreator()%-->
        <!--/display:column-->
        <!--display:column title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.services.metadata.creationDate", jParams, jParams.getLocale(),"Creation date")%>' sortable="true" sortProperty="creationDate" comparator="<%= comparator %>" headerClass="itemsListingHeader"-->
        <!--%=ContainerVersioningBean.getFormattedDate(ctnVB.getCreationDate(),jParams.getLocale(),"")%-->
        <!--/display:column-->
        <!--display:column title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.services.metadata.lastContributor", jParams, jParams.getLocale(),"Last contributor")%>' sortable="true" sortProperty="creator" comparator="<%= comparator %>" headerClass="itemsListingHeader"-->
        <!--%=ctnVB.getLastContributor()%-->
        <!--/display:column-->
        <display:column title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.services.metadata.lastModificationDate", jParams, jParams.getLocale(),"Creation date")%>' sortable="true" sortProperty="lastContributionDate" comparator="<%= comparator %>">
          <%=ContainerVersioningBean.getFormattedDate(ctnVB.getLastContributionDate(),jParams.getLocale(),"")%>
        </display:column>
        <display:column title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.engines.version.deletedBy", jParams, jParams.getLocale(),"Deleted by")%>' sortable="true" sortProperty="deleter" comparator="<%= comparator %>">
          <%=ctnVB.getDeleter()%>
        </display:column>
        <display:column title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.engines.version.deleteDate", jParams, jParams.getLocale(),"Delete date")%>' sortable="true" sortProperty="deleteDate" comparator="<%= comparator %>">
          <%=ContainerVersioningBean.getFormattedDate(ctnVB.getDeleteDate(),jParams.getLocale(),"")%>
        </display:column>
        <display:column title='<%=JahiaResourceBundle.getEngineResource( "org.jahia.engines.version.operations", jParams, jParams.getLocale(),"Operations")%>' sortable="false" headerClass="lastCol" class="lastCol">
          <%if ( ctnVB.getIsDeleted() || ctnVB.getIsMarkedForDelete() ){ String params = "containerId=" + ctnVB.getId();%>
            <a href="javascript:sendForm('showConfirmUndelete','<%=params%>');"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.undelete" defaultValue="undelete"/></a>
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