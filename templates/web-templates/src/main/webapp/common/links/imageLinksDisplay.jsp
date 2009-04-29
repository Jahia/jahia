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

<%@ include file="../declarations.jspf" %>
<template:absoluteContainerList name="imageLink${param.id}" id="links" displayActionMenu="false" pageLevel="1">
    <ul class="${param.cssClassName}">
        <template:container id="linkContainer" displayActionMenu="false" displayContainerAnchor="false">
            <template:field name='linkImage' var="linkImage" display="false"/>
            <c:if test="${empty linkImage}">
                <c:set var="styleLi" value=""/>
            </c:if>
            <c:if test="${not empty linkImage}">
                <c:set var="styleLi" value="style=\"background-image:url(${linkImage.file.downloadUrl})\""/>
            </c:if>
            <li class="codebase" ${styleLi}>
                <ui:actionMenu contentObjectName="linkContainer" namePostFix="link" labelKey="link.update">
                    <template:link page="link" maxChar="20"/>
                </ui:actionMenu>
            </li>
        </template:container>
        <c:if test="${requestScope.currentRequest.editMode}">
            <li><ui:actionMenu contentObjectName="links" namePostFix="links" labelKey="links.add"/></li>
        </c:if>
    </ul>
</template:absoluteContainerList>
