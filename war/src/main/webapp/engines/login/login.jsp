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

<%@ page import="org.jahia.params.ParamBean"%>
<%@ page language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>
<jsp:useBean id="engineTitle" class="java.lang.String" scope="request"/>
<jsp:useBean id="javaScriptPath" class="java.lang.String" scope="request"/>
<utility:setBundle basename="JahiaInternalResources"/>
<%
String username = request.getParameter("username");
username = (username == null) ? "" : username;
Boolean cookieAuthActivated = (Boolean) request.getAttribute("cookieAuthActivated");
if (cookieAuthActivated == null) {
    cookieAuthActivated = Boolean.FALSE;
}
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
%>

<!-- Script used by the login jsp -->
<script type="text/javascript">

  function autoFocus(username) {
    if (username && username != "null" && username != "") {
      document.mainForm.password.focus();
    } else {
      document.mainForm.username.focus();
    }
  }

  document.onkeydown = keyDown;

  function keyDown(e) {
    if (!e) e = window.event;
    // e gives access to the event in all browsers
    var ieKey = e.keyCode;
    if (ieKey == 13) {
      sendFormSave();
    }
    if (ieKey == 87 && e.ctrlKey) {
      window.close();
    }
  }

  function cancelButton() {
    if (window.opener == null) {
      window.location.href = "<%=jParams.composePageUrl(jParams.getPageID())%>";
    } else {
      window.close();
    }
  }
</script>
<center>
<div id="adminLogin">
<h3 class="loginIcon"><fmt:message key="org.jahia.engines.loginToJahia.label"/></h3>
<br class="clearFloat" />
<c:if test="${not empty param.badLogin}">
    <p class="error">
        <c:if test="${requestScope['org.jahia.engines.EngineHashMap'].notAllowedToLoginFromThisPage}" var="notAllowedToLoginFromThisPage">
            <fmt:message key="org.jahia.engines.login.Login_Engine.notAllowedToLog.label"/>
        </c:if>
        <c:if test="${!notAllowedToLoginFromThisPage}">
            <fmt:message key="org.jahia.engines.login.Login_Engine.invalidUsernamePassword.label"/>.
            <fmt:message key="org.jahia.engines.login.Login_Engine.pleaseTryAgain.label"/>
        </c:if>
    </p>
</c:if>
<table class="formTable" cellpadding="0" cellspacing="1" border="0">
  <tr>
    <th>
      <fmt:message key="org.jahia.engines.username.label"/>
    </th>
    <td>
      <%
      int columns = 13;
      final String userAgent = request.getHeader("user-agent");
      if (userAgent != null) {
        if (userAgent.indexOf("MSIE") != -1) {
          columns = 20;
        }
      }%>
      <input type="text" name="username" size="<%=columns%>" maxlength="250" tabindex="1" style="width:150px;" value="<%=username%>">
    </td>
  </tr>
  <tr>
    <th>
      <fmt:message key="org.jahia.engines.login.Login_Engine.password.label"/>
    </th>
    <td>
      <input type="password" name="password" size="<%=columns%>" maxlength="250" tabindex="2" style="width:150px;">
    </td>
  </tr>
</table>
<br />
<table align="center" width="100%" cellspacing="5px">
  <tr>
    <td class="alignCenter" colspan="2">
      <select name="loginChoice" tabindex="3">
        <option value="1"><fmt:message key="org.jahia.engines.login.Login_Engine.stayAtCurrentPage.label"/></option>
        <option value="2"><fmt:message key="org.jahia.engines.login.Login_Engine.jumpToHomePage.label"/></option>
      </select>
    </td>
  </tr>
  <% if (cookieAuthActivated.booleanValue()) { %>
    <tr>
      <td class="alignCenter" colspan="2">
        <input type="checkbox" id="useCookie" name="useCookie" value="on" tabindex="4"/>
        <label for="useCookie"><fmt:message key="org.jahia.engines.login.Login_Engine.rememberMe.label"/></label>
      </td>
    </tr>
  <% } %>
</table>
<div id="actionBar" class="alignCenter">
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-ok" href="javascript:sendFormSave();" tabindex="5" title="<fmt:message key="org.jahia.ok.button"/>">
          <fmt:message key="org.jahia.bin.JahiaErrorDisplay.login.label"/></a>
     </span>
  </span>
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-cancel" href="javascript:cancelButton();" tabindex="6" title="<fmt:message key="org.jahia.cancel.button"/>">
        <fmt:message key="org.jahia.cancel.button"/></a>
    </span>
  </span>
</div>
</div>
</center>
<script type="text/javascript">
  try{
    autoFocus('<%=request.getParameter("username")%>');
    function checkParent() {
      // Do nothing to avoid IE closing the window when trying to enter another login.
    }
  }catch(e){}
</script>