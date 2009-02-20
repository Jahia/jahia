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

<%@include file="include/header.inc" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
stretcherToOpen   = 0;
%>

<script type="text/javascript">
  function doAction(action){
      if (!action) {
          action = 'reset';
      }
      document.jahiaAdmin.sub.value = action;
      document.jahiaAdmin.submit();
      return false;
  }
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.patchmanagement.label"/></h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/jsp/jahia/administration/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
        <div class="dex-TabPanelBottom">
        <div class="tabContent">
            <jsp:include page="/jsp/jahia/administration/include/left_menu.jsp">
                <jsp:param name="mode" value="server"/>
            </jsp:include>
        
        <div id="content" class="fit">
            <div class="head">
                <div class="object-title">
                     <utility:resourceBundle resourceBundle="JahiaInternalResources"
                    resourceName="org.jahia.admin.patchmanagement.mainMenu.label"/>
                </div>
            </div>
            <div  class="content-item">
          <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"patches","")%>' method="post">
            <input type="hidden" name="sub" value="saveinit" /><h2><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.patchmanagement.warning.label"/></h2><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.patchmanagement.enterBuildNumber.label"/>: <input name="id" />
          </form>
          </div>
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
      <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/></a>
    </span>
  </span>
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-ok" href="#save" onclick="return doAction('saveinit')"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.saveChanges.label"/></a>
    </span>
  </span>
</div>
</div><%@include file="/jsp/jahia/administration/include/footer.inc" %>