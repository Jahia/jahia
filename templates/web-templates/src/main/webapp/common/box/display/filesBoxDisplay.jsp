<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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