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

<%@page language="java" contentType="text/html; charset=UTF-8" 
%><%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal"%>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/andromeda.css" type="text/css"/>
    <title><fmt:message key="org.jahia.bin.JahiaErrorDisplay.httpUnauthorized.label"/></title>
<script type="text/javascript">
function autoFocus() {
    document.loginForm.username.focus();
}

document.onkeydown = keyDown;

function keyDown(e) {
    if (!e) e = window.event;
    var ieKey = e.keyCode;
    if (ieKey == 13) {
    	document.loginForm.submit();
    }
}
</script>

</head>
<body onload="autoFocus()" class="login">
<center>
    <div id="adminLogin">
        <ui:loginArea>
        <h3 class="loginIcon"><fmt:message key="org.jahia.engines.loginToJahia.label"/></h3>
        <br class="clearFloat"/>
        <ui:loginErrorMessage bundleKey="JahiaInternalResources" invalidUsernamePasswordKey="org.jahia.engines.login.Login_Engine.invalidUsernamePassword.label" cssClassName="error"/>
        <table cellspacing="1" cellpadding="0" border="0" class="formTable">
            <tbody>
            <tr>
                <th><fmt:message key="org.jahia.engines.username.label"/></th>
                <td><input type="text" value="" style="width: 150px;" tabindex="1" maxlength="250" size="13" name="username"/></td>
            </tr>
            <tr>
                <th><fmt:message key="org.jahia.engines.login.Login_Engine.password.label"/></th>
                <td><input type="password" style="width: 150px;" tabindex="2" maxlength="250" size="13" name="password"/></td>
            </tr>
            </tbody>
        </table>
        <br/>
        <table align="center" width="100%" cellspacing="5">
          <tr>
            <td class="alignCenter" colspan="2">
              <select name="loginChoice" tabindex="3">
                <option value="1"><fmt:message key="org.jahia.engines.login.Login_Engine.stayAtCurrentPage.label"/></option>
                <option value="2"><fmt:message key="org.jahia.engines.login.Login_Engine.jumpToHomePage.label"/></option>
              </select>
            </td>
          </tr>
          <tr>
              <td class="alignCenter" colspan="2">
                <ui:loginRememberMe bundleKey="JahiaInternalResources" labelKey="org.jahia.engines.login.Login_Engine.rememberMe.label"/>
              </td>
            </tr>
        </table>
        <div id="actionBar" class="alignCenter">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="#login" onclick="document.forms.loginForm.submit(); return false;" tabindex="5" title="<fmt:message key="org.jahia.bin.JahiaErrorDisplay.login.label"/>">
                  <fmt:message key="org.jahia.bin.JahiaErrorDisplay.login.label"/></a>
             </span>
          </span>
        </div>
        
        </ui:loginArea>
    </div>
</center>
</body>
</html>
