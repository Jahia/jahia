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
                <ui:loginUsername labelCssClassName="left" cssClassName="field" labelKey="username"/>
            </p>

            <p>
                <ui:loginPassword labelCssClassName="left" cssClassName="field" labelKey="password"/>
            </p>

            <p>
                <ui:loginRememberMe labelCssClassName="left" cssClassName="field" labelKey="rememberme"/>
            </p>

            <p>
                <ui:loginButton cssClassName="button" labelKey="loginbutton"/>
            </p>

            <ui:loginErrorMessage invalidUsernamePasswordKey="invalidUsernamePasswordKey" cssClassName="error"/>

        </fieldset>
    </div>
</ui:loginArea>