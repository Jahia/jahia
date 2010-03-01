<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="forum.css" nodetype="jnt:boardIndex"/>
<form action="${url.base}${currentNode.path}/*" method="post">
    <input type="hidden" name="nodeType" value="jnt:topic"/>
    <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
    <%-- Define the output format for the newly created node by default html or by redirectTo--%>
    <input type="hidden" name="newNodeOutputFormat" value="detail.html"/>

    <div class="post-reply"><!--start post-reply-->
        <div class="forum-box forum-box-style2">
            <span class="forum-corners-top"><span></span></span>

            <div id="forum-Form"><!--start forum-Form-->
                <h4 class="forum-h4-first">${currentNode.propertiesAsString['boardSubject']} : Create new Topic</h4>

                <fieldset>
                    <p class="field">
                        <input value="New Topic Subject" type="text" size="35" id="forum_site" name="topicSubject"
                               tabindex="1"/>
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
</form>