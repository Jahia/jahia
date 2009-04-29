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
<%@ page language = "java" %><%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" 
%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="robots" content="noindex, nofollow"/>
    <title><fmt:message key="org.jahia.bin.JahiaErrorDisplay.jahiaError.label"/></title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css" type="text/css"/>
</head>

<body>

<br/><br/><br/>

<table class="errorbox" align="center" width="530" height="63" border="0" cellspacing="0" cellpadding="0">
<tr>
    <td class="boxtitle"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.error.label"/></td>
</tr>
<tr>
    <td class="boxcontent">
        <br/>
        <span class="bold"><fmt:message key="org.jahia.bin.Jahia.maintenance"/></span><br/>
        <br/>
    </td>
</tr>
</table>

</body>
</html>
