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
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title><fmt:message key="subscriptions.subscriptionConfirmation.title"/></title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <%@ include file="styles.jspf" %>
</head>
<body>
<div id="box">
    <div class="title"><fmt:message key="subscriptions.subscriptionConfirmation.title"/></div>
    <div class="content">
        <p>
        <c:choose>
            <c:when test="${confirmationResult == 'OK'}">
                <fmt:message key="subscriptions.subscriptionConfirmation.result.ok"/>
            </c:when>
            <c:when test="${confirmationResult == 'CONFIRMATION_KEY_MISMATCH'}">
                <fmt:message key="subscriptions.subscriptionConfirmation.result.keyMismatch"/>
            </c:when>
            <c:when test="${confirmationResult == 'ALREADY_CONFIRMED'}">
                <fmt:message key="subscriptions.subscriptionConfirmation.result.alreadyConfirmed"/>
            </c:when>
            <c:otherwise>
                <fmt:message key="subscriptions.subscriptionConfirmation.result.unknown"/>
            </c:otherwise>
        </c:choose>
        </p>
        
        <p><fmt:message key="notifications.common.farewell"/><br/><<fmt:message key="notifications.common.farewell.signature"/></p>
    </div>
</div>
</body>
</html>