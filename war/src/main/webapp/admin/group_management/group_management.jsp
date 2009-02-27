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

<%@page language = "java" %>
<%@page import = "java.util.*" %>
<%@page import="org.jahia.bin.*" %>
<%@page import = "org.jahia.data.JahiaData" %>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/><% // http files path. %>
<jsp:useBean id="groupMessage" class="java.lang.String" scope="session"/><%
String groupSearch = (String)request.getAttribute("groupSearch");
String currentSite = (String)request.getAttribute("currentSite");
JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");
int stretcherToOpen = 1; %>
<!-- For future version : <script language="javascript" src="../search_options.js"></script> -->
<script type="text/javascript" src="<%=URL%>../javascript/selectbox.js">
</script>
<script type="text/javascript">
  
  function submitForm(action){
      document.mainForm.action = '<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=")%>' + action;
      document.mainForm.method = "post";
      document.mainForm.submit();
  }
  
  function handleKey(e){
      if (e.altKey && e.ctrlKey) {
          submitForm('remove');
      }
      else 
          if (e.altKey) {
              submitForm('membership');
          }
          else 
              if (e.ctrlKey) {
                  submitForm('copy');
              }
              else {
                  submitForm('edit');
              }
  }
  
  function handleKeyCode(code){
      if (code == 46) {
          submitForm('remove');
      }
      else 
          if (code == 45) {
              submitForm('create');
          }
          else 
              if (code == 13) {
                  submitForm('edit');
              }
  }
  
  function setFocus(){
      document.mainForm.selectedGroup.focus();
  }
  
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageGroups.groupManagement.label"/> : <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.label"/>&nbsp;<%= currentSite %></h2>
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
                    <jsp:param name="mode" value="site"/>
                </jsp:include>
              <div id="content" class="fit">
                <div class="head">
                  <div class="object-title">
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.manageGroups.label"/>
                  </div>
                </div>
                <div class="content-body">
                  <!-- User operations -->
                  <div id="operationMenu">
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-group-add" href="javascript:submitForm('create');"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageGroups.createNewGroup.label"/></a>
                      </span>
                    </span>
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-group-copy" href="javascript:submitForm('copy');"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageGroups.copySelectedGroup.label"/></a>
                      </span>
                    </span>
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-group-edit" href="javascript:submitForm('edit');"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageGroups.editSelectedGroup.label"/></a>
                      </span>
                    </span>
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-group-remove" href="javascript:submitForm('remove');"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageGroups.removeSelectedGroup.label"/></a>
                      </span>
                    </span>
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-group-view" href="javascript:submitForm('membership');"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageGroups.viewSelectedGroup.label"/></a>
                      </span>
                    </span>
                  </div><!-- For future version : <li><a href="group_share.html">Share selected group</a></li> -->
                </div>
                  <div class="head">
                    <div class="object-title">
                      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageGroups.groupList.label"/>
                    </div>
                  </div>
                  <div  class="content-item-noborder">
                  <%
                if ( groupMessage.length()>0 ){ %>
                <p class="${not isError ? 'blueColor' : 'errorbold'}">
                  <%=groupMessage %>
                </p>
                <% } %>
                <form name="mainForm" action="" method="post">
                <!-- Group management -->
                <table border="0" style="width:100%">
                  <tr>
                    <td>
                      <jsp:include page="<%= groupSearch%>" flush="true"/>
                    </td>
                  </tr>
                </table><!-- -->
                </form>
                </div>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
          </div>
          <div id="actionBar">
            <span class="dex-PushButton">
              <span class="first-child">
                <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/></a>
              </span>
            </span>
          </div>
        </div>
        <script language="javascript">
          setFocus();
        </script>
