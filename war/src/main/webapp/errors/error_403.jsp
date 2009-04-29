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
%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" 
%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%@page import="java.text.MessageFormat"
%><%@page import="org.jahia.bin.Jahia" 
%><%@page import="org.jahia.params.ParamBean" 
%><%@page import="org.jahia.utils.i18n.JahiaResourceBundle"
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
    <title><fmt:message key="org.jahia.bin.JahiaErrorDisplay.invalidMode.label"/></title>
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
    <title><fmt:message key="org.jahia.bin.JahiaErrorDisplay.httpForbidden.label"/></title>
</c:if>    
</head>
<body>
<br/><br/><br/>
<c:if test="${isInvalidModeError}">
<table class="errorbox" align="center" width="530" height="63" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td class="boxtitle"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.errorPage.label"/></td>
    </tr>
    <tr>
        <td class="boxcontent">
            <p class="bold"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.invalidModeRequested.label"/></p>

            <p><a href="#redirect" onclick="redirectToPage(); return false;">
                <%=MessageFormat.format(JahiaResourceBundle.getJahiaInternalResource(
                    "org.jahia.bin.JahiaErrorDisplay.redirectToNormal.label",
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
        <td colspan="2" class="boxtitle"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.errorPage.label"/></td>
    </tr>
    <tr>
        <td colspan="2" class="boxcontent">
            <p class="bold"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.error403.label"/></p>

            <p><fmt:message key="org.jahia.bin.JahiaErrorDisplay.accessForbidden.label"/></p>

            <c:if test="${isGuest}">
                <p><fmt:message key="org.jahia.bin.JahiaErrorDisplay.loginAgain.label"/></p>
            </c:if>
        </td>
    </tr>
    <tr>
        <td align="left" class="boxcontent">
            <c:if test="${isGuest}">
            <a href="javascript:EnginePopup('<%=Jahia.getServletPath()%>','login')" class="bold"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.login.label"/></a>
            </c:if>
            <c:if test="${not isGuest}">
            <a href="${pageContext.request.contextPath}/logout.jsp" class="bold"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.logout.label"/></a>
            </c:if>
        </td>
        <td align="right" class="boxcontent">
          <script type="text/javascript">
            <!--
            if (window.opener != null) {
                document.writeln( "<a href=\"javascript:window.close()\" class=\"bold\"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.close.label"/></a>" );
            }
            // -->
          </script>                    
        </td>
    </tr>
</table>
</c:if>
</body>
</html>
