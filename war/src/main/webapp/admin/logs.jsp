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
  <h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.administrativeAuditLog.label"/></h2>
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
                  <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.ManageLogs.lastLogsSection.label"/>
                </div>
              </div>
              <form style="padding-top: 10px; padding-bottom: 10px" name="modifyLastLogs" action='<%=JahiaAdministration.composeActionURL(request,response,"logs","&sub=view")%>' method="post">
                <table border="0" cellpadding="4" cellspacing="0">
                    <tr>
                        <td><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.audit.ManageLogs.viewLastLogsPrefix.label"/><input type="text" name="viewLastDays" value="<%=viewLastDays%>" size="5" />
                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.audit.ManageLogs.viewLastLogsPostfix.label"/></td>
                        <td>
                            <span class="dex-PushButton">
                                <span class="first-child">
                                    <a class="ico-apply" href="javascript:document.modifyLastLogs.submit()"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.change.label"/></a>
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
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.audit.ManageLogs.flushLogs.label"/>
                  </div>
                </div>
                <ul style="list-style-type: none">
                  <li>
                    <input class="input" type="radio" name="keeprecent" value="false"<%if(!keeprecentlogs.booleanValue()){ %>checked<%} %>> <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.audit.ManageLogs.deleteAllLogEntries.label"/>
                  </li>
                  <li>
                    <input class="input" type="radio" name="keeprecent" value="true"<%if(keeprecentlogs.booleanValue()){ %>checked<%} %>> <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.audit.ManageLogs.keepEntriesMoreRecentThan.label"/><input type="text" size="5" name="maxlogsdays" value="<%=maxlogsdays%>" onFocus="keeprecent[1].checked = true"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.audit.ManageLogs.day.label"/>
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
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"logs","&sub=settings")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.audit.ManageLogs.backToLogSettings.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-flush" href="javascript:document.jahiaAdmin.submit()"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.flush.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/admin/include/footer.inc" %>
