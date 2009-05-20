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
<%@include file="/admin/include/header.inc" %>
<%@page import="org.jahia.bin.*" %>
<%@page import="org.jahia.params.*" %>
<%@page import="java.text.DateFormat" %>
<%@page import   = "java.util.*, org.jahia.services.usermanager.GroupRoleUtils" %>
<%@ taglib uri="http://displaytag.sf.net/el" prefix="display-el" %>
<%
List logData               = (List) request.getAttribute("logData");
Boolean   keeprecentlogs   = (Boolean)   session.getAttribute("keeprecentlogs");
Integer   maxlogsdays      = (Integer)   session.getAttribute("maxlogsdays");
Integer   viewLastDays     = (Integer)   session.getAttribute("viewLastDays");
ProcessingContext jParams = null;
if (jData != null) {
jParams = jData.getProcessingContext();
}
int      areaSize          = 80;
if(userAgent != null) {
if(userAgent.indexOf("MSIE") != -1) {
areaSize = 84;
}
} 
stretcherToOpen   = 0;
%>
<script type="text/javascript">
  function sendFormSave(){
      document.jahiaAdmin.submit();
  }
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="org.jahia.admin.administrativeAuditLog.label"/></h2>
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
              <div class="head">
                <div class="object-title">
                  <fmt:message key="org.jahia.admin.ManageLogs.lastLogsSection.label"/>
                </div>
              </div>
              <form style="padding-top: 10px; padding-bottom: 10px" name="modifyLastLogs" action='<%=JahiaAdministration.composeActionURL(request,response,"logs","&sub=view")%>' method="post">
                <table border="0" cellpadding="4" cellspacing="0">
                    <tr>
                        <td><fmt:message key="org.jahia.admin.audit.ManageLogs.viewLastLogsPrefix.label"/><input type="text" name="viewLastDays" value="<%=viewLastDays%>" size="5" />
                        <fmt:message key="org.jahia.admin.audit.ManageLogs.viewLastLogsPostfix.label"/></td>
                        <td>
                            <span class="dex-PushButton">
                                <span class="first-child">
                                    <a class="ico-apply" href="javascript:document.modifyLastLogs.submit()"><fmt:message key="org.jahia.admin.change.label"/></a>
                                </span>
                            </span>
                        </td>
                    </tr>
                </table>
              </form>
              <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"logs","&sub=flushconfirm")%>' method="post">
                <display-el:table class="evenOddTable tBorder bBorder" name="logData" cellspacing="0" cellpadding="1" style="width:100%; font-size:9px" pagesize="30" export="true" id="rowID">
                  <%@include file="/admin/include/displaytag_admin_properties.inc" %>
                  <display-el:column titleKey="org.jahia.admin.audit.ManageLogs.dateColumnTitle.label" property="timeStr" sortable="true" /><display-el:column titleKey="org.jahia.admin.audit.ManageLogs.userColumnTitle.label" property="username" sortable="true" /><display-el:column titleKey="org.jahia.admin.audit.ManageLogs.siteColumnTitle.label" property="sitekey" sortable="true"/><display-el:column titleKey="org.jahia.admin.audit.ManageLogs.operationColumnTitle.label" property="operation" sortable="true"/><display-el:column titleKey="org.jahia.admin.audit.ManageLogs.objectColumnTitle.label" property="objectname" maxLength="40" sortable="true"/><display-el:column titleKey="org.jahia.admin.audit.ManageLogs.objectIDColumnTitle.label" property="objectid" sortable="true"/>
                  <display-el:column titleKey="org.jahia.admin.audit.ManageLogs.parentColumnTitle.label" sortable="true">
                    <%= ((Map)pageContext.getAttribute("rowID")).get("parenttype") %><%= ((Map)pageContext.getAttribute("rowID")).get("parentname") %> (<%= ((Map)pageContext.getAttribute("rowID")).get("parentid") %>)
                  </display-el:column>
                </display-el:table>
                <div class="head headtop">
                  <div class="object-title">
                    <fmt:message key="org.jahia.admin.audit.ManageLogs.flushLogs.label"/>
                  </div>
                </div>
                <ul style="list-style-type: none">
                  <li>
                    <input class="input" type="radio" name="keeprecent" value="false"<%if(!keeprecentlogs.booleanValue()){ %>checked<%} %>> <fmt:message key="org.jahia.admin.audit.ManageLogs.deleteAllLogEntries.label"/>
                  </li>
                  <li>
                    <input class="input" type="radio" name="keeprecent" value="true"<%if(keeprecentlogs.booleanValue()){ %>checked<%} %>> <fmt:message key="org.jahia.admin.audit.ManageLogs.keepEntriesMoreRecentThan.label"/><input type="text" size="5" name="maxlogsdays" value="<%=maxlogsdays%>" onFocus="keeprecent[1].checked = true"><fmt:message key="org.jahia.admin.audit.ManageLogs.day.label"/>
                  </li>
                </ul>
                
              </form>
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
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"logs","&sub=settings")%>'><fmt:message key="org.jahia.admin.audit.ManageLogs.backToLogSettings.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-flush" href="javascript:document.jahiaAdmin.submit()"><fmt:message key="org.jahia.admin.flush.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/admin/include/footer.inc" %>
