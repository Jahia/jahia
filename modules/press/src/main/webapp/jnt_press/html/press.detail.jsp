<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<jcr:nodeProperty node="${currentNode}" name="j:defaultCategory" var="pressReleaseContainerCatKeys"/>

<h3><jcr:nodeProperty node="${currentNode}" name='jcr:title'/></h3>

<jcr:nodeProperty node="${currentNode}" name='date' var="datePress"/>
<h4 class="pressRealeseDate"><fmt:formatDate value="${currentNode.properties.date.time}" pattern="dd/MM/yyyy"/></h4>

<div>${currentNode.properties.body.string}</div>
<jcr:nodeProperty node="${currentNode}" name="pdfVersion" var="pdfVersion"/>
<c:if test="${not empty pdfVersion}">
    <div><strong><fmt:message key="label.download"/> :</strong>
        <a href="${pdfVersion.node.url}">${pdfVersion.node.name}</a></div>
</c:if>
<c:if test="${!empty pressReleaseContainerCatKeys }">
            <span class="pressRealeseCategory">
                <fmt:message key='label.categories'/> : <c:forEach items="${pressReleaseContainerCatKeys}" var="category" varStatus="status"><c:if test="${not status.first}">,</c:if><jcr:nodeProperty node="${category.node}" name="jcr:title" var="title"/><c:choose><c:when test="${not empty title}">${title}</c:when><c:otherwise>${category.node.name}</c:otherwise></c:choose></c:forEach>
            </span>
</c:if>
<br/>
<span><a class="returnLink" href="<c:url value='${url.base}${jcr:getParentOfType(currentNode, "jnt:page").path}.html'/>" title='<fmt:message key="backToPreviousPage"/>'><fmt:message key='backToPreviousPage'/></a></span>
