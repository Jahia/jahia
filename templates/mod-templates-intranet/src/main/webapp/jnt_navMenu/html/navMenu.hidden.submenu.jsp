<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:set var="items" value="${currentNode.nodes}"/>
<c:if test="${renderContext.editMode || not empty items}">
<c:forEach items="${items}" var="menuItem">
    <li class="box-inner-border">
    <template:module node="${menuItem}" editable="true">
        <template:param name="subNodesTemplate" value="hidden.navMenuItem"/>
    </template:module>
    </li>
</c:forEach>
<c:if test="${renderContext.editMode}">
    <li><fmt:message key="label.add.new.content"/><template:module path="*"/></li>
</c:if>
</c:if>
