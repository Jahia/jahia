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
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="commentable.css"/>
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
    <c:if test="${not empty bindedComponent}">
        <a name="addComments"></a>

        <template:tokenizedForm>
            <form action="${url.base}${bindedComponent.path}.addComment.do" method="post">

                <input type="hidden" name="nodeType" value="jnt:post"/>
                <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
                <input type="hidden" name="newNodeOutputFormat" value="html"/>

                <div id="formGenericComment">

                    <fieldset>
                        <p class="field">
                            <label class="left" for="comment-title"><fmt:message key="comment.title"/></label>
                            <input class="" value=""
                                   type="text" size="35" id="comment-title" name="jcr:title"
                                   tabindex="1"/>
                        </p>

                        <p class="field">
                            <label class="left" for="jahia-comment-${bindedComponent.identifier}"><fmt:message
                                    key="comment.body"/></label>
                            <textarea rows="7" cols="35" id="jahia-comment-${bindedComponent.identifier}"
                                      name="content"
                                      tabindex="2"></textarea>
                        </p>

                        <c:if test="${not renderContext.loggedIn}">
                        <p class="field">
                            <label class="left" for="captcha"><template:captcha /></label>
                            <input type="text" id="captcha" name="captcha"/>
                        </p>
                        </c:if>

                        <p>
                            <input type="reset" value="<fmt:message key='label.reset'/>" class="button"
                                   tabindex="3"  ${disabled}/>

                            <input type="submit" value="<fmt:message key='label.submit'/>" class="button"
                                   tabindex="4"  ${disabled}/>
                        </p>
                    </fieldset>
                </div>
            </form>
        </template:tokenizedForm>
    </c:if>
