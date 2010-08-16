<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery.validate.js"/>
<c:if test="${not renderContext.liveMode || not renderContext.loggedIn}">
    <script type="text/javascript">
        $(document).ready(function() {
            $("#newUser").validate({
                rules: {
                    desired_firstname : "required",
                    desired_lastname: "required",
                    username: {
                        required: true,
                        minlength: 2
                    },
                    password: {
                        required: true,
                        minlength: 6
                    },
                    confirm_password: {
                        required: true,
                        minlength: 6,
                        equalTo: "#desired_password"
                    },
                    desired_email: {
                        required: true,
                        email: true
                    }
                },
                messages: {
                    desired_firstname: "<fmt:message key='userregistration.label.askFirstname'/>",
                    desired_lastname: "<fmt:message key='userregistration.label.askLastname'/>",
                    username: {
                        required: "<fmt:message key='userregistration.label.askUsername'/>",
                        minlength: "<fmt:message key='userregistration.label.usernameSizeWarning'/>"
                    },
                    password: {
                        required: "<fmt:message key='userregistration.label.askPassword'/>",
                        minlength: "<fmt:message key='userregistration.label.passwordSizeWarning'/>"
                    },
                    confirm_password: {
                        required: "<fmt:message key='userregistration.label.askPassword'/>",
                        minlength: "<fmt:message key='userregistration.label.passwordSizeWarning'/>",
                        equalTo: "<fmt:message key='userregistration.label.validatePassword'/>"
                    },
                    desired_email: "<fmt:message key='userregistration.label.validateEmail'/>"
                }
            });
        });
    </script>
    <div class="Form">
    <form method="post" action="<c:url value='${currentNode.path}.newUser.do' context='${url.base}'/>" name="newUser" id="newUser">
        <input type="hidden" name="userredirectpage" value="${currentNode.properties['userRedirectPage'].node.path}"/>
        <c:if test="${not empty currentNode.properties['from']}">
            <input type="hidden" name="from" value="${currentNode.properties['from'].string}"/>
        </c:if>
        <c:if test="${not empty currentNode.properties['to']}">
            <input type="hidden" name="to" value="${currentNode.properties['to'].string}"/>
        </c:if>
        <c:if test="${not empty currentNode.properties['cc']}">
            <input type="hidden" name="cc" value="${currentNode.properties['cc'].string}"/>
        </c:if>
        <c:if test="${not empty currentNode.properties['bcc']}">
            <input type="hidden" name="bcc" value="${currentNode.properties['bcc'].string}"/>
        </c:if>
        <input type="hidden" name="toAdministrator" value="${currentNode.properties['toAdministrator'].string}"/>

        <h3 class="boxdocspacetitleh3"><fmt:message key="userregistration.label.form.name"/></h3>
        <fieldset>
            <legend><fmt:message key="userregistration.label.form.name"/></legend>

            <p><label class="left" for="desired_login"><fmt:message key="userregistration.label.form.login"/></label>
                <input type="text" name="username" id="desired_login" value="" tabindex="20"/></p>


            <p><label class="left" for="desired_password"><fmt:message
                    key="userregistration.label.form.password"/></label><input type="password" name="password"
                                                                               id="desired_password"/></p>

            <p><label class="left" for="confirm_password"><fmt:message
                    key="userregistration.label.form.confirmPassword"/></label><input type="password" name="confirm_password"
                                                                               id="confirm_password"/></p>

            <p><label class="left" for="desired_email"><fmt:message
                    key="userregistration.label.form.email"/></label><input type="text" name="desired_email"
                                                                            id="desired_email"/></p>

            <p><label class="left" for="desired_firstname"><fmt:message
                    key="userregistration.label.form.firstname"/></label><input type="text" name="desired_firstname"
                                                                                id="desired_firstname"/></p>

            <p><label class="left" for="desired_lastname"><fmt:message
                    key="userregistration.label.form.lastname"/></label><input type="text" name="desired_lastname"
                                                                               id="desired_lastname"/></p>

            <div class="divButton">
                <input type="submit" class="button"
                       value="<fmt:message key='userregistration.label.form.create'/>"/>
            </div>
        </fieldset>
    </form>
    </div>
</c:if>
