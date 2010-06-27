<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:nodeProperty node="${currentNode}" name="insertPosition" var="insertPosition"/>
<jcr:nodeProperty node="${currentNode}" name="insertType" var="insertType"/>
<jcr:nodeProperty node="${currentNode}" name="insertWidth" var="insertWidth"/>
<jcr:nodeProperty node="${currentNode}" name="insertText" var="insertText"/>
<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>

<h3><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>
<c:if test="${not empty insertText}">
<div class='${insertType.string}-top float${insertPosition.string}'
     style='width:${insertWidth.string}px'>

    <div class="${insertType.string}-bottom">
        ${insertText.string}
    </div>
</div>
</c:if>
<div class="float${currentNode.properties.align.string}">
    <c:if test="${!empty image}">
        <img src="${image.node.url}" alt="${image.node.url}" align="${currentNode.properties.align.string}"/>
    </c:if>
</div>
<div>
    ${currentNode.properties.body.string}
</div>
<div class="clear"></div>
