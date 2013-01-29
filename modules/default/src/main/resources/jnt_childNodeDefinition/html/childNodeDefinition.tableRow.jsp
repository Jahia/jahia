<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<tr>
    <td>${fn:startsWith(currentNode.name, '__node__') ? '*' : currentNode.name}</td>
    <td><c:forEach items="${currentNode.properties['j:requiredPrimaryTypes']}" var="elem" varStatus="status">${status.index > 0 ? ", " : ""}${elem.string}</c:forEach></td>
    <td>${currentNode.properties['j:mandatory'].boolean}</td>
</tr>
