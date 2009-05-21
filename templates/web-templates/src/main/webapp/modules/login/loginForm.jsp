<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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
                    <label for="username"><fmt:message key="username"/></label><ui:loginUsername class="field username" id="username" size="8"/>
                </p>
                <p>
                    <label for="password"><fmt:message key="password"/></label><ui:loginPassword class="field password" id="password" size="8"/>
                </p>
                <p>
                    <ui:loginRedirectChoice class="select loginRedirectChoice"/>
                </p>                
                <p>
                    <label class="rememberLabel" for="rememberme"><fmt:message key="rememberme"/></label><ui:loginRememberMe class="rememberme" id="rememberme"/>
                </p>
                <p><input type="submit" name="submit" id="submit" class="button" value="Login" tabindex="9"/></p>
                <ui:isLoginError>
                  <p><span class="error"><fmt:message key="invalidUsernamePasswordKey"/></span></p>
                </ui:isLoginError>
            </fieldset>
        </ui:loginArea>
    </div>
    <!--stop loginForm -->
</div>
<!--stop box -->
</c:if>
