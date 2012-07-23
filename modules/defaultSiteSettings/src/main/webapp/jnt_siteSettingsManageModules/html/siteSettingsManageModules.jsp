<%@ page import="java.util.*,
                 org.apache.commons.lang.StringUtils,
                 org.jahia.registries.ServicesRegistry,
                 org.jahia.data.templates.JahiaTemplatesPackage,
                 org.jahia.services.templates.JahiaTemplateManagerService,
                 org.jahia.services.sites.JahiaSite" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="site" value="${currentNode.resolveSite}"/>
<c:set var="siteKey" value="${site.name}"/>
<c:set var="installedModules" value="${site.installedModules}"/>
<c:set var="templatePackageName" value="${site.templatePackageName}"/>
<%
    JahiaTemplateManagerService templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
    
    pageContext.setAttribute("templateSetPackage", templateService.getTemplatePackage((String) pageContext.getAttribute("templatePackageName")));
    pageContext.setAttribute("modules", templateService.getInstalledModulesForSite((String) pageContext.getAttribute("siteKey"), false, true, false));
    pageContext.setAttribute("requiredModules", templateService.getInstalledModulesForSite((String) pageContext.getAttribute("siteKey"), false, false, true));
%>
<h2>${fn:escapeXml(currentNode.displayableName)} - ${fn:escapeXml(site.displayableName)}</h2>
<p>
<strong><fmt:message key="org.jahia.admin.site.ManageSites.templateSet.label"/>:&nbsp;</strong>
${fn:escapeXml(templatePackageName)}&nbsp;(${templateSetPackage.lastVersion})
</p>
<table style="width: 100%;" cellpadding="0" cellspacing="0" border="1">
    <thead>
        <tr>
            <th>#</th>
            <th>ID</th>
            <th>Name</th>
            <th>Version</th>
            <th>Type</th>
            <th>Source</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td colspan="6"><strong><fmt:message key="label.modules"/></strong></td>
        </tr>
        <c:forEach var="pkg" items="${modules}" varStatus="status">
        <tr>
            <td>${status.index + 1}</td>
            <td>${pkg.rootFolder}</td>
            <td>${pkg.name}</td>
            <td>${pkg.lastVersion}</td>
            <td><fmt:message key="moduleType.${pkg.moduleType}.label"/></td>
            <td>${pkg.provider}</td>
        </tr>
        </c:forEach>
        <c:if test="${not empty requiredModules}">
        <tr>
            <td colspan="6"><strong><fmt:message key="siteSettings.requiredModules"/></strong></td>
        </tr>
        <c:forEach var="pkg" items="${requiredModules}" varStatus="status">
        <tr>
            <td>${status.index + 1}</td>
            <td>${pkg.rootFolder}</td>
            <td>${pkg.name}</td>
            <td>${pkg.lastVersion}</td>
            <td><fmt:message key="moduleType.${pkg.moduleType}.label"/></td>
            <td>${pkg.provider}</td>
        </tr>
        </c:forEach>
        </c:if>
    </tbody>
</table>
