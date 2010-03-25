<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<div id="navigationN1">
<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<c:if test="${not empty title.string}">
    <span><c:out value="${title.string}"/></span>
</c:if>
<c:set var="items" value="${currentNode.nodes}"/>
<c:if test="${renderContext.editMode || not empty items}">
<ul class="level_1">
<c:forEach items="${items}" var="menuItem">
    <template:module node="${menuItem}" editable="true" templateWrapper="wrapper.navMenuItem">
        <template:param name="omitFormatting" value="true"/>
        <template:param name="subNodesTemplate" value="hidden.navMenuItem"/>
    </template:module>
</c:forEach>
<c:if test="${renderContext.editMode}">
    <li><fmt:message key="label.add.new.content"/><template:module path="*"/></li>
</c:if>
</ul>
</c:if>
</div>