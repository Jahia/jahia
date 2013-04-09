<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.TreeMap"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<h2><fmt:message key="serverSettings.systemInfo.systemProperties"/></h2>

<div>
    <% pageContext.setAttribute("systemProperties", new TreeMap(System.getProperties())); %>
    <table class="table table-bordered table-hover table-striped" >
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
<hr/>
<h2><fmt:message key="serverSettings.systemInfo.environmentVariables"/></h2>

<div>
    <% pageContext.setAttribute("envVariables", new TreeMap(System.getenv())); %>
    <table class="table table-bordered table-hover table-striped">
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
