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
<c:set var="site" value="${renderContext.mainResource.node.resolveSite}"/>
<c:set var="siteKey" value="${site.name}"/>
<c:set var="templatePackageName" value="${site.templatePackageName}"/>
<%
    JahiaTemplateManagerService templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
    
    pageContext.setAttribute("templateSetPackage", templateService.getTemplatePackage((String) pageContext.getAttribute("templatePackageName")));
    pageContext.setAttribute("modules", templateService.getInstalledModulesForSite((String) pageContext.getAttribute("siteKey"), false, true, false));
    pageContext.setAttribute("requiredModules", templateService.getInstalledModulesForSite((String) pageContext.getAttribute("siteKey"), false, false, true));
%>
<h2>${fn:escapeXml(currentNode.displayableName)} - ${fn:escapeXml(site.displayableName)}</h2>
<p>
<strong><fmt:message key="serverSettings.manageWebProjects.webProject.templateSet"/>:&nbsp;</strong>
${fn:escapeXml(templatePackageName)}&nbsp;(${templateSetPackage.lastVersion})
</p>
<table class="table table-bordered table-striped table-hover" >
    <thead>
        <tr>
            <th>#</th>
            <th><fmt:message key="siteSettings.label.modules.id"/></th>
            <th><fmt:message key="siteSettings.label.modules.name"/></th>
            <th><fmt:message key="siteSettings.label.modules.version"/></th>
            <th><fmt:message key="siteSettings.label.modules.type"/></th>
            <th><fmt:message key="siteSettings.label.modules.source"/></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td colspan="6"><h3><fmt:message key="label.modules"/></h3></td>
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
            <td colspan="6"><h3><fmt:message key="siteSettings.requiredModules"/></h3></td>
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
