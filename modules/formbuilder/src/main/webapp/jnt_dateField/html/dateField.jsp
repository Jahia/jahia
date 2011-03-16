<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<p class="field">
<label class="left">${fn:escapeXml(currentNode.properties.label.string)}</label>
<input type="text" id="${currentNode.name}" name="${currentNode.name}"
       value="${not empty sessionScope.formError ? sessionScope.formDatas[currentNode.name][0] : ''}" readonly="readonly"/>
<ui:dateSelector fieldId="${currentNode.name}"/>
<c:if test="${renderContext.editMode}">
<div class="formMarginLeft">
    <p><fmt:message key="checkbox.listOfValidation"/></p>
    <ol>
    <c:forEach items="${jcr:getNodes(currentNode,'jnt:formElementValidation')}" var="formElement" varStatus="status">
        <li><template:module node="${formElement}" view="edit"/></li>
    </c:forEach>
    </ol>
        <div class="addvalidation">
        <span><fmt:message key="checkbox.addElements"/> </span>
        <template:module path="*"/>
    </div>
</div>
</c:if>
</p>