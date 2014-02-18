<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<p><strong><fmt:message key="jnt_primaryNodeType.j_supertype" /></strong>: ${currentNode.properties['j:supertype'].string}</p>
<p><strong><fmt:message key="jnt_primaryNodeType.j_mixins" /></strong>: <c:forEach items="${currentNode.properties['j:mixins']}" var="elem" varStatus="status">${status.index > 0 ? ", " : ""}${elem.string}</c:forEach></p>
<p><strong><fmt:message key="jnt_primaryNodeType.j_isAbstract" /></strong>: ${currentNode.properties['j:isAbstract'].boolean}</p>
<p><strong><fmt:message key="jnt_primaryNodeType.j_isQueryable" /></strong>: ${currentNode.properties['j:isQueryable'].boolean}</p>
<p><strong><fmt:message key="jnt_primaryNodeType.j_hasOrderableChildNodes" /></strong>: ${currentNode.properties['j:hasOrderableChildNodes'].boolean}</p>
<p><strong><fmt:message key="jnt_primaryNodeType.j_primaryItemName" /></strong>: ${currentNode.properties['j:primaryItemName'].string}</p>
<p><strong><fmt:message key="jnt_primaryNodeType.j_itemsType" /></strong>: ${currentNode.properties['j:itemsType'].string}</p>
