<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<tr>
    <td>
        ${currentNode.properties["login"].string}
    </td>
    <td>
        &nbsp;
    </td>
    <td>
        <a href="${url.base}${currentNode.path}.data.html"><fmt:message key="label.viewData"/></a>
    </td>
    <td>
        <c:if test="${renderContext.editMode && jcr:hasPermission(currentNode,'jcr:write')}">
            <div class="jahia-template-gxt" jahiatype="module" id="googleAnalyticsAccount-${currentNode.identifier}" type="existingNode" scriptInfo="" path="${currentNode.path}" template="hidden.system" dragdrop="false">
            <fmt:message key="label.rightClickHere"/>
            </div>
        </c:if>

    </td>
</tr>
