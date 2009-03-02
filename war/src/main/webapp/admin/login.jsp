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
<%@page import="java.net.URLEncoder" %>
<%
String  jahiaLoginUsername   = (String) request.getAttribute("jahiaLoginUsername");
String  redirectTo = (String) request.getAttribute("redirectTo");
if (redirectTo == null) {
redirectTo = "";
}
inputSize                    = 13;
if(userAgent != null) {
if(userAgent.indexOf("MSIE") != -1) {
inputSize = 22;
}
} %>
<script language="javascript" type="text/javascript">
  function setFocus(){
      document.jahiaAdmin.login_username.focus();
  }
  
  document.onkeydown = keyDown;
    
  function keyDown(e) {
	  if (!e) e = window.event;
      var ieKey = e.keyCode;
      if (ieKey == 13) {
    	  document.jahiaAdmin.submit();
      }
  }
</script>
<center>
<div id="adminLogin">
<h1 class="big">Jahia</h1>
<h3 class="loginIcon"><fmt:message key="org.jahia.admin.jahiaAdministration.label"/></h3>
<br class="clearFloat" />
<form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"processlogin","&redirectTo=" + URLEncoder.encode(redirectTo))%>' method="post">
  <table align="center" class="formTable" cellpadding="0" cellspacing="1" border="0">
    <tr>
      <th>
        <fmt:message key="org.jahia.admin.username.label"/>
      </th>
      <td>
        <input class="input" type="text" name="login_username" size="<%=inputSize%>" maxlength="250" value="<%=jahiaLoginUsername%>" tabindex="1">
      </td>
    </tr>
    <tr>
      <th>
        <fmt:message key="org.jahia.admin.password.label"/>
      </th>
      <td>
        <input class="input" type="password" name="login_password" size="<%=inputSize%>" maxlength="250" tabindex="2" onkeydown="if (event.keyCode == 13) javascript:document.jahiaAdmin.submit();">
      </td>
    </tr>
  </table>
  <br/>
</form>
<%
String message = (String) request.getAttribute(JahiaAdministration.CLASS_NAME+"jahiaDisplayMessage");
if(message!=null) { %>
<div class="errorbold">
  <%=message %>&nbsp;&nbsp;&nbsp;
</div>
<%
} %>
<div class="text2">
  <%=jahiaDisplayMessage %>&nbsp;&nbsp;&nbsp;
</div>
<div id="actionBar" class="alignCenter">
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-ok" href="javascript:document.jahiaAdmin.submit();" tabindex="5" title="<fmt:message key="org.jahia.bin.JahiaAdministration.login.label"/>"><fmt:message key="org.jahia.bin.JahiaAdministration.login.label"/></a>
    </span>
  </span>
  
</div>
</div>
</center>
<script language="javascript" type="text/javascript">
  setFocus();
</script>
<%@include file="/admin/include/footer.inc" %>