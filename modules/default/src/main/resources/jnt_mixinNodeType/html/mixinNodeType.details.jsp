<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<p><fmt:message key="jnt_mixinNodeType.j_mixins" />: <c:forEach items="${currentNode.properties['j:mixins']}" var="elem" varStatus="status">${status.index > 0 ? ", " : ""}${elem.string}</c:forEach></p>
<p><fmt:message key="jnt_mixinNodeType.j_isQueryable" />: ${currentNode.properties['j:isQueryable'].boolean}</p>
<p><fmt:message key="jnt_mixinNodeType.j_itemsType" />: ${currentNode.properties['j:itemsType'].string}</p>
<p><fmt:message key="jnt_mixinNodeType.j_mixinExtends" />: <c:forEach items="${currentNode.properties['j:mixinExtends']}" var="elem" varStatus="status">${status.index > 0 ? ", " : ""}${elem.string}</c:forEach></p>
