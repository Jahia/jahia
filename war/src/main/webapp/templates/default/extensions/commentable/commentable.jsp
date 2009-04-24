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

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.apache.commons.id.IdentifierUtils" %>

<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date"/>

<template:getContainer containerID="${containerID}" valueID="container"/>
<template:getContainerField fieldName="isCommentable" containerBean="${container}" valueID="isCommentable"/>
<c:if test="${isCommentable.value}">

    <template:containerList name="comment" id="comment" displayActionMenu="false"
                            actionMenuNameLabelKey="comment.add" actionMenuNamePostFix="comment">
        <div class="comments">
            <h3><fmt:message key="comments"/></h3>
            <template:container id="commentContainer" displayActionMenu="false">
                <dl>
                    <dt><a class="comment-number" href="#">1.</a> <template:field name="commentDate"/> <fmt:message
                            key="by"/> <template:field name="commentAuthor"/>
                    </dt>
                    <dd><h4><template:field name="commentTitle"/></h4>
                        <template:field name="commentBody"/>
                    </dd>
                </dl>
            </template:container>
        </div>
        <!--stop comments-->


        <template:containerForm ignoreAcl="true" var="inputs">

            <input type="hidden" id="c_date" name="${inputs['commentDate'].name}"
                   value="${inputs['commentDate'].defaultValue}" />

            <div id="commentsForm"><!--start commentsForm-->
                <h3><fmt:message key="postYourComment"/></h3>

                <fieldset>
                    <p class="field">
                        <label for="c_name"><fmt:message key="comments.name"/>:</label>
                        <input type="text" size="30" id="c_name" name="${inputs['commentAuthor'].name}"
                               value="${inputs['commentAuthor'].defaultValue}" tabindex="11"/>
                    </p>

                    <p class="field">
                        <label for="c_title"><fmt:message key="comments.title"/>:</label>
                        <input type="text" size="30" id="c_title" name="${inputs['commentTitle'].name}" tabindex="12"/>
                    </p>

                    <p class="field">
                        <label for="c_content"><fmt:message key="comments.message"/>:</label>
                        <textarea rows="7" cols="35" id="c_content" name="${inputs['commentBody'].name}"
                                  tabindex="13"></textarea>
                    </p>


                        <%--<template:captcha/>--%>
                        <%--captcha = <input name="captcha" />--%>
                </fieldset>

                <p class="form-help"><fmt:message key="comments.help"/></p>
                <fieldset>

                    <p class="c_button">
                        <input type="submit" value="<fmt:message key="comments.submit"/>" class="button" tabindex="11"/>
                    </p>

                </fieldset>

            </div>
            <!--stop commentsForm-->

        </template:containerForm>

    </template:containerList>
    <%--<template:gwtJahiaModule isTemplate="true" jahiaType="form" id='<%= "form" + IdentifierUtils.nextStringNumericIdentifier() %>' nodeType="jnt:comment" captcha="${pageContext.request.contextPath}/jcaptcha"--%>
    <%--action="createNode" target="${comment.JCRPath}" cssClassName="comment" />--%>
    </div>
</c:if>
