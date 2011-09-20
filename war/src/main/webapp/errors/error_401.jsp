<%@page language="java" contentType="text/html; charset=UTF-8"
%><?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal"%>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<utility:setBundle basename="JahiaInternalResources"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-1.1.css" type="text/css"/>
    <title><fmt:message key="label.login"/></title>
    <script type="text/javascript">
    document.onkeydown = function (e) { if ((e || window.event).keyCode == 13) document.loginForm.submit(); };
    </script>
</head>
<body onload="document.loginForm.username.focus()" class="login">
    <div id="adminLogin">
    <h2 class="loginlogo"></h2>
        <ui:loginArea>
            <h3 class="loginIcon"><fmt:message key="label.login"/></h3>
            <br class="clearFloat" />
        <ui:isLoginError var="loginResult">
          <span class="error"><fmt:message key="${loginResult == 'account_locked' ? 'message.accountLocked' : 'message.invalidUsernamePassword'}"/></span>
        </ui:isLoginError>
        <table cellspacing="1" cellpadding="0" border="0" class="formTable">
            <tbody>
            <tr>
                <th><fmt:message key="label.username"/></th>
                <td><input type="text" value="" style="width: 150px;" tabindex="1" maxlength="250" size="13" name="username"/></td>
            </tr>
            <tr>
                <th><fmt:message key="label.password"/></th>
                <td><input type="password" style="width: 150px;" tabindex="2" maxlength="250" size="13" name="password"/></td>
            </tr>
            </tbody>
        </table>
        <c:if test="${not fn:contains(param.redirect, '/administration')}">
        <br/>
        <table align="center" width="100%" cellspacing="5">
          <tr>
              <td class="alignCenter" colspan="2">
                <label for="rememberme"><fmt:message key="label.rememberme"/></label><ui:loginRememberMe id="rememberme"/>
              </td>
            </tr>
        </table>
        </c:if>
        <div id="actionBar" class="alignCenter">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="#login" onClick="document.forms.loginForm.submit(); return false;" tabindex="5" title="<fmt:message key='label.login'/>"><fmt:message key="label.login"/></a>
             </span>
          </span>
        </div>

        </ui:loginArea>
    </div>
</body>
</html>
