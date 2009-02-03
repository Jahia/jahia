<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Subscription Confirmation</title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <style type="text/css">
    body {
        margin: 0;
    }
    
    body,p {
        font-family: Verdana, Arial, Helvetica, sans-serif;
        font-size: 11px;
        color: #333333;
    }
    
    #box {
        border: 1px solid #b7cbd8;
        width: 500px;
        margin: 50px auto;
    }
    #box .title {
        background-color: #d8e5ec;
        color:  #2f3c45;
        padding-left: 5px;
        padding-right: 5px;
        padding-top: 2px;
        padding-bottom: 2px;
        white-space: nowrap;
        font-weight: bold;
        border-bottom: 1px solid #b7cbd8;
    }
    #box .content {
        font-family:Verdana, Arial, Helvetica, sans-serif;
        padding: 5px;
        color: #666666;
        background-color: #f6f8fa;
    }
    </style>
</head>
<body>
<div id="box">
    <div class="title">Subscription confirmation</div>
    <div class="content">
        <p>
        <c:choose>
            <c:when test="${confirmationResult == 'OK'}">
                Thank you for confirming your subscription to our Content Update Notification Service.
            </c:when>
            <c:when test="${confirmationResult == 'CONFIRMATION_KEY_MISMATCH'}">
                Unfortunately, the confirmation key does not match the one we have. Please repeat the subscription process to request a new confirmation key.
            </c:when>
            <c:when test="${confirmationResult == 'ALREADY_CONFIRMED'}">
                This subscription is already confirmed. Thank you for using our Content Update Notification Service.
            </c:when>
            <c:otherwise>
                Unfortunately, we are unable to find any subscription, matching your request.
            </c:otherwise>
        </c:choose>
        </p>
        
        <p>Sincerely Yours<br>Portal Team</p>
    </div>
</div>
</body>
</html>