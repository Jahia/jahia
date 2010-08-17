<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../../common/declarations.jspf" %>
<!-- login -->
<ui:loginArea>
    <div class="loginform">
        <fieldset>
            <legend>&nbsp;<fmt:message key='login'/>&nbsp;</legend>
            <p>
                <label class="left" for="username"><fmt:message key="username"/></label><ui:loginUsername class="field" id="username" size="8"/>
            </p>

            <p>
                <label class="left" for="password"><fmt:message key="password"/></label><ui:loginPassword class="field" id="password" size="8"/>
            </p>

            <p>
                <label class="left" for="rememberme"><fmt:message key="rememberme"/></label><ui:loginRememberMe class="field" id="rememberme"/>
            </p>

            <p>
                <input type="submit" name="login" value="<fmt:message key='loginbutton'/>" class="button"/>
            </p>

            <ui:isLoginError>
              <p><span class="error"><fmt:message key="invalidUsernamePasswordKey"/></span></p>
            </ui:isLoginError>

        </fieldset>
    </div>
</ui:loginArea>