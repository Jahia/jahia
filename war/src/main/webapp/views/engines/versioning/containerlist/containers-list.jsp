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
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>

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
    <%@ include file="../../../../engines/tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title">
          <fmt:message key="org.jahia.engines.include.actionSelector.ContainerListVersioning.label"/>
        </div>
      </div>
      <div class="content-body padded">
        <fmt:message key="org.jahia.engines.version.containerListVersioningExplanation.label"/>
      </div>
      <%
      org.jahia.utils.displaytag.CaseInsensitiveComparator comparator = new org.jahia.utils.displaytag.CaseInsensitiveComparator(request.getLocale());
      String mainActionURI = actionURL + "&screen=" + theScreen;
      String requestURI = mainActionURI + "&method=showContainersList";
      %>
      <div class="headtop">
        <div class="object-title">
          <fmt:message key="org.jahia.engines.search.searchHitType.4"/>
        </div>
      </div>
      <display:table name="containersList" class="evenOddTable" pagesize="20" requestURI="<%=requestURI%>" excludedParams="*" id="listItem" htmlId="listItem" style="width: 100%;" cellpadding="0" cellspacing="0" export="false">
        <display:setProperty name="basic.empty.showtable" value="false" />
        <display:setProperty name="paging.banner.one_item_found" value="" />
        <display:setProperty name="paging.banner.all_items_found" value="" />
        <display:setProperty name="css.tr.even" value="evenLine" />
        <display:setProperty name="css.tr.odd" value="oddLine" />
        <display:setProperty name="basic.msg.empty_list">
          <div class="content-body padded"><center><strong><fmt:message key="org.jahia.engines.importexport.contentpick.noresults.label"/></strong></center></div>
        </display:setProperty>
        <%
          ContainerVersioningBean ctnVB = (ContainerVersioningBean) pageContext.getAttribute("listItem");
        %>
        <display:column sortProperty="id" title='<%=JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.container", jParams.getLocale(),"Container")%>' sortable="true">
<%--
          <a href="javascript:handleRevisionDetails('<%=ctnVB.getId()%>')"><%=JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.container", jParams.getLocale(),"Container") + "&nbsp;" + ctnVB.getId()%></a>
--%>
          <%=JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.container", jParams.getLocale(),"Container") + "&nbsp;" + ctnVB.getId()%>
        </display:column>
        <!--display:column title='<%=JahiaResourceBundle.getJahiaInternalResource( "org.jahia.services.metadata.creator", jParams.getLocale(),"Creator")%>' sortable="true" sortProperty="creator" comparator="<%= comparator %>" headerClass="itemsListingHeader"-->
        <!--%=ctnVB.getCreator()%-->
        <!--/display:column-->
        <!--display:column title='<%=JahiaResourceBundle.getJahiaInternalResource( "org.jahia.services.metadata.creationDate", jParams.getLocale(),"Creation date")%>' sortable="true" sortProperty="creationDate" comparator="<%= comparator %>" headerClass="itemsListingHeader"-->
        <!--%=ContainerVersioningBean.getFormattedDate(ctnVB.getCreationDate(),jParams.getLocale(),"")%-->
        <!--/display:column-->
        <!--display:column title='<%=JahiaResourceBundle.getJahiaInternalResource( "org.jahia.services.metadata.lastContributor", jParams.getLocale(),"Last contributor")%>' sortable="true" sortProperty="creator" comparator="<%= comparator %>" headerClass="itemsListingHeader"-->
        <!--%=ctnVB.getLastContributor()%-->
        <!--/display:column-->
        <display:column title='<%=JahiaResourceBundle.getJahiaInternalResource( "org.jahia.services.metadata.lastModificationDate", jParams.getLocale(),"Creation date")%>' sortable="true" sortProperty="lastContributionDate" comparator="<%= comparator %>">
          <%=ContainerVersioningBean.getFormattedDate(ctnVB.getLastContributionDate(),jParams.getLocale(),"")%>
        </display:column>
        <display:column title='<%=JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.deletedBy", jParams.getLocale(),"Deleted by")%>' sortable="true" sortProperty="deleter" comparator="<%= comparator %>">
          <%=ctnVB.getDeleter()%>
        </display:column>
        <display:column title='<%=JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.deleteDate", jParams.getLocale(),"Delete date")%>' sortable="true" sortProperty="deleteDate" comparator="<%= comparator %>">
          <%=ContainerVersioningBean.getFormattedDate(ctnVB.getDeleteDate(),jParams.getLocale(),"")%>
        </display:column>
        <display:column title='<%=JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.operations", jParams.getLocale(),"Operations")%>' sortable="false" headerClass="lastCol" class="lastCol">
          <%if ( ctnVB.getIsDeleted() || ctnVB.getIsMarkedForDelete() ){ String params = "containerId=" + ctnVB.getId();%>
            <a href="javascript:sendForm('showConfirmUndelete','<%=params%>');"><fmt:message key="org.jahia.engines.version.undelete"/></a>
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