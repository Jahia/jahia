<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${!renderContext.editMode}">
    <c:forEach items="${currentNode.nodes}" var="subchild">
        <li class="widget color-box" id="${subchild.path}">
            <div class="widget-head">
                <h3><jcr:nodeProperty node="${subchild}" name='jcr:title'/></h3>
            </div>
            <div class="widget-content" id="widget${subchild.UUID}">
                <script type="text/javascript">
                    $(document).ready(function() {
                        $.get('${url.base}${subchild.path}.html.ajax', null, function(data) {
                            $("#widget${subchild.UUID}").html(data);
                        });
                    });
                </script>
            </div>
        </li>
    </c:forEach>
</c:if>
<c:if test="${renderContext.editMode}">
    <li class="widget color-box"><fmt:message
            key="label.portal.column"/>&nbsp;${fn:substring(currentNode.name,6,7)}</li>
    <c:forEach items="${currentNode.nodes}" var="subchild">
        <li class="widget color-box" id="${subchild.path}">
            <template:module node="${subchild}"/>
        </li>
    </c:forEach>
    <li>
        <template:module path="*"/>
    </li>
</c:if>
