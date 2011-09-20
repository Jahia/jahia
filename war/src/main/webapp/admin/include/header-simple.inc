<%@page contentType="text/html; charset=UTF-8" %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import = "java.util.*"%>
<%@page import = "org.jahia.bin.JahiaAdministration"%>
<%@page import = "org.jahia.bin.Jahia"%>
<%@page import="org.jahia.params.ProcessingContext"%>
<%@page import="org.jahia.utils.i18n.JahiaResourceBundle"%>
<%@page import = "org.jahia.services.sites.JahiaSite"%>
<%@page import = "org.jahia.data.JahiaData"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<fmt:message key="org.jahia.copyright" var="i18nCopyright"/>
    <%

    String  jahiaDisplayMessage  = (String) request.getAttribute("jahiaDisplayMessage");
    String  URL = request.getContextPath() + "/engines/";
    JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");

    String  userAgent                        = request.getHeader("user-agent");
    int     inputSize                        = 38;
    boolean isLynx                           = false;

    if(userAgent != null) {
        if(userAgent.indexOf("MSIE") != -1) {
            inputSize = 36;
        } else if(userAgent.indexOf("Lynx") != -1) {
            isLynx    = true;
        }
    }

    JahiaSite currentSite = (JahiaSite)request.getAttribute("site");

    boolean isMSIE = request.getHeader("user-agent") != null && request.getHeader("user-agent").indexOf("MSIE") != -1;
    String copyright=Jahia.COPYRIGHT;
    String c = (String) pageContext.getAttribute("i18nCopyright");
    c = c != null && c.indexOf("???") == -1 ? c : "All rights reserved.";
    if (copyright.indexOf("All rights")!=-1){
        int p= copyright.indexOf("All rights");
     	  copyright=copyright.substring(0,p)+c;
    } else {
        copyright=copyright + "&nbsp;" +c;
    }

    final String contextPath = request.getContextPath();

    int stretcherToOpen = 1;
%>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=8"/>
    <title><fmt:message key="org.jahia.admin.jahiaAdministration.label"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <c:if test="${not empty sessionScope['org.jahia.usermanager.jahiauser'] && sessionScope['org.jahia.usermanager.jahiauser'].username != 'guest'}">
    <internal:gwtInit/>
    </c:if>
    <link rel="stylesheet" href="<c:url value='/css/admin-1.1.css'/>" type="text/css" />

    <c:if test="${not empty sessionScope['org.jahia.usermanager.jahiauser'] && sessionScope['org.jahia.usermanager.jahiauser'].username != 'guest'}">
    <script type="text/javascript">
        function showWorkInProgress() {
            if (typeof workInProgressOverlay != 'undefined') {
                workInProgressOverlay.start();
            }
        }
    </script>
    </c:if>
</head>
<body class="jahiaAdministration" ${not empty jahiaAdministrationLogin ? 'id="bodyLogin"' : ''}>
<div id="mainAdminLayout">