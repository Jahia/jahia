<%@page contentType="text/html; charset=UTF-8" %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
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
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import = "java.util.*"%>
<%@page import = "org.jahia.bin.JahiaAdministration"%>
<%@page import = "org.jahia.bin.Jahia"%>
<%@page import="org.jahia.params.ProcessingContext"%>
<%@page import="org.jahia.utils.i18n.JahiaResourceBundle"%>
<%@page import = "org.jahia.utils.JahiaTools"%>
<%@page import = "org.jahia.services.sites.JahiaSite"%>
<%@page import = "org.jahia.data.JahiaData"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%!
public String getAdminResource(String messageKey, HttpServletRequest request) {
    ProcessingContext jParams = ((JahiaData) request.getAttribute("org.jahia.data.JahiaData")).getProcessingContext();
    return JahiaResourceBundle.getJahiaInternalResource(messageKey, jParams.getLocale());    
}
%>

<%

    String  jahiaDisplayMessage  = (String) request.getAttribute("jahiaDisplayMessage");
    String  URL = (String) request.getAttribute("URL");
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
    
    boolean isMSIE = JahiaTools.isMSIExplorer(request);
    String copyright=Jahia.COPYRIGHT;
    String c;
    try {
      	c=getAdminResource("org.jahia.copyright", request);
    } catch (MissingResourceException e) {
      	c="All rights reserved.";
    }
    if (copyright.indexOf("All rights")!=-1){
        int p= copyright.indexOf("All rights");
     	  copyright=copyright.substring(0,p)+c;
    } else {
        copyright=copyright + "&nbsp;" +c;
    }
    
    final String behaviorPath = request.getContextPath() + "/css/pngbehavior.jsp";
    
    final String contextPath = request.getContextPath();

    final boolean isConfigWizard = session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") != null || request.getAttribute(JahiaAdministration.CLASS_NAME + "configWizard") != null;
    int stretcherToOpen = 1;
%>
<head>
    <title><fmt:message key="org.jahia.admin.jahiaAdministration.label"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <internal:gwtInit/>
    <link rel="stylesheet" href="<%=contextPath%>/css/andromeda.css" type="text/css" />
    <!--[if lte IE 6]>       
        <style type="text/css" media="all">@import "<%=contextPath%>/css/andromeda_ie6.css";</style>
            <style type="text/css">
			img {
			behavior: url("<%=contextPath%>/css/pngbehavior.jsp")}
			</style>


    <![endif]-->

    <script type="text/javascript" src="<%=contextPath%>/javascript/jahia.js"></script>
    <script type="text/javascript" src="<%=contextPath%>/javascript/actionlauncher.js"></script>
    

    <script type="text/javascript">
        function showWorkInProgress() {
	        if (typeof workInProgressOverlay != 'undefined') {
	            workInProgressOverlay.start();
	        }
        }
    </script>
</head>
<body class="jahiaAdministration" ${not empty jahiaAdministrationLogin ? 'id="bodyLogin"' : ''} onunload="closeEngineWin()" onclose="closeEngineWin()">
<div id="mainAdminLayout">
<%if(isConfigWizard){%>
<%@ include file="/configuration_wizard/admin_header.inc" %>
<%}%>
