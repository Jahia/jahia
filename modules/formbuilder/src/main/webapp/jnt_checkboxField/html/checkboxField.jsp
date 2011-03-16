<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:set value="${currentNode.propertiesAsString}" var="props"/>
<label class="left" for="${currentNode.name}">${props.label}</label>
<div class="formMarginLeft">
<c:forEach items="${jcr:getNodes(currentNode,'jnt:formListElement')}" var="option">
    <c:set var="isChecked" value="false"/>
    <c:if test="${not empty sessionScope.formError}">
    <c:forEach items="${sessionScope.formDatas[currentNode.name]}" var="checked">
        <c:if test="${option.properties.value.string eq checked}">
            <c:set var="isChecked" value="true"/>
        </c:if>
        </c:forEach>
    </c:if>
    <input type="checkbox" name="${currentNode.name}" id="${currentNode.name}" value="${option.properties.value.string}" <c:if test="${isChecked eq 'true'}">checked="true"</c:if>/>
    <label for="${currentNode.name}">${option.properties.label.string}</label>
</c:forEach>
<c:if test="${renderContext.editMode}">
    <p><fmt:message key="checkbox.listOfOptions"/> </p>
    <ol>
        <c:forEach items="${jcr:getNodes(currentNode,'jnt:formListElement')}" var="option">
            <li><template:module node="${option}" view="default" editable="true"/></li>
        </c:forEach>
    </ol>
    <p><fmt:message key="checkbox.listOfValidation"/> </p>
    <ol>
    <c:forEach items="${jcr:getNodes(currentNode,'jnt:formElementValidation')}" var="formElement" varStatus="status">
        <li><template:module node="${formElement}" view="edit"/></li>
    </c:forEach>
    </ol>
    <div class="addvalidation">
        <span><fmt:message key="checkbox.addElements"/> </span>
        <template:module path="*"/>
    </div>
</c:if>
</div>
