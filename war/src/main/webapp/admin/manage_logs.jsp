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
<%@page import="java.util.*" %>

<%
    Boolean logEnabled = (Boolean) request.getAttribute("logEnabled");
    String maxLogs = (String) request.getAttribute("maxLogs");
    stretcherToOpen = 0;
%>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.administrativeAuditLog.label"/></h2>
</div>
<div id="main">
    <table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
           cellspacing="0">
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
                                        <fmt:message key="org.jahia.admin.administrativeAuditLog.label"/>
                                    </div>
                                </div>
                                <div class="content-item">
                                    <form name="jahiaAdmin"
                                          action='<%=JahiaAdministration.composeActionURL(request,response,"logs","&sub=manage")%>'
                                          method="post">
                                        <div id="operationMenu" style="border-bottom:0">
                                            <% if (logEnabled.booleanValue()) { %>
                                            <input type="hidden" size="6" name="maxlogs" value="<%=maxLogs%>">
                <span class="dex-PushButton"> 
                    <span class="first-child">
                    <a class="ico-log-disable"
                       href='<%=JahiaAdministration.composeActionURL(request,response,"logs","&sub=disable")%>'><fmt:message key="org.jahia.admin.audit.ManageLogs.disableLogs.label"/></a>
                    </span> 
                </span>
                <span class="dex-PushButton"> 
                    <span class="first-child">
                    <a class="ico-log-view"
                       href='<%=JahiaAdministration.composeActionURL(request,response,"logs","&sub=view")%>'><fmt:message key="org.jahia.admin.audit.ManageLogs.viewLogs.label"/></a>
                    </span> 
                </span>
                <span class="dex-PushButton"> 
                    <span class="first-child">                    
                     <a class="ico-log-flush" href="javascript:document.jahiaAdmin.submit();">
                         <fmt:message key="org.jahia.admin.audit.ManageLogs.flushLogs.label"/>
                     </a>
                    </span> 
                </span>
                                            <%} else {%>
                <span class="dex-PushButton"> 
                    <span class="first-child">
                    <a class="ico-log-enable"
                       href='<%=JahiaAdministration.composeActionURL(request,response,"logs","&sub=enable")%>'><fmt:message key="org.jahia.admin.audit.ManageLogs.enableLogs.label"/></a>
                    </span> 
                </span>
                                            <% } %>
                                        </div>
                                    </form>
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
      	 <a class="ico-back"
              href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
      </span>
     </span>
</div>

</div>

<%@include file="/admin/include/footer.inc" %>