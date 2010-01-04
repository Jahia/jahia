<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!-- Login Form -->

<script type="text/javascript">
document.onkeydown = keyDown;

function keyDown(e) {
    if (!e) e = window.event;
    var ieKey = e.keyCode;
    if (ieKey == 13) {
    	document.loginForm.submit();
    }
}
</script>
<c:if test="${renderContext.editMode}">
    <div class="box">
        <div class="boxshadow boxpadding40 boxmarginbottom16">
            Login form : ${currentNode.properties['jcr:title'].string}
        </div>
    </div>

</c:if>
<ui:loginArea action="${pageContext.request.contextPath}/cms/login">
    <h3 class="loginIcon">${currentNode.properties['jcr:title'].string}</h3>
    <br class="clearFloat"/>
    <ui:isLoginError>
        <span class="error"><fmt:message bundle="JahiaInternalResources"
                key="org.jahia.engines.login.Login_Engine.invalidUsernamePassword.label"/></span>
    </ui:isLoginError>
    <table cellspacing="1" cellpadding="0" border="0" class="formTable">
        <tbody>
        <tr>
            <th>Username</th>
            <td><input type="text" value="" style="width: 150px;" tabindex="1" maxlength="250" size="13"
                       name="username"/></td>
        </tr>
        <tr>
            <th>Password</th>
            <td><input type="password" style="width: 150px;" tabindex="2" maxlength="250" size="13" name="password"/>
            </td>
        </tr>
        </tbody>
    </table>
    <br/>
    <c:if test="${currentNode.properties['j:displayLoginButton'].boolean}">
    <table align="center" width="100%" cellspacing="5">
        <tr>
            <td class="alignCenter" colspan="2">
                <label for="rememberme"><fmt:message
                        key="org.jahia.engines.login.Login_Engine.rememberMe.label"/></label><ui:loginRememberMe
                    id="rememberme"/>
            </td>
        </tr>
    </table>
    </c:if>
    <c:if test="${currentNode.properties['j:displayRememberMeButton'].boolean}">
    <div id="actionBar" class="alignCenter">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="#login" onclick="document.forms.loginForm.submit(); return false;" tabindex="5"
                 title="<fmt:message key="org.jahia.bin.JahiaErrorDisplay.login.label"/>">
                  <fmt:message key="org.jahia.bin.JahiaErrorDisplay.login.label"/></a>
             </span>
          </span>
    </div>
    </c:if>
</ui:loginArea>