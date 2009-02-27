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
%><?xml version="1.0" encoding="UTF-8"?>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.bin.Jahia" %>
<%@ page import="org.jahia.exceptions.JahiaServerOverloadedException"%>
<%@ page import="org.jahia.resourcebundle.JahiaResourceBundle"%>
<%@ page import="java.text.MessageFormat"%>
<%@ page import="org.jahia.params.ParamBean"%>
<%@ page import="org.jahia.exceptions.JahiaException"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal"
%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<c:set var="isDuringFirstRequest" value="${not empty requestScope['org.jahia.exception'] && requestScope['org.jahia.exception'].class.name == 'org.jahia.exceptions.JahiaServerOverloadedException' && requestScope['org.jahia.exception'].duringFirstRequest}"/>
<c:set var="timeInSeconds" value="${not empty requestScope['org.jahia.exception'] && requestScope['org.jahia.exception'].class.name == 'org.jahia.exceptions.JahiaServerOverloadedException' ? requestScope['org.jahia.exception'].suggestedRetryTime : 0}"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="robots" content="noindex, nofollow"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css" type="text/css"/>
<c:choose>
    <c:when test="${pageContext.request.method == 'GET' && isDuringFirstRequest && timeInSeconds > 0}">
        <title><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.ajax.loading"/></title>
        <script type="text/javascript">
        <!--
          function reloadPage()  {
              location.reload();
          } 
        
          window.onload = function() {
              setTimeout("reloadPage()", ${timeInSeconds}*1000);
          }
        //-->
        </script>        
    </c:when>
    <c:otherwise>
        <title><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.bin.JahiaErrorDisplay.httpServiceUnavailable.label"/></title>
    </c:otherwise>
</c:choose>
</head>

<body>
<c:choose>
    <c:when test="${pageContext.request.method == 'GET' && isDuringFirstRequest && timeInSeconds > 0}">
        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.ajax.loading"/>
    </c:when>
    <c:otherwise>
        <br/><br/><br/>
        <table class="errorbox" align="center" width="530" height="63" border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td class="boxtitle"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.bin.JahiaErrorDisplay.errorPage.label"/></td>
        </tr>
        <tr>
            <td class="boxcontent">
                <p class="bold"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.bin.JahiaErrorDisplay.serviceUnavailable.label"/></p>
            <%
            final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
            int timeInSeconds = (Integer) pageContext.getAttribute("timeInSeconds");
            String retryInMessage = "";
            if (timeInSeconds == 0) {
                retryInMessage = JahiaResourceBundle.getEngineResource(
                        "org.jahia.bin.JahiaErrorDisplay.retryLater.label", jParams,
                        jParams.getLocale());
            } else {
                int hours, minutes, seconds;
                hours = timeInSeconds / 3600;
                timeInSeconds = timeInSeconds - (hours * 3600);
                minutes = timeInSeconds / 60;
                timeInSeconds = timeInSeconds - (minutes * 60);
                seconds = timeInSeconds;
                Object[] arguments = {new Integer(hours), new Integer(minutes), new Integer(seconds)};
                retryInMessage = MessageFormat.format(JahiaResourceBundle.getEngineResource(
                        "org.jahia.bin.JahiaErrorDisplay.retryInTime.label", jParams,
                        jParams.getLocale()), arguments);                
            }
            %>
            <p><%=retryInMessage%>&nbsp;<a href="#reload" onclick="location.reload(); return false;" title='<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.bin.JahiaErrorDisplay.retry.label"/>'><img name="Retry" src="${pageContext.request.contextPath}/css/images/andromeda/icons/refresh.png" border="0" alt='<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.bin.JahiaErrorDisplay.retry.label"/>' title='<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.bin.JahiaErrorDisplay.retry.label"/>'></a></p>
            </td>
        </tr>
        </table>
    </c:otherwise>
</c:choose>
</body>
</html>
