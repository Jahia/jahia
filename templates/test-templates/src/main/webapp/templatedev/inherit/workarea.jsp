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

<%@ include file="../../common/declarations.jspf" %>

<b><fmt:message key='inherit.include'/></b>
<template:include page="common/news/newsDisplay.jsp"/>
<br/><br/>
<b><fmt:message key='inherit.withparam'/></b>

<template:include page="common/box/box.jsp">
    <template:param name="name" value="columnC_box"/>
</template:include>
<br/>
<b>Execute an super JSP if one available:</b>
<b><fmt:message key='inherit.execute'/></b><br/>
<template:executeSuper/>
<br/><br/>


