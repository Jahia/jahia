<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.

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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="commentable.css"/>
<template:addResources type="javascript" resources="jquery.js,jquery.validate.js"/>
<template:addResources type="inlinejavascript">
    <script type="text/javascript">
        $(document).ready(function() {
            $("#newCommentForm").validate({
                rules: {
                    'jcr:title': "required",
                    <c:if test="${not renderContext.loggedIn}">
                    pseudo: "required",
                    captcha: "required"
                    </c:if>
                }
            });
        });
    </script>
</template:addResources>
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">
    <a name="addComments"></a>

    <template:tokenizedForm>
        <form action="<c:url value='${url.base}${bindedComponent.path}.addComment.do'/>" method="post" id="newCommentForm">
            <input type="hidden" name="jcrNodeType" value="jnt:post"/>
            <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>
            <input type="hidden" name="jcrNewNodeOutputFormat" value="html"/>
            <input type="hidden" name="jcrResourceID" value="${currentNode.identifier}"/>

            <div id="formGenericComment">
                <fieldset>
                    <c:if test="${not renderContext.loggedIn}">
                        <p class="field">
                            <label for="comment_pseudo"><fmt:message key="comment.pseudo"/></label>
                            <input value="${sessionScope.formDatas['pseudo'][0]}"
                                   type="text" size="35" name="pseudo" id="comment_pseudo"
                                   tabindex="1"/>
                        </p>
                    </c:if>
                    <p class="field">
                        <label class="left" for="comment-title"><fmt:message key="comment.title"/></label>
                        <input class="" value="${sessionScope.formDatas['jcr:title'][0]}"
                               type="text" size="35" id="comment-title" name="jcr:title"
                               tabindex="1"/>
                    </p>

                    <p class="field">
                        <label class="left" for="jahia-comment-${bindedComponent.identifier}"><fmt:message
                                key="comment.body"/></label>
                        <textarea rows="7" cols="35" id="jahia-comment-${bindedComponent.identifier}"
                                  name="content"
                                  tabindex="2"><c:if
                                test="${not empty sessionScope.formDatas['content']}">${fn:escapeXml(sessionScope.formDatas['content'][0])}</c:if></textarea>
                    </p>

                    <c:if test="${not renderContext.loggedIn}">
                        <p class="field">
                            <label class="left" for="captcha"><fmt:message key="label.captcha"/></label>
                            <template:captcha/>
                        </p>
                        <p class="field">
                            <label class="left" for="captcha"><fmt:message key="label.captcha.enter"/></label>
                            <input type="text" id="captcha" name="jcrCaptcha"/>
                        </p>
                    </c:if>

                    <p>
                        <input type="reset" value="<fmt:message key='label.reset'/>" class="button"
                               tabindex="3"  ${disabled}/>

                        <input type="submit" value="<fmt:message key='label.submit'/>" class="button"
                               tabindex="4"  ${disabled} onclick=""/>
                    </p>
                </fieldset>
            </div>
        </form>
    </template:tokenizedForm>
</c:if>
