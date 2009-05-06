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