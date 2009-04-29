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
<%@ include file="../declarations.jspf" %>
<c:set var="name" scope="request" value="${param.name}"/>
<template:containerList name="webapps${name}" id="webapps"
                       actionMenuNamePostFix="webapps" actionMenuNameLabelKey="webapps.add">
    <template:container id="webappsContainer" actionMenuNamePostFix="webapp" actionMenuNameLabelKey="webapp.update">
        <template:field name="portlet" var="webapp" display="false"/>
        <c:if test="${!empty webapp.field.object}">
            <c:set var="portletWindowBean" value="${webapp.field.object}"/>
            <ui:portletModes name="portletWindowBean"/>
            <br/>
            <c:out escapeXml="false" value="${webapp.field.value}"/>
        </c:if>
    </template:container>
</template:containerList>