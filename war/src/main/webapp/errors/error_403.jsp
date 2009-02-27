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

<%@page language="java" contentType="text/html; charset=UTF-8" 
%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" 
%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@page import="java.text.MessageFormat"
%><%@page import="org.jahia.bin.Jahia" 
%><%@page import="org.jahia.params.ParamBean" 
%><%@page import="org.jahia.resourcebundle.JahiaResourceBundle" 
%><?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"><%!
    private final int REDIRECTION_DELAY = 5000;
%><%
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
%>
<c:set var="isInvalidModeError" value="${not empty requestScope['org.jahia.exception'] && requestScope['org.jahia.exception'].class.name == 'org.jahia.exceptions.JahiaInvalidModeException'}"/>
<c:set var="isGuest" value="${empty sessionScope['org.jahia.usermanager.jahiauser'] || sessionScope['org.jahia.usermanager.jahiauser'].username == 'guest'}"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css" type="text/css"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/javascript/jahia.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/errors/error_include.js"></script>
<c:if test="${isInvalidModeError}">
    <title><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.bin.JahiaErrorDisplay.invalidMode.label"/></title>
    <script type="text/javascript">
        <!--
        function redirectToPage()  {
            window.location.href = "<%=jParams.composeOperationUrl(ParamBean.NORMAL, null)%>";
        }	
      
        window.onload = function() {
            setTimeout("redirectToPage()", <%=REDIRECTION_DELAY%>);
        }
    //-->
    </script>
</c:if>
<c:if test="${not isInvalidModeError}">
    <title><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.bin.JahiaErrorDisplay.httpForbidden.label"/></title>
</c:if>    
</head>
<body>
<br/><br/><br/>
<c:if test="${isInvalidModeError}">
<table class="errorbox" align="center" width="530" height="63" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td class="boxtitle"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                resourceName="org.jahia.bin.JahiaErrorDisplay.errorPage.label"/></td>
    </tr>
    <tr>
        <td class="boxcontent">
            <p class="bold"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                    resourceName="org.jahia.bin.JahiaErrorDisplay.invalidModeRequested.label"/></p>

            <p><a href="#redirect" onclick="redirectToPage(); return false;">
                <%=MessageFormat.format(JahiaResourceBundle.getEngineResource(
                    "org.jahia.bin.JahiaErrorDisplay.redirectToNormal.label", jParams,
                    jParams.getLocale()), new Object[]{new Integer(REDIRECTION_DELAY / 1000)})%>
                </a>
            </p>
        </td>
    </tr>
</table>
</c:if>
<c:if test="${not isInvalidModeError}">
<table class="errorbox" align="center" width="530" height="63" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td colspan="2" class="boxtitle"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                resourceName="org.jahia.bin.JahiaErrorDisplay.errorPage.label"/></td>
    </tr>
    <tr>
        <td colspan="2" class="boxcontent">
            <p class="bold"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                    resourceName="org.jahia.bin.JahiaErrorDisplay.error403.label"/></p>

            <p><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.bin.JahiaErrorDisplay.accessForbidden.label"/></p>

            <c:if test="${isGuest}">
                <p><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.bin.JahiaErrorDisplay.loginAgain.label"/></p>
            </c:if>
        </td>
    </tr>
    <tr>
        <td align="left" class="boxcontent">
            <c:if test="${isGuest}">
            <a href="javascript:EnginePopup('<%=Jahia.getServletPath()%>','login')" class="bold"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                    resourceName="org.jahia.bin.JahiaErrorDisplay.login.label"/></a>
            </c:if>
            <c:if test="${not isGuest}">
            <a href="${pageContext.request.contextPath}/logout.jsp" class="bold"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                    resourceName="org.jahia.bin.JahiaErrorDisplay.logout.label"/></a>
            </c:if>
        </td>
        <td align="right" class="boxcontent">
          <script type="text/javascript">
            <!--
            if (window.opener != null) {
                document.writeln( "<a href=\"javascript:window.close()\" class=\"bold\"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.bin.JahiaErrorDisplay.close.label"/></a>" );
            }
            // -->
          </script>                    
        </td>
    </tr>
</table>
</c:if>
</body>
</html>
