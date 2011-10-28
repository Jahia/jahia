<%@include file="/admin/include/header.inc" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="site" value="${sessionScope['org.jahia.services.sites.jahiasite']}"/>
<%
    pageContext.setAttribute("templatePackageByNodeName", ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByNodeName());
    pageContext.setAttribute("templateSetPackage", ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(((JahiaSite) pageContext.getAttribute("site")).getTemplatePackageName()));
    List<String> modules = new LinkedList<String>(((JahiaSite) pageContext.getAttribute("site")).getInstalledModules());
    if (modules.size() > 1) {
        modules = modules.subList(1, modules.size());
        Collections.sort(modules);
    } else {
        modules = Collections.emptyList();
    }
    pageContext.setAttribute("modules", modules);
%>
<div id="topTitle">
    <h1>Jahia</h1>
    <h2 class="edit"><fmt:message key="label.manageTemplates"/>:&nbsp;<fmt:message
            key="org.jahia.admin.site.label"/>&nbsp;${site.title}</h2>
</div>
<div id="main">
    <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
        <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/admin/include/tab_menu.inc" %>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
                <div class="dex-TabPanelBottom">
                    <div class="tabContent">
                        <jsp:include page="/admin/include/left_menu.jsp">
                            <jsp:param name="mode" value="site"/>
                        </jsp:include>
                        <div id="content" class="fit">
                            <div class="head">
                                <div class="object-title">
                                    <fmt:message key="label.manageTemplates"/>
                                </div>
                            </div>
                            <div class="content-item">
                                <table cellpadding="5" cellspacing="0" border="0">
                                    <tr>
                                        <td>
                                            <strong><fmt:message key="org.jahia.admin.site.ManageSites.templateSet.label"/>:</strong>
                                        </td>
                                        <td>
                                            ${fn:escapeXml(site.templatePackageName)}&nbsp;(${templateSetPackage.version})
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <strong><fmt:message key="label.modules"/>&nbsp;(<fmt:message key="moduleType.jahiapp.label"/>):</strong>
                                        </td>
                                        <td>
                                            <c:set var="found" value="false"/>
                                            <c:forEach var="module" items="${modules}">
                                                <c:set var="pkg" value="${templatePackageByNodeName[module]}"/>
                                                <c:if test="${pkg.moduleType == 'jahiapp'}">${found ? '<br/>' : ''}${fn:escapeXml(pkg.name)}&nbsp;(${pkg.version})<c:set var="found" value="true"/></c:if>
                                            </c:forEach>
                                            <c:if test="${not found}">
                                                <fmt:message key="label.none"/>
                                            </c:if>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <strong><fmt:message key="label.modules"/>&nbsp;(<fmt:message key="moduleType.module.label"/>):</strong>
                                        </td>
                                        <td>
                                            <c:set var="found" value="false"/>
                                            <c:forEach var="module" items="${modules}">
                                                <c:set var="pkg" value="${templatePackageByNodeName[module]}"/>
                                                <c:if test="${empty pkg.moduleType || pkg.moduleType == 'module' || pkg.moduleType == 'system'}">${found ? '<br/>' : ''}${fn:escapeXml(pkg.name)}&nbsp;(${pkg.version})<c:set var="found" value="true"/></c:if>
                                            </c:forEach>
                                            <c:if test="${not found}">
                                                <fmt:message key="label.none"/>
                                            </c:if>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <strong><fmt:message key="label.modules"/>&nbsp;(<fmt:message key="moduleType.profileModule.label"/>):</strong>
                                        </td>
                                        <td>
                                            <c:set var="found" value="false"/>
                                            <c:forEach var="module" items="${modules}" varStatus="status">
                                                <c:set var="pkg" value="${templatePackageByNodeName[module]}"/>
                                                <c:if test="${pkg.moduleType == 'profileModule'}">${found ? '<br/>' : ''}${fn:escapeXml(pkg.name)}&nbsp;(${pkg.version})<c:set var="found" value="true"/></c:if>
                                            </c:forEach>
                                            <c:if test="${not found}">
                                                <fmt:message key="label.none"/>
                                            </c:if>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div id="actionBar">
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-back"
         href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message
              key="label.backToMenu"/></a>
    </span>
  </span>
</div>
<%@include file="/admin/include/footer.inc" %>
