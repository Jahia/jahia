<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${renderContext.editMode}">
    <div>
        <table width="100%" style="border:1px solid #DDDDDD">
            <caption>Answers for this job</caption>
            <thead style="font-weight:bold">
            <tr>
                <td width="120">Name</td>
                <td>Texte</td>
            </tr>
            </thead>
            <c:forEach items="${currentNode.nodes}" var="node">
                <template:module node="${node}"/>
            </c:forEach>
        </table>
    </div>
</c:if>