<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.TreeMap"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<h3><fmt:message key="serverSettings.systemInfo.systemProperties"/></h3>

<div>
    <% pageContext.setAttribute("systemProperties", new TreeMap(System.getProperties())); %>
    <table width="100%" border="0" cellspacing="0" cellpadding="5" style="table-layout: fixed;">
        <c:forEach items="${systemProperties}" var="prop" varStatus="loopStatus">
        <tr class="${(loopStatus.index + 1) % 2 == 0 ? 'evenLine' : 'oddLine'}">
            <td style="width: 30%; overflow: hidden;" title="${fn:escapeXml(prop.key)}">
                <strong>${fn:escapeXml(prop.key)}</strong>
            </td>
            <td style="width: 70%; overflow: hidden;" title="${fn:escapeXml(prop.value)}">
                ${fn:escapeXml(prop.value)}
            </td>
        </tr>
        </c:forEach>
    </table>
</div>

<h3><fmt:message key="serverSettings.systemInfo.environmentVariables"/></h3>

<div>
    <% pageContext.setAttribute("envVariables", new TreeMap(System.getenv())); %>
    <table width="100%" border="0" cellspacing="0" cellpadding="5" style="table-layout: fixed;">
        <c:forEach items="${envVariables}" var="prop" varStatus="loopStatus">
        <tr class="${(loopStatus.index + 1) % 2 == 0 ? 'evenLine' : 'oddLine'}">
            <td style="width: 30%; overflow: hidden;" title="${fn:escapeXml(prop.key)}">
                <strong>${fn:escapeXml(prop.key)}</strong>
            </td>
            <td style="width: 70%; overflow: hidden;" title="${fn:escapeXml(prop.value)}">
                ${fn:escapeXml(prop.value)}
            </td>
        </tr>
        </c:forEach>
    </table>
</div>
