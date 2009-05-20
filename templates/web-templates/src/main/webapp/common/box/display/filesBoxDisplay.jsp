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


<%@ include file="../../declarations.jspf" %>
<template:containerList name="fileContainer" id="files" actionMenuNamePostFix="files" actionMenuNameLabelKey="files">
        <ul class="document">
        <template:container id="fileContainer" actionMenuNamePostFix="file" displayContainerAnchor="false">
            <template:field name="title" var="title" display="false"/>
            <template:field name="file" var="file" display="false"/>
            <template:field name="fileDisplayDetails" display="false" var="fileDisplayDetails"/>
            
            <li class="document ${file.file.picto}">
                <c:if test="${!empty title.value}">
                    <a href="${file.file.downloadUrl}">${title}</a>
                </c:if>
                <c:if test="${empty title.value}">
                <template:file file="file" useFilePictoAsCssClassName="false"/>
                </c:if>
                <c:if test="${fileDisplayDetails.boolean}">
                   <span class="docsize"><fmt:formatNumber var="num" pattern="### ### ###.##" type="number" value="${(file.file.size/1024)}"/>
                    (${num} Ko) &nbsp; <template:metadata contentBean="${fileContainer}" metadataName="created" asDate="true" var="creationDate"/><fmt:formatDate pattern="dd/MM/yyyy" value="${creationDate}"/></span>
                </c:if>
                    <span class="resume"><template:field name="fileDesc"/>
                    </span>
            </li>
        </template:container>
     </ul>
</template:containerList>