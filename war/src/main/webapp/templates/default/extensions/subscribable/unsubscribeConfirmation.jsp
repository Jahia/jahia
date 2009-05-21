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
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title><fmt:message key="subscriptions.unsubscribeConfirmation.title"/></title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <%@ include file="styles.jspf" %>
</head>
<body>
<div id="box">
    <div class="title"><fmt:message key="subscriptions.unsubscribeConfirmation.title"/></div>
    <div class="content">
    <p>
    <c:choose>
        <c:when test="${confirmationResult == 'OK'}">
            <fmt:message key="subscriptions.unsubscribeConfirmation.result.ok"/>
        </c:when>
        <c:when test="${confirmationResult == 'CONFIRMATION_KEY_MISMATCH'}">
            <fmt:message key="subscriptions.unsubscribeConfirmation.result.keyMismatch"/>
        </c:when>
        <c:otherwise>
            <fmt:message key="subscriptions.subscriptionConfirmation.result.unknown"/>
        </c:otherwise>
    </c:choose>
    </p>
    
    <p><fmt:message key="notifications.common.farewell"/><br/><fmt:message key="notifications.common.farewell.signature"/></p>
    </div>
</div>
</body>
</html>