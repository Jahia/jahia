<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

Page : ${currentNode.name}

<br/>

<c:forEach items="${currentNode.children}" var="child">
    <ul>
        ${child.name}

        <table border="1">
            <tr>
                <td><template:module node="child" /></td>
            </tr>
        </table>
    </ul>
</c:forEach>

<a href="<%= request.getContextPath() %>/render/default${currentNode.path}.html"><%= request.getContextPath() %>/render/default/${currentNode.path}.html</a>