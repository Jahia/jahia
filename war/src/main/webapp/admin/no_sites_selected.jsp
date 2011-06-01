<%@ page import="org.jahia.services.usermanager.JahiaUserManagerProvider" %>
<%@ include file="/admin/include/header.inc" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.List" %>
<%
stretcherToOpen   = 0; %>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/></h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/admin/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
        <div class="dex-TabPanelBottom">
        <div class="tabContent">
            <jsp:include page="/admin/include/left_menu.jsp">
                <jsp:param name="mode" value="server"/>
            </jsp:include>
          <div id="content" class="fit">
            <div class="error">
              <fmt:message key="org.jahia.admin.site.ManageSites.noSiteSpecified.label"/>
            </div>
          </div>
        </div>
        </td>
      </tr>
    </tbody>
  </table>
</div>
<div id="actionBar">
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>'><fmt:message key="org.jahia.admin.site.ManageSites.backToSitesList.label"/></a>
    </span>
  </span>
</div>
</div><%@include file="/admin/include/footer.inc" %>