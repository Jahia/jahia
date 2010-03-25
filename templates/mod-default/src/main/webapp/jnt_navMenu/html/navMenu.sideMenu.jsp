<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<c:set var="outerMenues" value="${jcr:getParentsOfType(currentNode, 'jnt:navMenu')}"/>
<c:if test="${not empty title.string}">
	<span><c:out value="${title.string}"/></span>
</c:if>
	<c:if test="${empty outerMenues}">
    <div id="navigationN2">	
    </c:if>
        <ul class="level_${fn:length(outerMenues) + 2}">
        <c:forEach items="${currentNode.nodes}" var="menuItem">
            <template:module node="${menuItem}" editable="true" templateWrapper="wrapper.navMenuItem">
                <template:param name="omitFormatting" value="true"/>
        	    <template:param name="subNodesTemplate" value="navMenuItem"/>
            </template:module>
        </c:forEach>
        <c:if test="${renderContext.editMode}">
            <li><fmt:message key="label.add.new.content"/><template:module path="*"/></li>
        </c:if>
        </ul>
	<c:if test="${empty outerMenues}">        
    </div>
    </c:if>
<c:if test="${renderContext.editMode}">
</fieldset>
</c:if>
