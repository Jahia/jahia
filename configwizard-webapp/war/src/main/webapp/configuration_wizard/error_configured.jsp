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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.bin.Jahia" %>
<%@ include file="header.inc" %>
<div class="head">
  <div class="object-title">
    <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.error.label"/>: <fmt:message key="org.jahia.bin.JahiaConfigurationWizard.errorconfigured.jahiaAlreadyInstalledAndConfigured.label"/>&nbsp;!
  </div>
</div>
<div id="pagebody">
  <p>
    <fmt:message key="org.jahia.pleaseFollowTheLink.label"/>.
  </p>
  <p>
    <a href="<%=Jahia.getContextPath()%><%=Jahia.getServletPath()%>" title="<fmt:message key="org.jahia.goToJahia.label"/>"><fmt:message key="org.jahia.goToJahia.label"/></a>
  </p>
</div>
</div>
<%@ include file="footer.inc" %>