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
<%@ include file="../common/declarations.jspf" %>
<template:absoluteContainerList name="logo${param.id}" id="logoContainerList" pageLevel="1"
                                actionMenuNameLabelKey="logo.update">
    <template:container id="logoContainer">
        <a href='<template:composePageURL pageID="${requestScope.currentSite.homepageID}"/>' class="${param.position}"><template:image file="logo"/></a>
    </template:container>
</template:absoluteContainerList>
