<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="option" type="org.jahia.services.content.nodetypes.initializers.ChoiceListValue"--%>
<jcr:propertyInitializers var="options" node="${currentNode}"
                          initializers="${fn:split(currentNode.properties.type.string,';')[0]}" name="type"/>
<p class="field">
    <label class="left" for="${currentNode.name}">${fn:escapeXml(currentNode.properties.label.string)}</label>
    <select name="${currentNode.name}" id="${currentNode.name}">
        <c:forEach items="${options}" var="option">
            <option value="${option.value.string}"
                    style="background:url(${option.properties.image}) no-repeat top left;padding-left:25px" <c:if test="${not empty sessionScope.formError and sessionScope.formDatas[currentNode.name][0] eq option.value.string}">selected="true"</c:if>>${option.displayName}</option>
        </c:forEach>
    </select>
</p>