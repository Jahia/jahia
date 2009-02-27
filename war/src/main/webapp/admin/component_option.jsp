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
<%@page import   = "java.util.*,org.jahia.data.applications.*" %>
<%@page import="org.jahia.bin.*" %>
<%
String theURL = "";
String requestURI = (String)request.getAttribute("requestURI");
String contextRoot = (String)request.getContextPath();
Integer val = (Integer)request.getAttribute("autoDeploy");
boolean autoDeploy = false;
stretcherToOpen   = 0;
if ( val != null ){
autoDeploy = (val.intValue()==1);
} %>
<script type="text/javascript">
  function sendForm(subAction){
      document.mainForm.subaction.value = subAction;
      document.mainForm.action = "<%=requestURI%>?do=sharecomponents&sub=options";
      document.mainForm.submit();
  }
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.manageComponents.label"/></h2>
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
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.generalOptions.label"/>
                  </div>
                </div>
                <div class="content-body">
                  <div id="operationMenu">
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-app-add" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=add")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.manuallyAddNewComponent.label"/></a>
                      </span>
                    </span>
                  </div>
                </div>
                <form name="mainForm" action="" method="post">
                  <table border="0" cellpadding="5" cellspacing="0">
                    <tr>
                      <td>
                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.automaticDeployment.label"/>&nbsp;:
                      </td>
                      <td>
                        <input type="checkbox" name="autoDeploy" value="1"<% if ( autoDeploy ) { %>checked<% } %>>
                      </td>
                    </tr>
                  </table>
                  <div class="buttonList" style="text-align: right; padding-top: 10px">
                    <input type="hidden" name="subaction" value="">
                  </div>
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
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=display")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.backToComponentsList.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-restore" href="javascript:document.mainForm.reset()"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.resetChanges.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="javascript:sendForm('save');"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.save.label"/></a>
            </span>
          </span>
        </div>
      </div>
	  <%@include file="/admin/include/footer.inc" %>