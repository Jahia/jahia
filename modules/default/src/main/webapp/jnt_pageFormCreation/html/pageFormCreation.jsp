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
<template:addResources type="css" resources="pageformcreation.css"/>

<c:set var="formid" value="form" />
<c:set var="nodeType" value="jnt:page" />

<c:if test="${not empty currentNode.properties['class']}">
    <div class="${currentNode.properties['class'].string}">
</c:if>

<template:tokenizedForm>
<form class="pageFormCreation" method="post" action="${renderContext.mainResource.node.name}/*" name="${formid}">
    <c:if test="${currentNode.properties.i18npages.boolean}">
        <input type="hidden" name="jcrNodeType" value="jnt:page">
    </c:if>
    <c:if test="${not currentNode.properties.i18npages.boolean}">
        <input type="hidden" name="jcrNodeType" value="jnt:noni18npage">
    </c:if>
    <input type="hidden" name="jcrNormalizeNodeName" value="true"/>
    <input type="hidden" name="jcrAutoAssignRole" value="owner"/>
    <input type="hidden" name="jcr:mixinTypes" value="jmix:hasTemplateNode"/>
    <input type="hidden" name="j:templateNode" value="${currentNode.properties['templateNode'].string}"/>
    <c:if test="${currentNode.properties.stayOnPage.boolean}">
        <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>
    </c:if>
    <h3>${fn:escapeXml(currentNode.displayableName)}</h3>
    <fieldset>
        <legend>${fn:escapeXml(currentNode.displayableName)}</legend>

        <p><label for="title"><fmt:message key="label.title"/></label>
            <input type="text" name="jcr:title" id="title" class="field" value=""
                   tabindex="20"/></p>


        <c:if test="${currentNode.properties['useDescription'].boolean}">
        <p><label for="description"><fmt:message
                key="label.description"/></label>
            <textarea name="jcr:description" id="description" cols="45" rows="3"
                      tabindex="21" ></textarea></p>
        </c:if>
        <div>
            <input type="submit" class="button"
                   value="${currentNode.properties['buttonLabel'].string}" tabindex="28"
                   onclick="if (document.${formid}.elements['jcr:title'].value == '') {
                               alert('you must fill the title ');
                               return false;
                           }
                           document.${formid}.submit();
                       "
                     ${disabled} />
        </div>
    </fieldset>
</form>
</template:tokenizedForm>

<c:if test="${not empty currentNode.properties['class']}">
    </div>
</c:if>
