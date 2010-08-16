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

<jcr:nodeProperty node="${renderContext.mainResource.node}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${renderContext.mainResource.node}" name="text" var="text"/>
<jcr:nodeProperty node="${renderContext.mainResource.node}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${renderContext.mainResource.node}" name="jcr:created" var="created"/>
<template:addResources type="css" resources="blog.css"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<script type="text/javascript">
    $(document).ready(function() {
        $.each(['editContent'], function(index, element) {
            if ($('#' + element).length > 0) {
                $('label[for="' + element + '"]').hide();
                CKEDITOR.replace(element, { toolbar : 'User'});
            }
        });
    });
</script>
<div class="post">
    <form method="post" action="${renderContext.mainResource.node.name}/" name="blogPost">
        <input type="hidden" name="autoCheckin" value="true">
        <input type="hidden" name="nodeType" value="jnt:blogContent">
        <fmt:formatDate value="${created.time}" type="date" pattern="dd" var="userCreatedDay"/>
        <fmt:formatDate value="${created.time}" type="date" pattern="mm" var="userCreatedMonth"/>
        <div class="post-date"><span>${userCreatedMonth}</span>${userCreatedDay}</div>
        <h2 class="post-title"><input type="text" value="<c:out value='${title.string}'/>" name="jcr:title"/></h2>
        <p class="post-info"><fmt:message key="blog.label.by"/> <a href="${url.base}/users/${createdBy.string}.html">${createdBy.string}</a>
            - <fmt:formatDate value="${created.time}" type="date" dateStyle="medium"/>
        </p>
        <ul class="post-tags">
            <c:set var="tags" value=""/>
            <jcr:nodeProperty node="${renderContext.mainResource.node}" name="j:tags" var="assignedTags"/>
            <c:forEach items="${assignedTags}" var="tag" varStatus="status">
                <li>${tag.node.name}</li>
                <c:set var="tags" value="${tags}${tag.node.name}${!status.last ? ',' : ''}"/>
            </c:forEach>
        </ul>
        <div class="post-content">
                <p><textarea name="text" rows="10" cols="70" id="editContent">
                    ${fn:escapeXml(text.string)}
                </textarea>	</p>
            <p>

                <fmt:message key="blog.label.tag"/>:&nbsp;
                <input type="text" name="j:newTag" value="${tags}"/>
                <input
                        class="button"
                        type="button"
                        tabindex="16"
                        value="<fmt:message key='blog.label.save'/>"
                        onclick="document.blogPost.submit();"
                        />
            </p>
        </div>
    </form>
</div>