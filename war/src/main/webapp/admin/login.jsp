<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="jahiaAdministrationLogin" value="true" />
<%@include file="/admin/include/header-simple.inc" %>
<%@page import="java.net.URLEncoder" %>
<%
String  redirectTo = (String) request.getAttribute("redirectTo");
if (redirectTo == null) {
redirectTo = "";
}
inputSize                    = 13;
if(userAgent != null) {
if(userAgent.indexOf("MSIE") != -1) {
inputSize = 22;
}
}

pageContext.setAttribute("displayMsg", request.getAttribute(JahiaAdministration.CLASS_NAME+"jahiaDisplayMessage"));
%>
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

<div id="adminLogin">
<h2 class="loginlogo_beta"></h2>
<h3 class="loginIcon"><fmt:message key="org.jahia.admin.jahiaAdministration.label"/></h3>
<br class="clearFloat" />
<form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"processlogin","&redirectTo=" + URLEncoder.encode(redirectTo))%>' method="post">
  <table align="center" class="formTable" cellpadding="0" cellspacing="1" border="0">
    <tr>
      <th>
        <fmt:message key="label.username"/>
      </th>
      <td>
        <input class="input" type="text" name="login_username" style="width: 150px" size="<%=inputSize%>" maxlength="250" value="${fn:escapeXml(jahiaLoginUsername)}" tabindex="1">
      </td>
    </tr>
    <tr>
      <th>
        <fmt:message key="label.password"/>
      </th>
      <td>
        <input class="input" type="password" name="login_password" style="width: 150px" size="<%=inputSize%>" maxlength="250" tabindex="2" onkeydown="if (event.keyCode == 13) javascript:document.jahiaAdmin.submit();">
      </td>
    </tr>
  </table>
  <br/>
</form>
<c:if test="${not empty displayMsg}">
<div class="errorbold">
  ${displayMsg}&nbsp;&nbsp;&nbsp;
</div>
</c:if>
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
<script language="javascript" type="text/javascript">
  setFocus();
</script>
</body>
</html>
<% out.flush(); %>