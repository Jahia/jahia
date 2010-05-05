<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>

<tr class="evenLine">
    <td align="center">
        <input type="checkbox" value="ACME" name="sitebox">
    </td>
    <td >
        ${fn:escapeXml(currentNode.primaryNodeType.name)}
    </td>
    <td><a href="${url.base}${currentNode.path}.system.html"><c:if test="${!empty currentNode.properties['jcr:title'].string}">
        ${fn:escapeXml(currentNode.properties['jcr:title'].string)}
    </c:if>
        <c:if test="${empty currentNode.properties['jcr:title'].string}">
        ${fn:escapeXml(currentNode.name)}
    </c:if></a>
    </td>
    <td>
        <fmt:formatDate value="${currentNode.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
    </td>
    <td>
        <fmt:formatDate value="${currentNode.properties['jcr:lastModified'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
    </td align="center">
    <td>
        <fmt:formatDate value="${currentNode.properties['j:lastPublished'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
    </td>
    <td>
        <workflow:workflowsForNode node="${currentNode}" var="wfs"/>
        <c:forEach items="${wfs}" var="wf">
            <c:if test="${not empty wf.formResourceName}">
                ${wf.name}
            </c:if>
        </c:forEach>
    </td>
    <td class="lastCol">
    <c:if test="${currentNode.locked}">
        <img height="16" width="16" border="0" style="cursor: pointer;" title="Locked" alt="Supprimer" src="${url.currentModule}/images/icons/locked.gif">
    </c:if>
    </td>
</tr>