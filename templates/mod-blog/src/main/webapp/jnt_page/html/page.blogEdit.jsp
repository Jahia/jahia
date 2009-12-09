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
<template:addWrapper name="blogWrapper"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/ckeditor/ckeditor.js,ckeditor_init.js"/>
<script type="text/javascript">
    $(document).ready(function() {
        $.each(['editContent'], function(index, element) {
            if ($('#' + element).length > 0) {
                $('label[for="' + element + '"]').hide();
                CKEDITOR.replace(element, ck.config);
            }
        });
    });
</script>
<form method="post" action="${currentNode.name}/" name="blogPost">
    <input type="hidden" name="autoCheckin" value="true">
    <input type="hidden" name="nodeType" value="jnt:blogContent">
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
        <p>
            <textarea name="text" rows="10" cols="80" id="editContent">
            </textarea>
            <fmt:message key="tag.this.article"/>:&nbsp;
            <input type="text" name="j:newTag" value=""/>

        </p>
        <p class="c_button">
            <input
                    class="button"
                    type="button"
                    tabindex="16"
                    value="<fmt:message key='save'/>"
                    onclick="
                        if (document.blogPost.elements['jcr:title'].value == '') {
                            alert('you must fill the title ');
                            return false;
                        }
                        document.blogPost.action = '${currentNode.name}/'+document.blogPost.elements['jcr:title'].value.replace(' ','');
                        document.blogPost.submit();
                    "
                    />
        </p>
    </div>
    <p class="post-info-links">
        <a class="comment_count" href="#">aucun commentaire</a>
        <a class="ping_count" href="#">aucun rétrolien</a>
    </p>
</form>

