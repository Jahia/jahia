<%@page language="java" contentType="text/html;charset=UTF-8" %>
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
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Custom 404 error page</title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/templates/test_templates/common/css/styles.css"
          type="text/css"/>
</head>

<body>

<center>
    <div id="columnB" style="text-align: left; padding-top: 50px">
        <h3>Custom 404 error page</h3>

        <p><fmt:message key="org.jahia.bin.JahiaErrorDisplay.fileNotFound.label"/></p>

        <p><fmt:message key="org.jahia.bin.JahiaErrorDisplay.clickHere1stPart.label"/>&nbsp;<a
                href="javascript:history.back()"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.clickHere2ndPartLink.label"/></a>&nbsp;
            <fmt:message key="org.jahia.bin.JahiaErrorDisplay.clickHere3rdPart.label"/></p>
    </div>
</center>
</body>
</html>
