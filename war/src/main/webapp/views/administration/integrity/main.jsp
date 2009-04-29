<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
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
<%@page import="java.util.*,
                org.jahia.bin.*,
                org.jahia.data.JahiaData" %>
<%@include file="/views/administration/common/taglibs.jsp" %>
<%
    JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");
    String rowStatus = "even";
    String rowClass = "td_lavender";
    int stretcherToOpen   = 1;
%>
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
                <jsp:param name="mode" value="site"/>
            </jsp:include>
            
            <div id="content" class="fit">
              <div class="head">
                <div class="object-title">
                  <fmt:message key="${dialogTitle}"/>
                </div>
              </div>
              <div  class="content-item-noborder">
                <logic:messagesPresent>
                  <html:messages id="msg">
                      <p class="errorbold"><bean:write name="msg"/></p>
                  </html:messages>
                </logic:messagesPresent>
                <logic:messagesPresent message="true">
                  <html:messages id="msg" message="true">
                      <p class="blueColor"><bean:write name="msg"/></p>
                  </html:messages>
                </logic:messagesPresent>
            <form name="mainForm" method="get" action='<%=jData.params().composeStrutsUrl("IntegrityChecks","")%>'>
                <input type="hidden" name="site" value="${param.site}"/>
                <input type="hidden" name="group" value="integrity"/>
                <input type="hidden" name="method" value="save"/>
                <%@include file="settings.jspf" %>
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
               <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","&sub=site")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
             </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
            <a class="ico-ok" href="javascript:document.mainForm.submit()"><fmt:message key="org.jahia.admin.save.label"/></a>
            </span>
          </span>
        </div>
      </div>