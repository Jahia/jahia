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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<html>
	<head>
		<title><internal:message key="button.workflow"/></title>
		<internal:gwtInit standalone="true"/>
		<internal:gwtImport module="org.jahia.ajax.gwt.module.workflow.WorkflowManager" />
	</head>
	<body>
    <%
        final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
        String siteKey ;
        if (jParams != null) {
            siteKey = jParams.getSiteKey() ;
        } else {
            siteKey = "Site" ;
        }
        String startPageId = request.getParameter("startpage") ;
        if (startPageId == null) {
            if (jParams != null) {
                startPageId = jParams.getContentPage().getObjectKey().getKey() ;
            } else {
                startPageId = "" ;
            }
        }
    %>

		<internal:workflowManager sitekey="<%=siteKey%>" startpage="<%=startPageId%>" />
        <internal:gwtGenerateDictionary/>   
	</body>
</html>