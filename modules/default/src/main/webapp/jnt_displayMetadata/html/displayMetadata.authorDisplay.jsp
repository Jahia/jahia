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

<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<jcr:nodeProperty node="${bindedComponent}" name="j:defaultCategory" var="assignedCategories"/>
<c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ', ')}"/>
<jsp:useBean id="filteredCategories" class="java.util.LinkedHashMap"/>
<c:set var="props" value="${currentNode.properties}"/>
<c:if test="${props.creationdate.boolean}">
    ${bindedComponent.properties['jcr:created'].time}
</c:if>
<c:if test="${props.creator.boolean}">
    <c:set var="mainTemplate" value="${currentNode.properties['j:userView'].string}"/>
    <c:choose>
        <c:when test="${renderContext.editMode}">
            <div class="authorDisplayArea<c:if test="${not empty currentNode.properties['j:mockupStyle']}"> ${currentNode.properties['j:mockupStyle'].string}</c:if>">
                <c:if test="${not empty currentNode.properties['j:userView'].string}">
                    <div class="authorDisplayTemplate">
                        <span>${currentNode.properties['j:userView'].string}</span>
                    </div>
                </c:if>
            </div>
        </c:when>
        <c:otherwise>
			<c:set var="user" value="${bindedComponent.properties['jcr:createdBy'].string}"/>
            <template:module path="/users/${user}" template="${mainTemplate}"/>
        </c:otherwise>
    </c:choose>
</c:if>
<c:if test="${props.lastmodification.boolean}">
    ${bindedComponent.properties['jcr:lastModified'].time}
</c:if>
<c:if test="${props.lastcontributor.boolean}">
    ${bindedComponent.properties['jcr:lastModifiedBy'].string}
</c:if>
<c:if test="${props.description.boolean}">
    ${bindedComponent.properties['jcr:description'].string}
</c:if>
<c:if test="${props.keywords.boolean}">
    <c:if test="${not empty bindedComponent.properties['j:keywords']}">
        <c:forEach items="${bindedComponent.properties['j:keywords']}" var="keyword">
            ${keyword.string}
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




