<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="forum.css" nodetype="jnt:thread"/>
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

<%--<form action="${url.base}${currentNode.path}/*" method="post">
    <input type="hidden" name="nodeType" value="jnt:post"/>
    <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
    --%><%-- Define the output format for the newly created node by default html or by stayOnNode--%><%--
    <input type="hidden" name="newNodeOutputFormat" value="html">

    <div id="commentsForm"><!--start commentsForm-->
        <p></p>
        <fieldset>


            <p class="field">
                <input type="text" size="35" id="c_site" name="title"
                       value="<c:if test="${not empty currentNode.children}"> Re:</c:if>${currentNode.propertiesAsString['threadSubject']}" tabindex="1"/>
            </p>

            <p class="field">
                <textarea rows="7" cols="35" id="jahia-forum-thread-${currentNode.UUID}" name="content"
                          tabindex="2"></textarea>
            </p>

            <p class="commentsForm_button">
                <input type="reset" value="Annuler" class="button" tabindex="3"/>

                <input type="submit" value="Sauvegarder" class="button" tabindex="4"/>
            </p>
        </fieldset>
    </div>
</form>--%>
<form action="${url.base}${currentNode.path}/*" method="post">
    <input type="hidden" name="nodeType" value="jnt:post"/>
    <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
    <%-- Define the output format for the newly created node by default html or by stayOnNode--%>
    <input type="hidden" name="newNodeOutputFormat" value="html"/>

    <div class="post-reply"><!--start post-reply-->
        <div class="forum-box forum-box-style2">
            <span class="forum-corners-top"><span></span></span>

            <div id="forum-Form"><!--start forum-Form-->
                 <h4 class="forum-h4-first">${currentNode.propertiesAsString['threadSubject']} : <fmt:message key="reply"/></h4>

                <fieldset>
                    <p class="field">
                        <input value="<c:if test="${not empty currentNode.children}"> Re:</c:if>${currentNode.propertiesAsString['threadSubject']}" type="text" size="35" id="forum_site" name="jcr:title"
                               tabindex="1"/>
                    </p>

                    <p class="field">
                        <textarea rows="7" cols="35" id="jahia-forum-thread-${currentNode.UUID}" name="content" tabindex="2"></textarea>
                    </p>

                    <p class="forum_button">
                        <input type="reset" value="Reset" class="button" tabindex="3"/>

                        <input type="submit" value="Submit" class="button" tabindex="4"/>
                    </p>
                </fieldset>
            </div>
            <!--stop forum-Form-->


            <div class="clear"></div>
            <span class="forum-corners-bottom"><span></span></span>
        </div>
    </div>
    <!--stop post-reply-->
</form>