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
<%@page language="java" contentType="text/html; charset=UTF-8" 
%><?xml version="1.0" encoding="UTF-8"?>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.bin.Jahia" %>
<%@ page import="org.jahia.exceptions.JahiaServerOverloadedException"%>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle"%>
<%@ page import="java.text.MessageFormat"%>
<%@ page import="org.jahia.params.ParamBean"%>
<%@ page import="org.jahia.exceptions.JahiaException"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal"
%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
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
        <title><fmt:message key="org.jahia.ajax.loading"/></title>
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
        <title><fmt:message key="org.jahia.bin.JahiaErrorDisplay.httpServiceUnavailable.label"/></title>
    </c:otherwise>
</c:choose>
</head>

<body>
<c:choose>
    <c:when test="${pageContext.request.method == 'GET' && isDuringFirstRequest && timeInSeconds > 0}">
        <fmt:message key="org.jahia.ajax.loading"/>
    </c:when>
    <c:otherwise>
        <br/><br/><br/>
        <table class="errorbox" align="center" width="530" height="63" border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td class="boxtitle"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.errorPage.label"/></td>
        </tr>
        <tr>
            <td class="boxcontent">
                <p class="bold"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.serviceUnavailable.label"/></p>
            <%
            final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
            int timeInSeconds = (Integer) pageContext.getAttribute("timeInSeconds");
            String retryInMessage = "";
            if (timeInSeconds == 0) {
                retryInMessage = JahiaResourceBundle.getJahiaInternalResource(
                        "org.jahia.bin.JahiaErrorDisplay.retryLater.label",
                        jParams.getLocale());
            } else {
                int hours, minutes, seconds;
                hours = timeInSeconds / 3600;
                timeInSeconds = timeInSeconds - (hours * 3600);
                minutes = timeInSeconds / 60;
                timeInSeconds = timeInSeconds - (minutes * 60);
                seconds = timeInSeconds;
                Object[] arguments = {hours, minutes, seconds};
                retryInMessage = MessageFormat.format(JahiaResourceBundle.getJahiaInternalResource(
                        "org.jahia.bin.JahiaErrorDisplay.retryInTime.label",
                        jParams.getLocale()), arguments);                
            }
            %>
            <p><%=retryInMessage%>&nbsp;<a href="#reload" onclick="location.reload(); return false;" title='<fmt:message key="org.jahia.bin.JahiaErrorDisplay.retry.label"/>'><img name="Retry" src="${pageContext.request.contextPath}/css/images/andromeda/icons/refresh.png" border="0" alt='<fmt:message key="org.jahia.bin.JahiaErrorDisplay.retry.label"/>' title='<fmt:message key="org.jahia.bin.JahiaErrorDisplay.retry.label"/>'></a></p>
            </td>
        </tr>
        </table>
    </c:otherwise>
</c:choose>
</body>
</html>
