<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<jcr:nodeProperty node="${currentNode}" name='date' var="datePress"/>
<span class="pressRealeseDate"><fmt:formatDate value="${datePress.date.time}" pattern="dd/MM/yyyy"/></span>
<jcr:nodeProperty node="${currentNode}" name="j:defaultCategory" var="pressReleaseContainerCatKeys"/>
<c:if test="${!empty pressReleaseContainerCatKeys }">
            <span class="pressRealeseCategory">
                <fmt:message key='label.categories'/> : <c:forEach items="${pressReleaseContainerCatKeys}" var="category" varStatus="status"><c:if test="${not status.first}">,</c:if><jcr:nodeProperty node="${category.node}" name="jcr:title" var="title"/><c:choose><c:when test="${not empty title}">${title}</c:when><c:otherwise>${category.node.name}</c:otherwise></c:choose></c:forEach>
            </span>
</c:if>
<h4><a href="<c:url value='${url.base}${currentNode.path}.detail.html'/>"><jcr:nodeProperty node="${currentNode}" name='jcr:title'/></a>
</h4>

<div class="clear"></div>