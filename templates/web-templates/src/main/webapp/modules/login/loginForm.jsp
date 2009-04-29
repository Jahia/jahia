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
<%@ include file="../../common/declarations.jspf" %>
<c:if test="${!requestScope.currentRequest.logged}">
<div class="loginForm"><!--start box -->
    <h3><fmt:message key='login'/></h3>

    <div class="loginForm"><!--start loginForm -->
        <ui:loginArea>
            <fieldset>
                <legend><fmt:message key='login'/></legend>
                <p>
                    <ui:loginUsername labelCssClassName="" cssClassName="field username" labelKey="username" tabIndex="6"/>
                </p>
                <p>
                    <ui:loginPassword labelCssClassName="" cssClassName="field password" labelKey="password" tabIndex="7"/>
                </p>
                <p>
                    <ui:loginRememberMe labelCssClassName="rememberLabel" cssClassName="rememberme" labelKey="rememberme" tabIndex="8"/>
                </p>
                <p><input type="submit" name="submit" id="submit" class="button" value="Login" tabindex="9"/></p>
                <p><ui:loginErrorMessage invalidUsernamePasswordKey="invalidUsernamePasswordKey" cssClassName="error"/></p>
            </fieldset>
        </ui:loginArea>
    </div>
    <!--stop loginForm -->
</div>
<!--stop box -->
</c:if>
