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
<template:absoluteContainerList name="basicLink${param.id}" id="links" displayActionMenu="false" pageLevel="1">
    <c:if test="${requestScope.currentRequest.editMode}">
        <table cellpadding="0" cellspacing="0">
        <tr>
    </c:if>
    <c:if test="${!requestScope.currentRequest.editMode}">
        <ul>
    </c:if>
    <c:if test="${requestScope.currentRequest.editMode}">
        <template:container id="linkContainer" displayActionMenu="false" displayContainerAnchor="false">
            <td class="${param.cssClassName}"><template:link page="link" maxChar="30"/></td>
            <td>
                <ui:actionMenu contentObjectName="linkContainer" namePostFix="link" labelKey="link.update"/>
            </td>
        </template:container>
    </c:if>
    <c:if test="${!requestScope.currentRequest.editMode}">
        <template:container id="linkContainer" displayActionMenu="false" displayContainerAnchor="false">

            <li class="${param.cssClassName}"><template:link page="link" maxChar="30"/></li>
        </template:container>
    </c:if>

    <c:if test="${requestScope.currentRequest.editMode}">
        <td><ui:actionMenu contentObjectName="links" namePostFix="links" labelKey="links.add"/></td>
        </tr>
        </table>
    </c:if>
    <c:if test="${!requestScope.currentRequest.editMode}">
        </ul>
    </c:if>
</template:absoluteContainerList>
