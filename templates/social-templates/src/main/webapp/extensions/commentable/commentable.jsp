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
<%@ page import="org.apache.commons.id.IdentifierUtils" %>

<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date"/>

<template:getContainer containerID="${containerID}" valueID="container"/>
<template:getContainerField fieldName="isCommentable" containerBean="${container}" valueID="isCommentable"/>
<c:if test="${isCommentable.value}">

    <template:containerList name="comment" id="comment"
                            actionMenuNameLabelKey="comment.add" actionMenuNamePostFix="comment" sortByMetaData="created" sortOrder="desc">
        <div class="comments">
            <h3><fmt:message key="comments"/></h3>
            <c:set var="answerCounter" scope="page" value="1"/>
            <dl>
            <template:container id="commentContainer">

                    <dt><a class="comment-number" href="#">${answerCounter}.</a> <template:field name="commentDate" inlineEditingActivated="false"/> <fmt:message
                            key="by"/> <template:field name="commentAuthor" inlineEditingActivated="false"/>
                    </dt>
                    <dd><h4><template:field name="commentTitle"/></h4>
                        <template:field name="commentBody"/>
                    </dd>
                    <c:set var="answerCounter" scope="page" value="${answerCounter + 1}"/>
            </template:container>
            </dl>
        </div>
        <!--stop comments-->
        <a name="comment"/>
        <template:containerForm ignoreAcl="true" var="inputs" action="${currentPage.url}?article=${containerID}">

            <input type="hidden" id="c_date" name="${inputs['commentDate'].name}"
                   value="${inputs['commentDate'].defaultValue}" />
            <div id="comment-form">
            <div id="commentsForm"><!--start commentsForm-->
                <h3><fmt:message key="postYourComment"/>:</h3>

                <fieldset>
                    <p class="field">
                        <label for="c_name"><fmt:message key="comments.name"/> :</label>
                        <input type="text" size="30" id="c_name" name="${inputs['commentAuthor'].name}"
                               value="${inputs['commentAuthor'].defaultValue}" tabindex="11"/>
                    </p>

                    <p class="field">
                        <label for="c_title"><fmt:message key="comments.title"/> :</label>
                        <input type="text" size="30" id="c_title" name="${inputs['commentTitle'].name}" tabindex="12"/>
                    </p>

                    <p class="field">
                        <label for="c_content"><fmt:message key="comments.message"/> :</label>
                        <textarea rows="7" cols="35" id="c_content" name="${inputs['commentBody'].name}"
                                  tabindex="13"></textarea>
                    </p>
                        <%--<template:captcha/>--%>
                        <%--captcha = <input name="captcha" />--%>
                    <p class="c_button">
                        <input type="submit" value="envoyer" class="button" tabindex="11"/>
                    </p>

                </fieldset>

            </div>
            <!--stop commentsForm-->
           </div>  
        </template:containerForm>
    </template:containerList>


</c:if>
