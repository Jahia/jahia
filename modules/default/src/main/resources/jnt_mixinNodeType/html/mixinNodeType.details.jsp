<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<p><strong><fmt:message key="jnt_mixinNodeType.j_mixins" /></strong>: <c:forEach items="${currentNode.properties['j:mixins']}" var="elem" varStatus="status">${status.index > 0 ? ", " : ""}${elem.string}</c:forEach></p>
<p><strong><fmt:message key="jnt_mixinNodeType.j_isQueryable" /></strong>: ${currentNode.properties['j:isQueryable'].boolean}</p>
<p><strong><fmt:message key="jnt_mixinNodeType.j_itemsType" /></strong>: ${currentNode.properties['j:itemsType'].string}</p>
<p><strong><fmt:message key="jnt_mixinNodeType.j_mixinExtends" /></strong>: <c:forEach items="${currentNode.properties['j:mixinExtends']}" var="elem" varStatus="status">${status.index > 0 ? ", " : ""}${elem.string}</c:forEach></p>
