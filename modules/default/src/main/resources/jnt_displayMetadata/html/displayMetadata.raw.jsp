<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:set var="boundComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<jcr:nodeProperty node="${boundComponent}" name="j:defaultCategory" var="assignedCategories"/>
<c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ', ')}"/>
<jsp:useBean id="filteredCategories" class="java.util.LinkedHashMap"/>
<c:set var="props" value="${currentNode.properties}"/>
<c:if test="${props.creationdate.boolean}">
    ${boundComponent.properties['jcr:created'].time}
</c:if>
<c:if test="${props.creator.boolean}">
    ${boundComponent.properties['jcr:createdBy'].string}
</c:if>
<c:if test="${props.lastmodification.boolean}">
    ${boundComponent.properties['jcr:lastModified'].time}
</c:if>
<c:if test="${props.lastcontributor.boolean}">
    ${boundComponent.properties['jcr:lastModifiedBy'].string}
</c:if>
<c:if test="${props.description.boolean}">
    <c:out value="${boundComponent.properties['jcr:description'].string}" />
</c:if>
<c:if test="${props.keywords.boolean}">
    <c:if test="${not empty boundComponent.properties['j:keywords']}">
        <c:forEach items="${boundComponent.properties['j:keywords']}" var="keyword" varStatus="status">
            <c:out value="${keyword.string}" />${!status.last ? separator : ''}
        </c:forEach>
    </c:if>
</c:if>
<c:if test="${props.categories.boolean}">
    <c:forEach items="${assignedCategories}" var="category" varStatus="status">
        <c:if test="${not empty category.node}">
            <c:set target="${filteredCategories}" property="${category.node.properties['jcr:title'].string}"
                   value="${category.node.properties['jcr:title'].string}"/>
        </c:if>
    </c:forEach>
    <c:choose>
        <c:when test="${not empty filteredCategories}">
            <c:forEach items="${filteredCategories}" var="category" varStatus="status">
                ${fn:escapeXml(category.value)}${!status.last ? separator : ''}
            </c:forEach>
        </c:when>
        <c:otherwise>
            <fmt:message key="label.categories.noCategory"/>
        </c:otherwise>
    </c:choose>
</c:if>
