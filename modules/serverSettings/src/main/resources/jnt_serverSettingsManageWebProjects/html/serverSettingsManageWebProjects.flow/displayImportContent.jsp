<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="mailSettings" type="org.jahia.services.mail.MailSettings"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="flowExecutionUrl" type="java.lang.String"--%>
<%--@elvariable id="webprojectHandler" type="org.jahia.modules.serversettings.flow.WebprojectHandler"--%>

<div>
<p>
    <c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
        <c:if test="${message.severity eq 'ERROR'}">
            <span style="color: red;">${message.text}</span><br/>
        </c:if>
    </c:forEach>
</p>
    <form action="${flowExecutionUrl}" method="post">
        <ol>
            <c:forEach items="${webprojectHandler.importsInfos}" var="importInfoMap">
                <li>
                    <input type="checkbox" id="${importInfoMap.key}" name="importsInfos['${importInfoMap.key}'].selected" value="true"
                           <c:if test="${importInfoMap.value.selected}">checked="checked"</c:if>/>
                    <input type="hidden" id="${importInfoMap.key}" name="_importsInfos['${importInfoMap.key}'].selected"/>
                    <label for="${importInfoMap.key}">${importInfoMap.key}</label>
                    <%@include file="importValidation.jspf"%>
                    <c:if test="${importInfoMap.value.site}">
                        <ul>
                            <li>
                                <label for="${importInfoMap.value.siteKey}siteTitle">
                                    <fmt:message key="org.jahia.admin.site.ManageSites.siteTitle.label"/>
                                </label>
                                <input type="text" id="${importInfoMap.value.siteKey}siteTitle"
                                       name="importsInfos['${importInfoMap.key}'].siteTitle"
                                       value="${fn:escapeXml(importInfoMap.value.siteTitle)}"/>
                            </li>
                            <li>
                                <label for="${importInfoMap.value.siteKey}siteServerName">
                                    <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>
                                </label>
                                <input type="text" id="${importInfoMap.value.siteKey}siteServerName"
                                       name="importsInfos['${importInfoMap.key}'].siteServername"
                                       value="${fn:escapeXml(importInfoMap.value.siteServername)}"/>
                            </li>
                            <li>
                                <label for="${importInfoMap.value.siteKey}siteKey">
                                    <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>
                                </label>
                                <input type="text" id="${importInfoMap.value.siteKey}siteKey"
                                       name="importsInfos['${importInfoMap.key}'].siteKey"
                                       value="${fn:escapeXml(importInfoMap.value.siteKey)}"/>
                            </li>
                            <li>
                                <label for="${importInfoMap.value.siteKey}templates">
                                    <fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseTemplateSet.label"/>
                                </label>
                                <select id="${importInfoMap.value.siteKey}templates" name="importsInfos['${importInfoMap.key}'].templates">
                                    <c:forEach items="${requestScope.templateSets}" var="module">
                                            <option value="${module}" <c:if test="${importInfoMap.value.templates eq module}"> selected="selected"</c:if>>${module}</option>
                                    </c:forEach>
                                </select>
                            </li>
                            <c:if test="${importInfoMap.value.legacyImport}">
                                <li>
                                    <label for="${importInfoMap.value.siteKey}legacyMapping">
                                        <fmt:message key="org.jahia.admin.site.ManageSites.selectDefinitionMapping"/>
                                    </label>
                                    <select id="${importInfoMap.value.siteKey}legacyMapping"
                                            name="importsInfos['${importInfoMap.key}'].selectedLegacyMapping">
                                        <c:forEach items="${importInfoMap.value.legacyMappings}" var="module">
                                            <option value="${module.absolutePath}" <c:if
                                                    test="${importInfoMap.value.selectedLegacyMapping eq module.name}"> selected="selected"</c:if>>${module.name}</option>
                                        </c:forEach>
                                    </select>
                                </li>
                                <li>
                                    <label for="${importInfoMap.value.siteKey}legacyDefinitions">
                                        <fmt:message key="org.jahia.admin.site.ManageSites.selectLegacyDefinitions"/>
                                    </label>
                                    <select id="${importInfoMap.value.siteKey}legacyDefinitions"
                                            name="importsInfos['${importInfoMap.key}'].selectedLegacyDefinitions">
                                        <c:forEach items="${importInfoMap.value.legacyDefinitions}" var="module">
                                            <option value="${module.absolutePath}" <c:if
                                                    test="${importInfoMap.value.selectedLegacyDefinitions eq module.name}"> selected="selected"</c:if>>${module.name}</option>
                                        </c:forEach>
                                    </select>
                                </li>
                            </c:if>
                        </ul>
                    </c:if>
                </li>
            </c:forEach>
        </ol>
        <input type="submit" name="_eventId_processImport" value="<fmt:message key="label.next"/>"/>
    </form>
</div>
