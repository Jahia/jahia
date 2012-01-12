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
        <c:set var="user" value="${functions:lookupUser(boundComponent.properties['jcr:createdBy'].string).localPath}"/>
        <template:addCacheDependency path="${user}"/>
        <template:module path="${user}" view="${mainTemplate}">
            <template:param name="displayFirstName" value="${currentNode.properties['j:firstName'].string}"/>
            <template:param name="displayLastName" value="${currentNode.properties['j:lastName'].string}"/>
            <template:param name="displayTitle" value="${currentNode.properties['j:title'].string}"/>
            <template:param name="displayGender" value="${currentNode.properties['j:gender'].string}"/>
            <template:param name="displayBirthDate" value="${currentNode.properties['j:birthDate'].string}"/>
            <template:param name="displayPicture" value="${currentNode.properties['j:picture'].string}"/>
            <template:param name="displayEmail" value="${currentNode.properties['j:email'].string}"/>
            <template:param name="displayAbout" value="${currentNode.properties['j:about'].string}"/>
        </template:module>
    </c:otherwise>
</c:choose>


