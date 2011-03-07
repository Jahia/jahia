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

<c:if test="${empty bindedComponent}">
    <fmt:message key="notBinded"/>
</c:if>

<c:if test="${not empty bindedComponent}">
    <div class="contentinfos">
        <h3>Metadatas</h3>
    <dl>
        <c:if test="${props.creationdate.boolean}">
                <dt><fmt:message key="label.creationDate"/></dt>
                <dd><fmt:formatDate value="${bindedComponent.properties['jcr:created'].time}"
                                    pattern="dd, MMMM yyyy HH:mm"/></dd>
        </c:if>
        <c:if test="${props.creator.boolean}">
            <dl>
                <dt><fmt:message key="label.creator"/></dt>
                <dd>${bindedComponent.properties['jcr:createdBy'].string}</dd>
        </c:if>
        <c:if test="${props.lastmodification.boolean}">
                <dt><fmt:message key="label.lastModif"/></dt>
                <dd><fmt:formatDate value="${bindedComponent.properties['jcr:lastModified'].time}"
                                    pattern="dd, MMMM yyyy HH:mm"/></dd>
        </c:if>
        <c:if test="${props.lastcontributor.boolean}">
                <dt><fmt:message key="label.lastContributor"/></dt>
                <dd>${bindedComponent.properties['jcr:lastModifiedBy'].string}</dd>
        </c:if>
        <c:if test="${props.description.boolean}">
                <dt><fmt:message key="label.Description"/></dt>
                <c:if test="${not empty bindedComponent.properties['jcr:description']}">
                        <dd>${bindedComponent.properties['jcr:description'].string}  </dd>
                </c:if>
        </c:if>
        <c:if test="${props.keywords.boolean}">
                <dt><fmt:message key="label.Keywords"/></dt>
                <c:if test="${not empty bindedComponent.properties['j:keywords']}">
                    <dd>
                    <c:forEach items="${bindedComponent.properties['j:keywords']}" var="keyword">
                            ${keyword.string}
                        </c:forEach>
                    </dd>
                </c:if>
        </c:if>
        <c:if test="${props.categories.boolean}">
                <dt><fmt:message key="label.categories"/>:</dt>
                    <c:forEach items="${assignedCategories}" var="category" varStatus="status">
                        <dd>
                        <c:if test="${not empty category.node}">
                            <c:set target="${filteredCategories}" property="${category.node.properties['jcr:title'].string}"
                                   value="${category.node.properties['jcr:title'].string}"/>
                        </c:if>
                    </c:forEach>
                        </dd>
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
    </dl>
    </div>
</c:if>
