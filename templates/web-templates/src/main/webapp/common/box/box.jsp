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

<%-- Set up a variable to store the boxID value. This value will be used by the JSP files displaying the boxes --%>
<c:set var="boxID" scope="request" value="${param.name}"/>
<%-- Let us now display the main box list with all the different boxes it has --%>
<template:boxList name="${param.name}" id="${param.name}" actionMenuNamePostFix="boxes" actionMenuNameLabelKey="boxes">
    <template:container id="boxContainer" actionMenuNamePostFix="box" actionMenuNameLabelKey="box.update" cache="false" emptyContainerDivCssClassName="mockup-boxes">
        <template:field name="boxTitle" var="boxTitle" display="false"/>
        <%-- Set boxTitle to be use in box skinner template_rep/skins/myskin/myskin.jsp --%>
        <c:set var="boxTitle" scope="request" value="${boxTitle}"/>
        <%-- Let us invoke the box tag so it will dispatch the request to the correct JSP file used for box display --%>
        <template:box displayTitle="false" id="${param.name}" onError="${jahia.requestInfo.normalMode ? 'hide' : 'default'}"/>
    </template:container>
</template:boxList>