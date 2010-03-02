<%@include file="../include/header.jspf" %>
<c:forEach items="${currentList}" var="subchild" begin="${begin}" end="${end}">
    <template:module node="${subchild}" template="${subNodesTemplate}" editable="${editable}">
        <c:if test="${not empty forcedSkin}">
            <template:param name="forcedSkin" value="${forcedSkin}"/>
        </c:if>
        <c:if test="${not empty renderOptions}">
            <template:param name="renderOptions" value="${renderOptions}"/>
        </c:if>
    </template:module>
</c:forEach>
<div class="clear"></div>
<c:if test="${editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>
<%@include file="../include/footer.jspf" %>