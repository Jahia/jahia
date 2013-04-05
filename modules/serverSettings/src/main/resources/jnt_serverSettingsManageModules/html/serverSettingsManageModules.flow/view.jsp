<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<table border="1">

    <tr>
        <th><fmt:message key='serverSettings.manageModules.moduleName' /></th>
        <th></th>
        <th><fmt:message key='serverSettings.manageModules.details' /></th>
        <th><fmt:message key='serverSettings.manageModules.versions' /></th>
        <th><fmt:message key='serverSettings.manageModules.status' /></th>
        <th><fmt:message key='serverSettings.manageModules.sources' /></th>
        <th><fmt:message key='serverSettings.manageModules.usedInSites' /></th>
    </tr>

    <c:forEach items="${allModuleVersions}" var="entry" >
        <c:set value="${registeredModules[entry.key]}" var="currentModule" />
        <tr>
            <td>${currentModule.name}</td>
            <td>${entry.key}</td>
            <c:url var="detailUrl" value="${url.base}/modules/${currentModule.rootFolder}.siteTemplate.html"/>
            <td><a href="${detailUrl}"><fmt:message key='serverSettings.manageModules.details' /></a>
            </td>
            <td>
                <c:forEach items="${entry.value}" var="version">
                    <c:choose>
                        <c:when test="${version.key eq currentModule.version}">
                            <div class="active-version">${version.key}
                                <form action="${flowExecutionUrl}" method="POST">
                                    <input type="hidden" name="module" value="${entry.key}"/>
                                    <fmt:message var="label" key='serverSettings.manageModules.stopModule' />
                                    <input type="submit" name="_eventId_stopModule" value="${label}" onclick=""/>
                                </form>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="inactive-version">${version.key}
                                <form action="${flowExecutionUrl}" method="POST">
                                    <input type="hidden" name="module" value="${entry.key}"/>
                                    <input type="hidden" name="version" value="${version.key}"/>
                                    <fmt:message var="label" key='serverSettings.manageModules.startModule' />
                                    <input type="submit" name="_eventId_startModule" value="${label}" onclick=""/>
                                </form>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </td>

            <td>
                <c:choose>
                    <c:when test="${not empty currentModule}">
                        Active
                    </c:when>
                    <c:otherwise>
                        Inactive
                    </c:otherwise>
                </c:choose>
            </td>

            <td>
                <%--${currentModule.sourcesFolder}--%>
                <c:choose>
                    <c:when test="${not empty currentModule.sourcesFolder}">
                        <input type="button" onclick='window.parent.location.assign("/cms/studio/${currentResource.locale}/modules/${currentModule.rootFolder}.siteTemplate.html")' value="<fmt:message key='serverSettings.manageModules.goToStudio' />"/>
                        <%--<c:if test="${renderContext.editModeConfigName ne 'studiomode' and renderContext.editModeConfigName ne 'studiolayoutmode'}">--%>
                            <%--<a href="/cms/studio/${currentResource.locale}/modules/${currentModule.rootFolder}.siteTemplate.html"></a>--%>
                        <%--</c:if>--%>

                    </c:when>
                    <c:when test="${not empty currentModule.scmURI}">
                        <form action="${flowExecutionUrl}" method="POST">
                            <input type="hidden" name="module" value="${entry.key}"/>
                            <input type="hidden" name="scmUri" value="${currentModule.scmURI}"/>
                            <fmt:message var="label" key='serverSettings.manageModules.downloadSources' />
                            <input type="submit" name="_eventId_downloadSources" value="${label}" onclick=""/>
                        </form>
                    </c:when>

                    <c:otherwise>
                        <form action="${flowExecutionUrl}" method="POST">
                            <input type="hidden" name="module" value="${entry.key}"/>
                            <input type="hidden" name="scmUri" value="scm:git:"/>
                            <fmt:message var="label" key='serverSettings.manageModules.downloadSources' />
                            <input type="submit" name="_eventId_downloadSources" value="${label}" onclick=""/>
                        </form>
                    </c:otherwise>
                </c:choose>

            </td>

        </tr>

    </c:forEach>
</table>