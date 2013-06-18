<%@page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal"%>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<utility:setBundle basename="JahiaInternalResources"/>
<html>
<head>
    <meta charset="utf-8">
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/errors.css" type="text/css"/>
    <title><fmt:message key="label.login"/></title>
    <script type="text/javascript">
<<<<<<< .working
        document.onkeydown = function (e) { if ((e || window.event).keyCode == 13) document.loginForm.submit(); };
=======
    var clickcounter=0;	
    document.onkeydown = function (e) { if ((e || window.event).keyCode == 13) clickcounter++; doSubmit(); };
>>>>>>> .merge-right.r46385
    </script>
</head>
<body onload="document.loginForm.username.focus()" class="login">
<<<<<<< .working
    <div class="row-fluid login-wrapper">
        <img class="logo" alt="jahia" src="${pageContext.request.contextPath}/css/images/jahia-logo-white.png">
        <div class="span4 box">
            <div class="content-wrap">
                <ui:loginArea>
                    <h1><fmt:message key="label.login"/></h1>
                    <ui:isLoginError var="loginResult">
                        <div class="alert alert-error"><fmt:message key="${loginResult == 'account_locked' ? 'message.accountLocked' : 'message.invalidUsernamePassword'}"/></div>
                    </ui:isLoginError>
                    <input class="span12" type="text" placeholder="<fmt:message key="label.username"/>" tabindex="1" maxlength="250" name="username"/>
                    <input class="span12" type="password" placeholder="<fmt:message key="label.password"/>" tabindex="2" maxlength="250" name="password"/>
                    <c:if test="${not fn:contains(param.redirect, '/administration')}">
                        <div class="remember">
                            <ui:loginRememberMe id="rememberme" tabindex="3" />
                            <label for="rememberme"><fmt:message key="label.rememberme"/></label>
                        </div>
                    </c:if>
                    <a class="btn btn-block btn-primary" href="#login" onClick="document.forms.loginForm.submit(); return false;" tabindex="5" title="<fmt:message key='label.login'/>">
                        <i class="icon-ok icon-white"></i>
                        &nbsp;<fmt:message key="label.login"/>
                    </a>
                </ui:loginArea>
            </div>
=======
    <div id="adminLogin">
    <h2 class="loginlogo"></h2>
        <ui:loginArea>
           <script type="text/javascript">
        	 	function doSubmit() {
        	 		if(clickcounter == 1) {
        	       		document.forms.loginForm.submit();
        	     	}
              	}  
        	</script>
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
                <label for="rememberme"><fmt:message key="label.rememberme"/></label><ui:loginRememberMe id="rememberme" tabindex="3" />
              </td>
            </tr>
        </table>
        </c:if>
        <div id="actionBar" class="alignCenter">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="#login" onClick="clickcounter++; doSubmit(); return false;" tabindex="5" title="<fmt:message key='label.login'/>"><fmt:message key="label.login"/></a>
             </span>
          </span>
>>>>>>> .merge-right.r46383
        </div>
    </div>
</body>
</html>
