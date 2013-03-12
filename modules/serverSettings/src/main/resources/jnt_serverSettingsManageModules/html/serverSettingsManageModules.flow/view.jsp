<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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
        <th>Module name</th>
        <th></th>
        <th>Details</th>
        <th>Versions</th>
        <th>Status</th>
        <th>Sources</th>
        <th>Used in sites</th>
    </tr>

    <c:forEach items="${allModuleVersions}" var="entry" >
        <c:set value="${registeredModules[entry.key]}" var="currentModule" />
        <tr>
            <td>${currentModule.name}</td>
            <td>${entry.key}</td>
            <td><a href="${url.base}/modules/${currentModule.rootFolder}.siteTemplate.html">Details</a>
                <c:if test="${renderContext.editModeConfigName ne 'studiomode' and renderContext.editModeConfigName ne 'studiolayoutmode'}">
                    <a href="/cms/studio/${currentResource.locale}/modules/${currentModule.rootFolder}.siteTemplate.html">Go to studio</a>
                </c:if>
            </td>
            <td>
                <c:forEach items="${entry.value}" var="version">
                    <c:choose>
                        <c:when test="${version.key eq currentModule.version}">
                            <div class="active-version">${version.key}</div>
                        </c:when>
                        <c:otherwise>
                            <div class="inactive-version">${version.key} <a href="#">Activate</a></div>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </td>

            <td>
                <c:choose>
                    <c:when test="${not empty currentModule}">
                        Active : <a href="#">stop</a>
                    </c:when>
                    <c:otherwise>
                        Inactive
                    </c:otherwise>
                </c:choose>
            </td>

            <td>
                <c:choose>
                    <c:when test="${not empty currentModule.sourcesFolder}">
                        Available
                    </c:when>
                    <c:otherwise>
                        Get from <a href="#">${currentModule.scmURI}</a>
                    </c:otherwise>
                </c:choose>

            </td>

        </tr>

    </c:forEach>
</table>