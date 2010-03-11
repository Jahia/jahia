<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="text" var="text"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
<template:addResources type="css" resources="blog.css"/>

<template:addWrapper name="hidden.blogWrapper"/>
<div class="grid_10  alpha omega">
<form method="post" action="${currentNode.name}/" name="blogPost">
    <input type="hidden" name="autoCheckin" value="true"/>
    <input type="hidden" name="nodeType" value="jnt:blogContent"/>
    <input type="hidden" name="normalizeNodeName" value="true"/>
    <fmt:formatDate value="${created.time}" type="date" pattern="dd" var="userCreatedDay"/>
    <fmt:formatDate value="${created.time}" type="date" pattern="mm" var="userCreatedMonth"/>
    <div class="post-date"><span>${userCreatedMonth}</span>${userCreatedDay}</div>
    <h2 class="post-title"><input type="text" value="" name="jcr:title"/></h2>

    <p class="post-info"><fmt:message key="by"/> <a href="#"></a>
        - <fmt:formatDate value="${userCreated.time}" type="date" dateStyle="medium"/>
        <a href="#"><fmt:message key="category"/></a>
    </p>
    <ul class="post-tags">
        <jcr:nodeProperty node="${currentNode}" name="j:tags" var="assignedTags"/>
        <c:forEach items="${assignedTags}" var="tag" varStatus="status">
            <li>${tag.node.name}</li>
        </c:forEach>
    </ul>
    <div class="post-content">
                   <textarea name="text" rows="10" cols="80" id="editContent"></textarea><br/>
        <p>

            <fmt:message key="jnt_blog.tagThisBlogPost"/>:&nbsp;
            <input type="text" name="j:newTag" value=""/>

            <fmt:message key='jnt_blog.noTitle' var="noTitle"/>
            <input
                    class="button"
                    type="button"
                    tabindex="16"
                    value="<fmt:message key='save'/>"
                    onclick="
                        if (document.blogPost.elements['jcr:title'].value == '') {
                            alert('${noTitle}');
                            return false;
                        }
                        document.blogPost.action = '${currentNode.name}/' + encodeURIComponent(document.blogPost.elements['jcr:title'].value);
                        document.blogPost.submit();
                    "
                    />
        </p>
    </div>
    <p class="post-info-links">
        <a class="comment_count" href="#"><fmt:message key="jnt_blog.noComment"/></a>
        <a class="ping_count" href="#"><fmt:message key="jnt_blog.noTrackback"/></a>
    </p>
</form>
</div>
