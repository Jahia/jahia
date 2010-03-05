<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set value="${currentNode.propertiesAsString}" var="props"/>
<c:if test="${not empty props.mask}">
    <script>
        $(document).ready(function() {
            $("#${currentNode.name}").mask("${props.mask}");
        });
    </script>
</c:if>
<p class="field">
<label>${props.label}:</label>
<input type="text" id="${currentNode.name}" name="${currentNode.name}" maxlength="${props.size}"
       value="<c:if test="${empty props.mask}">${props.defaultValue}</c:if>"/>
<c:if test="${renderContext.editMode}">
    <div style="border:darksalmon solid medium; margin:5px; background:#888888;">
        <span>Add your validation elements here</span>
        <template:module path="*"/>
    </div>
</c:if>
</p>