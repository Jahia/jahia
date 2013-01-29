<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<tr>
    <td>${fn:startsWith(currentNode.name, '__prop__') ? '*' : currentNode.name}</td>
    <td>${currentNode.properties['j:requiredType'].string}</td>
    <td><c:forEach items="${currentNode.properties['j:defaultValues']}" var="elem" varStatus="status">${status.index > 0 ? ", " : ""}${elem.string}</c:forEach></td>
    <td>${currentNode.properties['j:isInternationalized'].boolean}</td>
    <td>${currentNode.properties['j:mandatory'].boolean}</td>
    <td>${currentNode.properties['j:protected'].boolean}</td>
    <td>${currentNode.properties['j:isHidden'].boolean}</td>
</tr>
