<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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
<template:addResources type="css" resources="forum.css"/>
<uiComponents:ckeditor selector="jahia-ckeditor"/>

<c:set var="linked" value="${uiComponents:getBindedComponentPath(currentNode, renderContext, 'j:bindedComponent')}"/>
<template:addResources type="css" resources="forum.css"/>
<script type="text/javascript">
    function jahiaForumQuote(targetId, quotedText) {
        var targetArea = document.getElementById(targetId);
        if (targetArea) {
            targetArea.value = targetArea.value + '\n<blockquote>\n' + quotedText + '\n</blockquote>\n';
        }
        return false;
    }
</script>

<a name="threadPost"></a>
<c:if test="${!empty param.reply}">
    <jcr:node uuid="${param.reply}" var="reply"/>
</c:if>
<template:tokenizedForm>
    <form action="${url.base}${linked}.addTopic.do" method="post" name="newTopicForm">
        <input type="hidden" name="nodeType" value="jnt:post"/>
        <input type="hidden" name="redirectTo"
               value="${url.base}${renderContext.mainResource.node.path}.${renderContext.mainResource.template}"/>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <input type="hidden" name="resourceID" value="${currentNode.identifier}"/>
        <div class="post-reply">
            <!--start post-reply-->
            <div class="forum-Form">
                <!--start forum-Form-->
                <h4 class="forum-h4-first">${fn:escapeXml(currentNode.displayableName)}:</h4>
                <fieldset>
                    <p class="field">
                        <fmt:message key="reply.prefix" var="replyPrefix"/><c:set var="replyPrefix"
                                                                                  value="${replyPrefix} "/>
                        <c:set var="replyTitle" value="${reply.properties['jcr:title'].string}"/>
                        <c:if test="${not empty sessionScope.formDatas['jcr:title']}"><input value="${sessionScope.formDatas['jcr:title'][0]}"
                               type="text" size="35" id="forum_site" name="jcr:title"
                               tabindex="1"/></c:if>
                        <c:if test="${empty sessionScope.formDatas['jcr:title']}">
                        <input value="${not empty replyTitle ? replyPrefix : ''}${not empty replyTitle ? fn:escapeXml(replyTitle) : ''}"
                               type="text" size="35" id="forum_site" name="jcr:title"
                               tabindex="1"/></c:if></p>

                    <p class="field">
                        <textarea rows="7" cols="35" id="jahia-forum-thread-${currentNode.UUID}" name="content"
                                  tabindex="2" class="jahia-ckeditor"><c:if test="${not empty sessionScope.formDatas['content']}">${fn:escapeXml(sessionScope.formDatas['content'][0])}</c:if><c:if test="${not empty reply.properties['content'].string}"><blockquote>${reply.properties['content'].string}</blockquote></c:if></textarea>
                    </p>
                    <c:if test="${not renderContext.loggedIn}">
                        <p class="field">
                            <label class="left" for="captcha"><template:captcha/></label>
                            <c:if test="${not empty sessionScope.formError}">
                                <label class="error">${sessionScope.formError}</label>
                            </c:if>
                            <input type="text" id="captcha" name="captcha"/>
                        </p>
                    </c:if>
                    <p class="forum_button">
                        <input type="reset" value="<fmt:message key='label.reset'/>" class="button" tabindex="3"/>
                        <input type="submit" value="<fmt:message key='label.submit'/>" class="button" tabindex="4"/>
                    </p>
                </fieldset>
            </div>
            <!--stop forum-Form-->
            <div class="clear"></div>
        </div>
        <!--stop post-reply-->
    </form>
</template:tokenizedForm>
