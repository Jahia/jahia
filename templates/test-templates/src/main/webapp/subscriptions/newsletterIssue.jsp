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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="../common/declarations.jspf" %>
<template:template doctype="html-transitional">
<template:templateHead>
<link type="text/css" rel="stylesheet" href="${jahia.includes.webPath['/common/css/styles.css']}">
</template:templateHead>
<template:templateBody>
<center>
    <div id="columnB" style="text-align: left;">

    <template:containerList name="newsletterHeader" id="headers">
        <c:if test="${not empty headers}">
            <template:container id="header">
                <template:field name="logo" display="false" var="logo"/>
                <h1>
                <c:if test="${not empty logo}">
                    <template:image file="logo" cssClassName="left"/>
                </c:if>
                ${jahia.page.title}</h1>
                <hr><br>
                <p><template:field name="introduction"/></p>
            </template:container>
        </c:if>
    </template:containerList>
    <template:containerList name="maincontent" id="maincontents">
        <c:if test="${not empty maincontents}">
            <template:container id="maincontent">
                <div>
                    <h3><template:field name="mainContentTitle"/></h3>
                    <p>
                        <template:field name="mainContentImage" display="false" var="image"/>
                        <template:field name="mainContentAlign" display="false" var="align"/>
                        <template:image file="image" cssClassName="${fn:toLowerCase(align)}"/>
                        <template:field name="mainContentBody"/>
                    </p>
                    <br class="clear"/>
                </div>
            </template:container>
        </c:if>
    </template:containerList>
    <template:containerList name="newsletterFooter" id="footers">
        <c:if test="${not empty footers}">
            <template:container id="footer">
                <p><template:field name="footer"/></p>
            </template:container>
        </c:if>
    </template:containerList>
    </div>
</center>
</template:templateBody>
</template:template>