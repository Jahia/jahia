<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="search" uri="http://www.jahia.org/tags/search" %>

<c:set var="props" value="${currentNode.propertiesAsString}"
<s:form method="${props.method}" id="${props.id}" css="${props.css}" style="${props.style}">
<div class="form">
	<c:forEach items="${jcr:getNodes(currentNode,'jnt:searchFormElement')}" var="formElement">
	    <template:module node="${formElement}" template="default" editable="true"/>
	</c:forEach>
</div>
<c:if test="${renderContext.editMode}">
    <div style="border:darkorange solid medium; margin:5px; background:#888888;">
        <span>Add your new form elements here</span>
        <template:module path="*" nodeTypes="jnt:searchFormElement"/>
    </div>
</c:if>
</s:form>