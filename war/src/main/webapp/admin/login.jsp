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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="jahiaAdministrationLogin" value="true" />
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
<h3 class="loginIcon"><fmt:message key="org.jahia.admin.jahiaAdministration.label"/></h3>
<br class="clearFloat" />
<form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"processlogin","&redirectTo=" + URLEncoder.encode(redirectTo))%>' method="post">
  <table align="center" class="formTable" cellpadding="0" cellspacing="1" border="0">
    <tr>
      <th>
        <fmt:message key="org.jahia.admin.username.label"/>
      </th>
      <td>
        <input class="input" type="text" name="login_username" style="width: 150px" size="<%=inputSize%>" maxlength="250" value="<%=jahiaLoginUsername%>" tabindex="1">
      </td>
    </tr>
    <tr>
      <th>
        <fmt:message key="org.jahia.admin.password.label"/>
      </th>
      <td>
        <input class="input" type="password" name="login_password" style="width: 150px" size="<%=inputSize%>" maxlength="250" tabindex="2" onkeydown="if (event.keyCode == 13) javascript:document.jahiaAdmin.submit();">
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