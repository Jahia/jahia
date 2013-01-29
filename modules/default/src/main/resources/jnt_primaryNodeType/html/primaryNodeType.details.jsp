<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<p><fmt:message key="jnt_primaryNodeType.j_supertype" />: ${currentNode.properties['j:supertype'].string}</p>
<p><fmt:message key="jnt_primaryNodeType.j_mixins" />: <c:forEach items="${currentNode.properties['j:mixins']}" var="elem" varStatus="status">${status.index > 0 ? ", " : ""}${elem.string}</c:forEach></p>
<p><fmt:message key="jnt_primaryNodeType.j_isAbstract" />: ${currentNode.properties['j:isAbstract'].boolean}</p>
<p><fmt:message key="jnt_primaryNodeType.j_isQueryable" />: ${currentNode.properties['j:isQueryable'].boolean}</p>
<p><fmt:message key="jnt_primaryNodeType.j_hasOrderableChildNodes" />: ${currentNode.properties['j:hasOrderableChildNodes'].boolean}</p>
<p><fmt:message key="jnt_primaryNodeType.j_primaryItemName" />: ${currentNode.properties['j:primaryItemName'].string}</p>
<p><fmt:message key="jnt_primaryNodeType.j_itemsType" />: ${currentNode.properties['j:itemsType'].string}</p>
