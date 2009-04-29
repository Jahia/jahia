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
<%@ include file="../../declarations.jspf" %>

<template:containerList name="files${boxID}" id="files">
    <table class="files" border="0" cellspacing="0" cellpadding="0" width="100%">
        <template:container id="fileContainer">
            <template:field name="boxFile" var="myFileField" display="false"/>
            <template:field name="boxFileDisplayDetails" var="displayDetails" display="false"/>
            <c:if test="${!empty myFileField}">
                <tr>
                    <td>
                        <c:if test="${myFileField.file.downloadable}">
                            <a href="${myFileField.file.downloadUrl}"
                               title="${myFileField.file.fileFieldTitle}">
                                <c:out value="${myFileField.file.fileFieldTitle}"/>
                            </a>
                        </c:if>
                    </td>
                    <td>
                        <template:field var="boxFileDesc" name="fileDesc" display="false"/>
                        <c:if test="${!empty fileDesc}">
                            <c:out value="${fileDesc}" escapeXml="false"/>
                        </c:if>
                    </td>
                    <c:choose>
                        <c:when test="${displayDetails == 'true'}">
                            <td class="nowrap">${myFileField.file.formatedSize}</td>
                            <td class="nowrap">${myFileField.file.formatedLastModifDate}</td>
                        </c:when>
                        <c:otherwise>
                            <td>&nbsp;</td>
                            <td>&nbsp;</td>
                        </c:otherwise>
                    </c:choose>
                </tr>
            </c:if>
        </template:container>
    </table>
</template:containerList>